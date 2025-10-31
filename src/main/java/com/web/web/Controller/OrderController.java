package com.web.web.Controller;

import com.web.web.Dto.OrderDTO;
import com.web.web.Dto.ResponseDTO;
import com.web.web.Entity.User;
import com.web.web.Repository.UserRepository;
import com.web.web.Service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final UserRepository userRepository;

    @Autowired
    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = principal instanceof UserDetails ? ((UserDetails) principal).getUsername() : principal.toString();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("User not found for username: {}", username);
            throw new IllegalArgumentException("Người dùng không hợp lệ");
        }
        return user.getId();
    }

    // Người dùng đặt hàng từ giỏ hàng
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<OrderDTO>> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        try {
            Long userId = getCurrentUserId();
            logger.info("Creating order for user {} with delivery address: {} and payment method: {}",
                    userId, orderRequest.getDeliveryAddress(), orderRequest.getPaymentMethod());
            OrderDTO order = orderService.createOrder(userId, orderRequest.getDeliveryAddress(),
                    orderRequest.getPaymentMethod());
            return ResponseEntity.status(201).body(new ResponseDTO<>("Đặt hàng thành công", order));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // Người dùng đặt hàng trực tiếp từ sản phẩm
    @PostMapping("/create-from-product")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<OrderDTO>> createOrderFromProduct(@Valid @RequestBody OrderFromProductRequest orderRequest) {
        try {
            Long userId = getCurrentUserId();
            logger.info("Creating order from product for user {} with productId: {}, quantity: {}, delivery address: {}, and payment method: {}",
                    userId, orderRequest.getProductId(), orderRequest.getQuantity(), orderRequest.getDeliveryAddress(), orderRequest.getPaymentMethod());
            OrderDTO order = orderService.createOrderFromProduct(userId, orderRequest.getProductId(), orderRequest.getQuantity(),
                    orderRequest.getDeliveryAddress(), orderRequest.getPaymentMethod());
            return ResponseEntity.status(201).body(new ResponseDTO<>("Đặt hàng trực tiếp từ sản phẩm thành công", order));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // Người dùng xem danh sách đơn hàng của mình
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<List<OrderDTO>>> getUserOrders() {
        try {
            Long userId = getCurrentUserId();
            logger.info("Fetching orders for user {}", userId);
            List<OrderDTO> orders = orderService.getUserOrders(userId);
            return ResponseEntity.ok(new ResponseDTO<>("Lấy danh sách đơn hàng thành công", orders));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // Admin xem tất cả đơn hàng
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<OrderDTO>>> getAllOrders() {
        try {
            logger.info("Admin fetching all orders");
            List<OrderDTO> orders = orderService.getAllOrders();
            return ResponseEntity.ok(new ResponseDTO<>("Lấy danh sách tất cả đơn hàng thành công", orders));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // Admin cập nhật trạng thái đơn hàng
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<OrderDTO>> updateOrderStatus(
            @PathVariable @Positive Long id,
            @RequestParam @Pattern(regexp = "^(PENDING|CONFIRMED|SHIPPING|DELIVERED|CANCELLED|CANCEL_REQUESTED)$",
                    message = "Trạng thái đơn hàng phải là PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED hoặc CANCEL_REQUESTED") String status) {
        try {
            logger.info("Admin updating order {} to status {}", id, status);
            OrderDTO order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(new ResponseDTO<>("Cập nhật trạng thái đơn hàng thành công", order));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // Admin cập nhật trạng thái thanh toán
    @PutMapping("/{id}/payment-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<OrderDTO>> updatePaymentStatus(
            @PathVariable @Positive Long id,
            @RequestParam @Pattern(regexp = "^(PENDING|PAID|FAILED|REFUNDED)$",
                    message = "Trạng thái thanh toán phải là PENDING, PAID, FAILED hoặc REFUNDED") String status) {
        try {
            logger.info("Admin updating payment status for order {} to {}", id, status);
            OrderDTO order = orderService.updatePaymentStatus(id, status);
            return ResponseEntity.ok(new ResponseDTO<>("Cập nhật trạng thái thanh toán thành công", order));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // Admin cập nhật thời gian giao hàng
    @PutMapping("/{id}/delivery-date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<OrderDTO>> updateDeliveryDate(@PathVariable @Positive Long id, @Valid @RequestBody DeliveryDateRequest deliveryDateRequest) {
        try {
            logger.info("Admin updating delivery date for order {} to {}", id, deliveryDateRequest.getDeliveryDate());
            OrderDTO order = orderService.updateDeliveryDateByAdmin(id, deliveryDateRequest.getDeliveryDate());
            return ResponseEntity.ok(new ResponseDTO<>("Cập nhật thời gian giao hàng thành công", order));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // Người dùng yêu cầu hủy đơn hàng
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<ResponseDTO<OrderDTO>> cancelOrder(@PathVariable @Positive Long id) {
        try {
            Long userId = getCurrentUserId();
            logger.info("Canceling order {} for user {}", id, userId);
            OrderDTO order = orderService.cancelOrder(id, userId);
            return ResponseEntity.ok(new ResponseDTO<>("Yêu cầu hủy đơn hàng thành công", order));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // Admin đồng ý yêu cầu hủy đơn hàng
    @PutMapping("/{id}/approve-cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<OrderDTO>> approveCancelOrder(@PathVariable @Positive Long id) {
        try {
            logger.info("Admin approving cancel request for order {}", id);
            OrderDTO order = orderService.approveCancelOrderByAdmin(id);
            return ResponseEntity.ok(new ResponseDTO<>("Đồng ý yêu cầu hủy đơn hàng thành công", order));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // Admin từ chối yêu cầu hủy đơn hàng
    @PutMapping("/{id}/reject-cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<OrderDTO>> rejectCancelOrder(@PathVariable @Positive Long id) {
        try {
            logger.info("Admin rejecting cancel request for order {}", id);
            OrderDTO order = orderService.rejectCancelOrderByAdmin(id);
            return ResponseEntity.ok(new ResponseDTO<>("Từ chối yêu cầu hủy đơn hàng thành công", order));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    // Admin xóa đơn hàng
    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Object>> deleteOrder(@PathVariable @Positive Long id) {
        try {
            logger.info("Admin deleting order {}", id);
            orderService.deleteOrderByAdmin(id);
            return ResponseEntity.ok(new ResponseDTO<>("Xóa đơn hàng thành công", null));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }
}

class OrderRequest {
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 255, message = "Địa chỉ giao hàng không được vượt quá 255 ký tự")
    private String deliveryAddress;

    @NotBlank(message = "Hình thức thanh toán không được để trống")

    private String paymentMethod;

    // Getters and setters
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

class OrderFromProductRequest {
    @Positive(message = "ID sản phẩm phải lớn hơn 0")
    private Long productId;

    @Positive(message = "Số lượng phải lớn hơn 0")
    private int quantity;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 255, message = "Địa chỉ giao hàng không được vượt quá 255 ký tự")
    private String deliveryAddress;

    @NotBlank(message = "Hình thức thanh toán không được để trống")

    private String paymentMethod;

    // Getters and setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

class DeliveryDateRequest {
    private LocalDateTime deliveryDate;

    // Getters and setters
    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}