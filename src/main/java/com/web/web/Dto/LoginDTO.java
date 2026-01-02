package com.web.web.Dto;


import jakarta.validation.constraints.*;

public class LoginDTO {
    @NotNull(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 40, message = "Tên đăng nhập phải từ 3 đến 40 ký tự")
    private String username;

    @NotNull(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 15, message = "Mật khẩu phải từ 6 đến 15 ký tự")
    private String password;

    public LoginDTO() {}

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
