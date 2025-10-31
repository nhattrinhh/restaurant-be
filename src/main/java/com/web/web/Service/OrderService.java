package com.web.web.Service;
import com.web.web.Dto.OrderDTO;
import com.web.web.Dto.OrderItemDTO;
import com.web.web.Entity.*;
import com.web.web.Repository.OrderRepository;
import com.web.web.Repository.PaymentRepository;
import com.web.web.Repository.ProductRepository;
import com.web.web.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        ProductRepository productRepository, PaymentRepository paymentRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
        this.cartService = cartService;
    }

    // Đặt hàng từ giỏ hàng
    @Transactional
    public OrderDTO createOrder(Long userId, String deliveryAddress, String paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        Cart cart = cartService.getCartEntity(userId);
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống");
        }

        // Validate payment method
        Payment.PaymentMethod paymentMethodEnum;
        try {
            paymentMethodEnum = Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Hình thức thanh toán không hợp lệ");
        }

        Order order = new Order();
        order.setUser(user);
        order.setFullname(user.getFullname());
        order.setEmail(user.getEmail());
        order.setPhoneNumber(user.getPhoneNumber());
        order.setDeliveryAddress(deliveryAddress);
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setOrderStatus(Order.OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getDiscountedPrice() > 0 ? product.getDiscountedPrice() : product.getOriginalPrice());
            orderItem.updateSubtotal();
            orderItems.add(orderItem);
            totalAmount += orderItem.getSubtotal();
        }
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Create Payment entity
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentMethod(paymentMethodEnum);
        paymentRepository.save(payment);

        cartService.clearCart(userId);

        return convertToDTO(savedOrder);
    }

    // Đặt hàng trực tiếp từ sản phẩm
    @Transactional
    public OrderDTO createOrderFromProduct(Long userId, Long productId, int quantity, String deliveryAddress, String paymentMethod) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // Validate product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0");
        }

        // Validate product status
        if (!"AVAILABLE".equalsIgnoreCase(product.getStatus())) {
            throw new IllegalArgumentException("Sản phẩm không khả dụng để đặt hàng");
        }

        // Validate payment method
        Payment.PaymentMethod paymentMethodEnum;
        try {
            paymentMethodEnum = Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Hình thức thanh toán không hợp lệ");
        }

        // Create Order
        Order order = new Order();
        order.setUser(user);
        order.setFullname(user.getFullname());
        order.setEmail(user.getEmail());
        order.setPhoneNumber(user.getPhoneNumber());
        order.setDeliveryAddress(deliveryAddress);
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setOrderStatus(Order.OrderStatus.PENDING);

        // Create OrderItem
        List<OrderItem> orderItems = new ArrayList<>();
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(product.getDiscountedPrice() > 0 ? product.getDiscountedPrice() : product.getOriginalPrice());
        orderItem.updateSubtotal();
        orderItems.add(orderItem);

        // Calculate total amount
        double totalAmount = orderItem.getSubtotal();
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        // Save Order
        Order savedOrder = orderRepository.save(order);

        // Create and save Payment
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentMethod(paymentMethodEnum);
        paymentRepository.save(payment);

        return convertToDTO(savedOrder);
    }

    // Lấy danh sách đơn hàng của người dùng
    @Transactional
    public List<OrderDTO> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        return orderRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy tất cả đơn hàng
    @Transactional
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Cập nhật trạng thái đơn hàng
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        try {
            Order.OrderStatus statusEnum = Order.OrderStatus.valueOf(newStatus.toUpperCase());
            order.setOrderStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ");
        }
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    // Cập nhật trạng thái thanh toán
    @Transactional
    public OrderDTO updatePaymentStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        try {
            Order.PaymentStatus statusEnum = Order.PaymentStatus.valueOf(newStatus.toUpperCase());
            order.setPaymentStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái thanh toán không hợp lệ");
        }
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    // Người dùng yêu cầu hủy đơn hàng
    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn hàng này");
        }
        if (order.getOrderStatus() != Order.OrderStatus.PENDING && order.getOrderStatus() != Order.OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể hủy đơn hàng ở trạng thái Chờ xác nhận hoặc Đã xác nhận");
        }
        order.setOrderStatus(Order.OrderStatus.CANCEL_REQUESTED);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    // Admin đồng ý yêu cầu hủy đơn hàng
    @Transactional
    public OrderDTO approveCancelOrderByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        if (order.getOrderStatus() != Order.OrderStatus.CANCEL_REQUESTED) {
            throw new IllegalArgumentException("Đơn hàng không ở trạng thái yêu cầu hủy");
        }
        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    // Admin từ chối yêu cầu hủy đơn hàng
    @Transactional
    public OrderDTO rejectCancelOrderByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        if (order.getOrderStatus() != Order.OrderStatus.CANCEL_REQUESTED) {
            throw new IllegalArgumentException("Đơn hàng không ở trạng thái yêu cầu hủy");
        }
        order.setOrderStatus(Order.OrderStatus.CONFIRMED);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    // Admin xóa đơn hàng
    @Transactional
    public void deleteOrderByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        if (order.getOrderStatus() != Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Chỉ có thể xóa đơn hàng ở trạng thái Đã hủy");
        }
        // Xóa Payment liên quan
        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment != null) {
            paymentRepository.delete(payment);
        }
        // Xóa Order (OrderItem sẽ tự động bị xóa nhờ cascade)
        orderRepository.delete(order);
    }

    // Admin cập nhật thời gian giao hàng
    @Transactional
    public OrderDTO updateDeliveryDateByAdmin(Long orderId, LocalDateTime newDeliveryDate) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        if (newDeliveryDate == null) {
            throw new IllegalArgumentException("Thời gian giao hàng không được để trống");
        }
        if (newDeliveryDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian giao hàng không được là thời điểm trong quá khứ");
        }
        order.setDeliveryDate(newDeliveryDate);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    // Chuyển đổi Order sang OrderDTO
    private OrderDTO convertToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setFullname(order.getFullname());
        orderDTO.setEmail(order.getEmail());
        orderDTO.setPhoneNumber(order.getPhoneNumber());
        orderDTO.setDeliveryAddress(order.getDeliveryAddress());
        orderDTO.setOrderDate(order.getOrderDate());
        orderDTO.setDeliveryDate(order.getDeliveryDate());
        orderDTO.setPaymentStatus(order.getPaymentStatus().name());
        orderDTO.setOrderStatus(order.getOrderStatus().name());
        orderDTO.setTotalAmount(order.getTotalAmount());
        // Fetch payment method from Payment entity
        Payment payment = paymentRepository.findByOrderId(order.getId());
        if (payment != null) {
            orderDTO.setPaymentMethod(payment.getPaymentMethod().name());
        }
        orderDTO.setOrderItems(order.getOrderItems().stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList()));
        return orderDTO;
    }

    // Chuyển đổi OrderItem sang OrderItemDTO
    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setId(orderItem.getId());
        orderItemDTO.setProductId(orderItem.getProduct().getId());
        orderItemDTO.setProductName(orderItem.getProduct().getName());
        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setProductImage(orderItem.getProduct().getImg());
        orderItemDTO.setUnitPrice(orderItem.getUnitPrice());
        orderItemDTO.setSubtotal(orderItem.getSubtotal());
        return orderItemDTO;
    }
}
