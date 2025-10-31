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
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setCartItems(cart.getCartItems() != null
                ? cart.getCartItems().stream().map(this::toCartItemDTO).collect(Collectors.toList())
                : new ArrayList<>());
        dto.setTotalPrice(calculateTotalPrice(cart));
        return dto;
    }

    private CartItemDTO toCartItemDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setQuantity(cartItem.getQuantity());
        dto.setProductImage(cartItem.getProduct().getImg());
        double price = cartItem.getProduct().getDiscountedPrice() > 0 ?
                cartItem.getProduct().getDiscountedPrice() :
                cartItem.getProduct().getOriginalPrice();
        dto.setPrice(price);
        dto.setSubtotal(cartItem.getSubtotal());
        return dto;
    }

    private double calculateTotalPrice(Cart cart) {
        return cart.getCartItems() != null
                ? cart.getCartItems().stream().mapToDouble(CartItem::getSubtotal).sum()
                : 0.0;
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
            double price = cartItem.getProduct().getDiscountedPrice() > 0 ?
                    cartItem.getProduct().getDiscountedPrice() :
                    cartItem.getProduct().getOriginalPrice();
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại với ID: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Giỏ hàng không tồn tại"));

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