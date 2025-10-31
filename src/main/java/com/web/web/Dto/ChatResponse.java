package com.web.web.Dto;

import java.util.List;

public class ChatResponse {
    private String reply;
    private String error;
    private String message;
    private List<ProductDTO> products;

    public ChatResponse() {
    }

    public ChatResponse(String reply, String message, List<ProductDTO> products) {
        this.reply = reply;
        this.message = message;
        this.products = products;
    }

    public ChatResponse(String error) {
        this.error = error;
    }

    // Getter và Setter
    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ProductDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDTO> products) {
        this.products = products;
    }
}
