package com.web.web.Service;

import com.web.web.Config.SePayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility ký / verify HMAC-SHA256 cho SePay Payment Gateway.
 *
 * Theo tài liệu SePay:
 * - Khi init checkout: signature = HMAC_SHA256(secretKey,
 * "merchant_id|order_id|amount|currency")
 * - Khi nhận IPN: signature = HMAC_SHA256(secretKey,
 * "merchant_id|order_id|amount|currency|status|transaction_id")
 *
 * Lưu ý: SePay trong docs dùng thứ tự field theo quy ước, triển khai dưới đây
 * theo
 * convention ghép các giá trị bằng dấu "|" theo thứ tự cố định đã thống nhất.
 * Nếu SePay yêu cầu algorithm khác (ví dụ sort alphabetical toàn bộ params),
 * điều
 * chỉnh tại {@link #buildRawSignInit} / {@link #buildRawSignIpn}.
 */
@Component
public class SePaySignatureUtil {

    private static final Logger log = LoggerFactory.getLogger(SePaySignatureUtil.class);
    private static final String HMAC_ALGO = "HmacSHA256";

    private final SePayProperties props;

    public SePaySignatureUtil(SePayProperties props) {
        this.props = props;
    }

    // ─── PUBLIC API ─────────────────────────────────────────────────────────

    /**
     * Sinh signature cho request init checkout.
     * Raw string: merchant_id|order_id|amount|currency
     */
    public String signInit(String merchantId, String orderId, String amount, String currency) {
        String raw = buildRawSignInit(merchantId, orderId, amount, currency);
        return hmacHex(raw, props.getSecretKey());
    }

    /**
     * Sinh signature từ toàn bộ map params (sort theo key alphabetical),
     * dùng nếu SePay yêu cầu ký toàn bộ.
     */
    public String signParams(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if ("signature".equalsIgnoreCase(e.getKey()))
                continue;
            if (e.getValue() == null)
                continue;
            if (!first)
                sb.append('&');
            sb.append(e.getKey()).append('=').append(e.getValue());
            first = false;
        }
        return hmacHex(sb.toString(), props.getSecretKey());
    }

    /**
     * Sinh signature cho luồng checkout form POST theo docs SePay.
     *
     * Chuỗi ký: key=value,key=value,... với thứ tự field cố định.
     * Kết quả: Base64(HMAC_SHA256(raw, secretKey)).
     */
    public String signCheckoutFormFields(Map<String, String> fields) {
        String[] signedOrder = {
                "merchant",
                "operation",
                "payment_method",
                "order_amount",
                "currency",
                "order_invoice_number",
                "order_description",
                "customer_id",
                "success_url",
                "error_url",
                "cancel_url"
        };

        List<String> parts = new ArrayList<>();
        for (String key : signedOrder) {
            if (!fields.containsKey(key)) {
                continue;
            }
            parts.add(key + "=" + nz(fields.get(key)));
        }

        String raw = String.join(",", parts);
        return hmacBase64(raw, props.getSecretKey());
    }

    /**
     * Verify signature IPN.
     * Raw: merchant_id|order_id|amount|currency|status|transaction_id
     */
    public boolean verifyIpn(String merchantId,
            String orderId,
            String amount,
            String currency,
            String status,
            String transactionId,
            String signature) {
        if (signature == null || signature.isBlank()) {
            log.warn("SePay IPN signature missing");
            return false;
        }
        String raw = buildRawSignIpn(merchantId, orderId, amount, currency, status, transactionId);
        String expected = hmacHex(raw, props.getSecretKey());
        boolean ok = constantTimeEquals(expected, signature);
        if (!ok) {
            log.warn("SePay IPN signature mismatch. raw='{}' expected='{}' got='{}'", raw, expected, signature);
        }
        return ok;
    }

    /**
     * Verify với toàn bộ map params (sort alphabetical), phòng trường hợp SePay ký
     * kiểu này.
     */
    public boolean verifyParams(Map<String, String> params, String signature) {
        if (signature == null || signature.isBlank())
            return false;
        String expected = signParams(params);
        return constantTimeEquals(expected, signature);
    }

    // ─── INTERNAL ───────────────────────────────────────────────────────────

    private String buildRawSignInit(String merchantId, String orderId, String amount, String currency) {
        return String.join("|",
                nz(merchantId),
                nz(orderId),
                nz(amount),
                nz(currency));
    }

    private String buildRawSignIpn(String merchantId, String orderId, String amount,
            String currency, String status, String transactionId) {
        return String.join("|",
                nz(merchantId),
                nz(orderId),
                nz(amount),
                nz(currency),
                nz(status),
                nz(transactionId));
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String hmacHex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot compute HMAC-SHA256 for SePay", e);
        }
    }

    private static String hmacBase64(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot compute Base64 HMAC-SHA256 for SePay", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null)
            return false;
        if (a.length() != b.length())
            return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
