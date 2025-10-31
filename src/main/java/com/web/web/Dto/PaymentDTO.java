package com.web.web.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentDTO {
    private Long id;

    private Long orderId;

    @NotBlank(message = "Hình thức thanh toán không được để trống")
    private String paymentMethod;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}