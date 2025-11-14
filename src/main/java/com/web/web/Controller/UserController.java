package com.web.web.Controller;

import com.web.web.Dto.UserDTO;
import com.web.web.Entity.Role;
import com.web.web.Entity.User;
import com.web.web.Service.UserService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1a. Lấy profile người dùng hiện tại
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDTO(user));
    }

    // 1b. Cập nhật profile người dùng hiện tại (dành cho người dùng thường)
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDTO dto) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        String username = userDetails.getUsername();
        if (!username.equals(dto.getUsername())) {
            return ResponseEntity.status(403).build();
        }

        // Gọi service update nhưng đảm bảo chỉ sửa thông tin cơ bản, không sửa roles
        // hoặc enabled
        User updatedUser = userService.updateOwnProfile(username, dto);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDTO(updatedUser));
    }

    // 1c. Cập nhật profile admin
    @PutMapping("/admin/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateAdminProfile(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDTO dto) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        String username = userDetails.getUsername();
        if (!username.equals(dto.getUsername())) {
            return ResponseEntity.status(403).build();
        }

        // Gọi service update nhưng đảm bảo chỉ sửa thông tin cơ bản, không sửa roles
        // hoặc enabled
        User updatedUser = userService.updateOwnProfile(username, dto);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDTO(updatedUser));
    }

    // 2. Admin lấy danh sách người dùng thường
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers().stream()
                .filter(user -> user.getRoles().stream()
                        .noneMatch(role -> role.getName().equalsIgnoreCase("ADMIN")))
                .collect(Collectors.toList());

        List<UserDTO> dtos = users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 3. Admin xem chi tiết 1 user
    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDTO(user));
    }

    // 4. Admin tạo user mới
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.ok(toDTO(user));
    }

    // 5. Admin cập nhật user bất kỳ
    @PutMapping("/update/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String username, @RequestBody UserDTO dto) {
        User updatedUser = userService.updateUser(username, dto);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDTO(updatedUser));
    }

    // 6. Admin xóa user
    @DeleteMapping("/delete/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        boolean deleted = userService.deleteByUsername(username);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    // Helper chuyển từ Entity -> DTO
    private UserDTO toDTO(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullname(),
                user.getAddress(),
                user.getPhoneNumber(),
                user.isEnabled(),
                roles);
    }
}