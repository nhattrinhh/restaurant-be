package com.web.web.Dto;
import jakarta.validation.constraints.NotBlank;

public class ChatRequest {
    @NotBlank(message = "Tin nhắn không được để trống")
    private String message;

    // Getter và Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
