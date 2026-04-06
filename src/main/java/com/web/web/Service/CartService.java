package com.web.web.Service;

import com.web.web.Dto.CartDTO;
import com.web.web.Dto.CartItemDTO;
import com.web.web.Entity.Cart;
import com.web.web.Entity.CartItem;
import com.web.web.Entity.Product;
import com.web.web.Entity.User;
import com.web.web.Repository.CartItemRepository;
import com.web.web.Repository.CartRepository;
import com.web.web.Repository.ProductRepository;
import com.web.web.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private CartDTO toDTO(Cart cart) {
        try {
            CartDTO dto = new CartDTO();
            dto.setId(cart.getId());
            dto.setUserId(cart.getUser().getId());

            // Kiểm tra cartItems null hoặc empty
            List<CartItemDTO> cartItemDTOs = new ArrayList<>();
            if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                cartItemDTOs = cart.getCartItems().stream()
                        .filter(item -> item != null)
                        .map(this::toCartItemDTO)
                        .collect(Collectors.toList());
            }
            dto.setCartItems(cartItemDTOs);
            dto.setTotalPrice(calculateTotalPrice(cart));
            return dto;
        } catch (Exception e) {
            logger.error("Lỗi khi convert Cart sang DTO: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi xử lý giỏ hàng: " + e.getMessage(), e);
        }
    }

    private CartItemDTO toCartItemDTO(CartItem cartItem) {
        try {
            CartItemDTO dto = new CartItemDTO();
            dto.setId(cartItem.getId());
            dto.setProductId(cartItem.getProduct().getId());
            dto.setProductName(cartItem.getProduct().getName());
            dto.setQuantity(cartItem.getQuantity());
            dto.setProductImage(cartItem.getProduct().getImg());
            double price = cartItem.getProduct().getDiscountedPrice() > 0 ? cartItem.getProduct().getDiscountedPrice()
                    : cartItem.getProduct().getOriginalPrice();
            dto.setPrice(price);
            dto.setSubtotal(cartItem.getSubtotal());
            return dto;
        } catch (Exception e) {
            logger.error("Lỗi khi convert CartItem sang DTO cho product ID {}: {}",
                    cartItem.getProduct().getId(), e.getMessage(), e);
            throw new RuntimeException("Lỗi xử lý item trong giỏ hàng", e);
        }
    }

    private double calculateTotalPrice(Cart cart) {
        try {
            if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                return 0.0;
            }
            return cart.getCartItems().stream()
                    .filter(item -> item != null)
                    .mapToDouble(CartItem::getSubtotal)
                    .sum();
        } catch (Exception e) {
            logger.error("Lỗi khi tính tổng giá: {}", e.getMessage(), e);
            return 0.0;
        }
    }

    @Transactional
    public CartDTO addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại với ID: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại với ID: " + productId));

        if (!"AVAILABLE".equals(product.getStatus())) {
            throw new IllegalArgumentException("Sản phẩm không khả dụng");
        }

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElse(null);

        double price = product.getDiscountedPrice() > 0 ? product.getDiscountedPrice() : product.getOriginalPrice();

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setSubtotal(cartItem.getQuantity() * price);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setSubtotal(quantity * price);
            cart.getCartItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
        return toDTO(cart);
    }

    @Transactional
    public CartDTO updateCartItemQuantity(Long userId, Long productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại với ID: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Giỏ hàng không tồn tại"));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không có trong giỏ hàng"));

        if (quantity == 0) {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            double price = cartItem.getProduct().getDiscountedPrice() > 0 ? cartItem.getProduct().getDiscountedPrice()
                    : cartItem.getProduct().getOriginalPrice();
            cartItem.setQuantity(quantity);
            cartItem.setSubtotal(quantity * price);
            cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart);
        return toDTO(cart);
    }

    @Transactional
    public CartDTO removeFromCart(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại với ID: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Giỏ hàng không tồn tại"));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không có trong giỏ hàng"));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);
        return toDTO(cart);
    }

    @Transactional(readOnly = true)
    public CartDTO getCart(Long userId) {
        logger.info("CartService.getCart() called for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại với ID: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    logger.info("Giỏ hàng không tồn tại, tạo mới cho user {}", userId);
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    // Khởi tạo cartItems để tránh NullPointerException
                    newCart.setCartItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        logger.info("Cart found, converting to DTO. CartItems count: {}",
                cart.getCartItems() != null ? cart.getCartItems().size() : 0);

        return toDTO(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại với ID: " + userId));

        cartRepository.findByUser(user).ifPresent(cartRepository::delete);
    }

    public Cart getCartEntity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại với ID: " + userId));
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }
}