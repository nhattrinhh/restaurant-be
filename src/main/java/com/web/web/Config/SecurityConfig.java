package com.web.web.Config;

import com.web.web.Entity.Role;
import com.web.web.Entity.User;
import com.web.web.Repository.UserRepository;
import com.web.web.Security.OAuth2SuccessHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(UserRepository userRepository, JwtUtil jwtUtil, OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username);
            if (user == null)
                throw new UsernameNotFoundException("User not found");
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                    .build();
        };
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/products", "/api/products/search").permitAll()
                        .requestMatchers("/api/product-types/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers("/api/chatbot").permitAll()
                        .requestMatchers("/api/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/profile").permitAll()
                        .requestMatchers("/api/admin/profile").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasRole("ADMIN")
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/news", "/api/news/search").permitAll()
                        .requestMatchers("/api/news/**").hasRole("ADMIN")
                        .requestMatchers("/api/booking/create", "/api/booking/history", "/api/booking/user/cancel/**",
                                "/api/booking/{id}")
                        .authenticated()
                        .requestMatchers("/api/booking/**").hasRole("ADMIN")
                        .requestMatchers("/api/orders").authenticated()
                        .requestMatchers("/api/orders/admin").hasRole("ADMIN")
                        .requestMatchers("/api/orders/{id}/status", "/api/orders/{id}/payment-status",
                                "/api/orders/{id}/approve-cancel", "/api/orders/{id}/reject-cancel",
                                "/api/orders/{id}/delete", "/api/orders/{id}/delivery-date")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/orders/{id}", "/api/orders/{id}/cancel").authenticated()
                        .requestMatchers("/api/statistics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/table-areas/**", "/api/tables/**").permitAll()
                        .requestMatchers("/api/table-areas/**", "/api/tables/**").hasRole("ADMIN")
                        .requestMatchers("/api/table-invoices/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Chưa đăng nhập hoặc token không hợp lệ\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Không có quyền truy cập\"}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();// response
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}