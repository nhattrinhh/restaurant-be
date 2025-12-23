package com.web.web.Security;

import com.web.web.Config.JwtUtil;
import com.web.web.Entity.Role;
import com.web.web.Entity.User;
import com.web.web.Repository.RoleRepository;
import com.web.web.Repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken; // Import cái này
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // ... (Giữ nguyên các dependency injection) ...
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public OAuth2SuccessHandler(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CÁCH LẤY PROVIDER CHUẨN: Ép kiểu sang OAuth2AuthenticationToken
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        String email = null;
        String name = null;
        String providerId = null;

        if (registrationId.equals("google")) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");

        } else if (registrationId.equals("facebook")) {
            providerId = oAuth2User.getAttribute("id"); // FACEBOOK LUÔN CÓ ID
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");

            // Facebook có thể KHÔNG trả email → fake email
            if (email == null || email.isEmpty()) {
                email = providerId + "@facebook.local";
            }

        } else if (registrationId.equals("github")) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");

            if (name == null) {
                name = oAuth2User.getAttribute("login");
            }

            if (email == null || email.isEmpty()) {
                String githubId = oAuth2User.getAttribute("id").toString();
                email = githubId + "@github.local";
            }
        }

        // --- QUAN TRỌNG: Xử lý trường hợp Facebook không trả về Email ---
        // Facebook đôi khi không trả về email nếu user đăng ký bằng sđt
        // hoặc App chưa xin quyền 'email' trong Meta Developer Console.
        if (email == null) {
            // Fallback: Tạo email giả hoặc báo lỗi (Ở đây tôi tạo email giả từ ID)
            String id = oAuth2User.getAttribute("id").toString();
            email = id + "@facebook.com";
            if (name == null)
                name = "Facebook User";
        }

        // ... (Đoạn dưới giữ nguyên logic tạo user/token ) ...
        // Copy lại đoạn logic tạo User và Redirect ở code cũ của cậu vào đây

        // ... (Code cũ từ dòng "Tạo username từ email..." trở xuống) ...

        // Logic tìm user cũ hoặc tạo mới
        User user = userRepository.findByEmail(email);
        if (user == null) {
            // ... (Logic tạo user mới của cậu) ...
            // Nhớ copy phần tạo user vào nhé
            String username = email.split("@")[0];
            String randomPassword = "OAUTH2_" + System.currentTimeMillis();

            // Xử lý trùng username
            String baseUsername = username;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter++;
            }

            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(randomPassword));
            user.setFullname(name != null ? name : username);
            user.setAddress("Chưa cập nhật");
            user.setPhoneNumber("Chưa cập nhật");
            user.setEnabled(true);

            Role userRole = roleRepository.findByName("USER");
            // ... (xử lý role) ...
            if (userRole == null) {
                /* ... */ } // Nhớ handle null role cẩn thận
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);

            user = userRepository.save(user);
        }

        // Tạo JWT
        Set<String> roleNames = new HashSet<>();
        user.getRoles().forEach(r -> roleNames.add(r.getName()));
        String token = jwtUtil.generateToken(user.getUsername(), roleNames); // Cần đảm bảo hàm này khớp tham số

        String redirectUrl = frontendUrl + "/oauth2/callback?token=" + token + "&username=" + user.getUsername();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}