package com.web.web.Controller;

import com.web.web.Dto.CartDTO;
import com.web.web.Dto.ResponseDTO;
import com.web.web.Entity.User;
import com.web.web.Repository.UserRepository;
import com.web.web.Service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final UserRepository userRepository;

    @Autowired
    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("User not found for username: {}", username);
            throw new IllegalArgumentException("Người dùng không hợp lệ");
        }
        return user.getId();
    }

    @PostMapping("/add")
    public ResponseEntity<ResponseDTO<CartDTO>> addToCart(@RequestParam Long productId, @RequestParam int quantity) {
        try {
            Long userId = getCurrentUserId();
            logger.info("Adding product {} with quantity {} to cart for user {}", productId, quantity, userId);
            CartDTO cart = cartService.addToCart(userId, productId, quantity);
            return ResponseEntity.status(201).body(new ResponseDTO<>("Thêm sản phẩm vào giỏ hàng thành công", cart));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseDTO<CartDTO>> updateCartItemQuantity(@RequestParam Long productId, @RequestParam int quantity) {
        try {
            Long userId = getCurrentUserId();
            logger.info("Updating product {} with quantity {} in cart for user {}", productId, quantity, userId);
            CartDTO cart = cartService.updateCartItemQuantity(userId, productId, quantity);
            return ResponseEntity.ok(new ResponseDTO<>("Cập nhật số lượng sản phẩm thành công", cart));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ResponseDTO<CartDTO>> removeFromCart(@RequestParam Long productId) {
        try {
            Long userId = getCurrentUserId();
            logger.info("Removing product {} from cart for user {}", productId, userId);
            CartDTO cart = cartService.removeFromCart(userId, productId);
            return ResponseEntity.ok(new ResponseDTO<>("Xóa sản phẩm khỏi giỏ hàng thành công", cart));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<CartDTO>> getCart() {
        try {
            Long userId = getCurrentUserId();
            logger.info("Fetching cart for user {}", userId);
            CartDTO cart = cartService.getCart(userId);
            return ResponseEntity.ok(new ResponseDTO<>("Lấy giỏ hàng thành công", cart));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ResponseDTO<Object>> clearCart() {
        try {
            Long userId = getCurrentUserId();
            logger.info("Clearing cart for user {}", userId);
            cartService.clearCart(userId);
            return ResponseEntity.ok(new ResponseDTO<>("Xóa toàn bộ giỏ hàng thành công", null));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>("Lỗi: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResponseDTO<>("Lỗi server: " + e.getMessage(), null));
        }
    }
}
