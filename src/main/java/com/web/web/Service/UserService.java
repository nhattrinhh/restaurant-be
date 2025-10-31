package com.web.web.Service;

import com.web.web.Dto.UserDTO;
import com.web.web.Entity.Role;
import com.web.web.Entity.User;
import com.web.web.Repository.RoleRepository;
import com.web.web.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Đăng ký người dùng mới
    @Transactional
    public User registerUser(String username, String password, String email, String fullname,
                             String address, String phoneNumber, String... roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFullname(fullname);
        user.setAddress(address);
        user.setPhoneNumber(phoneNumber);
        user.setEnabled(true);
        user.setRoles(getOrCreateRoles(roleNames));

        return userRepository.save(user);
    }

    // 2. Lấy tất cả user
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 3. Lấy user theo ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 4. Xóa user theo ID
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // 5. Xóa user theo username
    @Transactional
    public boolean deleteByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return false;
        userRepository.delete(user);
        return true;
    }

    // 6. Tìm user theo username
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 7. Tạo user từ DTO (Admin)
    @Transactional
    public User createUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullname(dto.getFullname());
        user.setAddress(dto.getAddress());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEnabled(dto.isEnabled());
        user.setRoles(getOrCreateRoles(dto.getRoles().toArray(new String[0])));

        return userRepository.save(user);
    }

    // 8. Admin cập nhật user bất kỳ
    @Transactional
    public User updateUser(String username, UserDTO dto) {
        User user = userRepository.findByUsername(username);
        if (user == null) return null;

        if (!dto.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        user.setEmail(dto.getEmail());
        user.setFullname(dto.getFullname());
        user.setAddress(dto.getAddress());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEnabled(dto.isEnabled());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        user.setRoles(getOrCreateRoles(dto.getRoles().toArray(new String[0])));
        return userRepository.save(user);
    }

    // 9. User tự cập nhật profile (không được đổi password, role, enabled)
    @Transactional
    public User updateOwnProfile(String username, UserDTO dto) {
        User user = userRepository.findByUsername(username);
        if (user == null) return null;

        // Kiểm tra email có bị trùng không
        if (!dto.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        user.setEmail(dto.getEmail());
        user.setFullname(dto.getFullname());
        user.setAddress(dto.getAddress());
        user.setPhoneNumber(dto.getPhoneNumber());

        return userRepository.save(user);
    }

    // Helper: Tạo role nếu chưa tồn tại
    private Set<Role> getOrCreateRoles(String... roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName);
            if (role == null) {
                role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
            roles.add(role);
        }
        return roles;
    }
}
