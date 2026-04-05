package com.web.web.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Áp dụng cho tất cả API
                        .allowedOrigins(
                                "http://localhost:5173",
                                "https://nhat.cloud") // Thêm origin của Front-end
                        .allowedMethods("*")// Thêm origin của Front-end
                        .allowedHeaders("*") // Các phương thức được phép "GET", "POST", "PUT", "DELETE", "OPTIONS"
                        .allowCredentials(true);// Cho phép cookie, authorization headers, v.v.
            }
        };
    }
}
