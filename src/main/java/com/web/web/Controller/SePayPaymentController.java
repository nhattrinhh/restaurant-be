package com.web.web.Controller;

import com.web.web.Config.SePayProperties;
import com.web.web.Dto.OrderDTO;
import com.web.web.Dto.ResponseDTO;
import com.web.web.Dto.SePayCheckoutRequest;
import com.web.web.Dto.SePayCheckoutResponse;
import com.web.web.Dto.SePayIpnPayload;
import com.web.web.Entity.User;
import com.web.web.Repository.UserRepository;
import com.web.web.Service.OrderService;
import com.web.web.Service.SePaySignatureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller xử lý tích hợp SePay Payment Gateway.
 *
 * Flow: BE tạo Order + form fields đã ký → FE auto-submit POST thẳng tới
 * checkout URL của SePay bằng browser.
 *
 * Endpoints:
 * - POST /api/payment/sepay/init — authenticated user → tạo Order + trả
 * paymentUrl/formFields
 * - POST /api/payment/sepay/ipn — SePay callback (permitAll) → verify + mark
 * PAID
 */
@RestController
@RequestMapping("/api/payment/sepay")
public class SePayPaymentController {

    private static final Logger log = LoggerFactory.getLogger(SePayPaymentController.class);

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final SePayProperties props;
    private final SePaySignatureUtil signatureUtil;

    public SePayPaymentController(OrderService orderService,
            UserRepository userRepository,
            SePayProperties props,
            SePaySignatureUtil signatureUtil) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.props = props;
        this.signatureUtil = signatureUtil;
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = principal instanceof UserDetails ? ((UserDetails) principal).getUsername()
                : principal.toString();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Người dùng không hợp lệ");
        }
        return user.getId();
    }

    private String buildDeliveryAddress(SePayCheckoutRequest req) {
        StringBuilder sb = new StringBuilder();
        if (req.getTableId() != null) {
            sb.append("Dine-in | Bàn ID ").append(req.getTableId());
        } else {
            sb.append("Dine-in");
        }
        if (req.getSpecialRequests() != null && !req.getSpecialRequests().isBlank()) {
            sb.append(" | Ghi chú: ").append(req.getSpecialRequests().trim());
        }
        return sb.toString();
    }

    // ═══ 1. INIT CHECKOUT ═════════════════════════════════════════════════════

    @PostMapping("/init")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<SePayCheckoutResponse>> initCheckout(@RequestBody SePayCheckoutRequest req) {
        try {
            // Validate cấu hình
            if (props.getMerchantId() == null || props.getMerchantId().isBlank()
                    || props.getSecretKey() == null || props.getSecretKey().isBlank()) {
                log.error("SePay chưa cấu hình MERCHANT_ID hoặc SECRET_KEY");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ResponseDTO<>("Cổng thanh toán chưa được cấu hình. Vui lòng liên hệ quản trị viên.",
                                null));
            }

            Long userId = getCurrentUserId();
            String deliveryAddress = buildDeliveryAddress(req);
            String paymentMethod = req.getPaymentMethod() == null ? "ONLINE_PAYMENT" : req.getPaymentMethod();

            // Tạo Order (paymentStatus=PENDING, orderStatus=PENDING)
            OrderDTO order;
            if (req.getProductId() != null && req.getQuantity() != null && req.getQuantity() > 0) {
                order = orderService.createOrderFromProduct(userId, req.getProductId(), req.getQuantity(),
                        deliveryAddress, paymentMethod);
            } else {
                order = orderService.createOrder(userId, deliveryAddress, paymentMethod);
            }

            long amountLong = (long) order.getTotalAmount();
            String orderInvoiceNumber = "ORD" + order.getId();
            String currency = props.getCurrency();
            String description = "Thanh toan don hang " + orderInvoiceNumber;

            String base = stripTrailingSlash(props.getReturnBaseUrl());
            String successUrl = base + "/payment/success?order_id=" + orderInvoiceNumber;
            String errorUrl = base + "/payment/error?order_id=" + orderInvoiceNumber;
            String cancelUrl = base + "/payment/cancel?order_id=" + orderInvoiceNumber;

            Map<String, String> formFields = new LinkedHashMap<>();
            formFields.put("merchant", props.getMerchantId());
            formFields.put("operation", "PURCHASE");
            formFields.put("payment_method", "BANK_TRANSFER");
            formFields.put("order_amount", String.valueOf(amountLong));
            formFields.put("currency", currency);
            formFields.put("order_invoice_number", orderInvoiceNumber);
            formFields.put("order_description", description);
            formFields.put("customer_id", String.valueOf(userId));
            formFields.put("success_url", successUrl);
            formFields.put("error_url", errorUrl);
            formFields.put("cancel_url", cancelUrl);

            String signature = signatureUtil.signCheckoutFormFields(formFields);
            formFields.put("signature", signature);

            String paymentUrl = resolveCheckoutInitUrl(props.getCheckoutUrl());

            SePayCheckoutResponse res = new SePayCheckoutResponse();
            res.setPaymentUrl(paymentUrl);
            res.setFormFields(formFields);
            res.setOrderId(order.getId());
            res.setAmount(order.getTotalAmount());

            log.info("SePay init OK → orderId={} paymentUrl={}", order.getId(), paymentUrl);
            return ResponseEntity.ok(new ResponseDTO<>("Khởi tạo thanh toán thành công", res));

        } catch (IllegalArgumentException e) {
            log.warn("SePay init bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("SePay init error", e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // ═══ 2. IPN CALLBACK ══════════════════════════════════════════════════════

    @PostMapping("/ipn")
    public ResponseEntity<Map<String, Object>> receiveIpn(@RequestBody SePayIpnPayload payload) {
        log.info("SePay IPN received: {}", payload);

        try {
            if (payload.getMerchantId() != null
                    && !payload.getMerchantId().equals(props.getMerchantId())) {
                log.warn("SePay IPN merchant mismatch: expected={} got={}",
                        props.getMerchantId(), payload.getMerchantId());
                return okIpn(false, "merchant mismatch");
            }

            boolean sigOk = signatureUtil.verifyIpn(
                    props.getMerchantId(),
                    payload.getOrderId(),
                    payload.getAmount(),
                    payload.getCurrency() == null ? props.getCurrency() : payload.getCurrency(),
                    payload.getStatus(),
                    payload.getTransactionId(),
                    payload.getSignature());

            if (!sigOk) {
                log.warn("SePay IPN signature invalid — bỏ qua để tránh giả mạo");
                return okIpn(false, "invalid signature");
            }

            Long orderId = parseOrderIdFromSePay(payload.getOrderId());
            if (orderId == null) {
                log.warn("SePay IPN không parse được orderId: {}", payload.getOrderId());
                return okIpn(false, "invalid order_id");
            }

            String status = payload.getStatus() == null ? "" : payload.getStatus().toUpperCase();
            switch (status) {
                case "SUCCESS":
                case "PAID":
                case "COMPLETED":
                    orderService.updatePaymentStatus(orderId, "PAID");
                    orderService.updateOrderStatus(orderId, "CONFIRMED");
                    log.info("SePay IPN: Order {} → PAID + CONFIRMED", orderId);
                    break;
                case "FAILED":
                case "ERROR":
                    orderService.updatePaymentStatus(orderId, "FAILED");
                    log.info("SePay IPN: Order {} → FAILED", orderId);
                    break;
                case "CANCELLED":
                case "CANCELED":
                    orderService.updatePaymentStatus(orderId, "FAILED");
                    orderService.updateOrderStatus(orderId, "CANCELLED");
                    log.info("SePay IPN: Order {} → CANCELLED", orderId);
                    break;
                default:
                    log.info("SePay IPN: unknown status '{}' cho order {}", status, orderId);
            }

            return okIpn(true, "ok");
        } catch (Exception e) {
            log.error("SePay IPN processing error", e);
            return okIpn(false, "server error");
        }
    }

    // ─── Utils ──────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> okIpn(boolean success, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", success);
        body.put("message", message);
        return ResponseEntity.ok(body);
    }

    private static Long parseOrderIdFromSePay(String raw) {
        if (raw == null)
            return null;
        String digits = raw.replaceAll("\\D", "");
        if (digits.isBlank())
            return null;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String stripTrailingSlash(String url) {
        if (url == null)
            return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String resolveCheckoutInitUrl(String checkoutBaseOrInitUrl) {
        String base = stripTrailingSlash(checkoutBaseOrInitUrl);
        if (base.isBlank()) {
            return "https://pay-sandbox.sepay.vn/v1/checkout/init";
        }
        if (base.endsWith("/v1/checkout/init")) {
            return base;
        }
        return base + "/v1/checkout/init";
    }
}
