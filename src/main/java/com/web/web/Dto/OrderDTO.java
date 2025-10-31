package com.web.web.Dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
@Data
public class OrderDTO {
    @Positive(message = "ID phải là số dương")
    private Long id;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z\\s\\-']+$", message = "Họ tên chỉ được chứa chữ cái, khoảng trắng, dấu gạch ngang hoặc dấu nháy đơn")
    private String fullname;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 15, message = "Số điện thoại không được vượt quá 15 ký tự")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,12}$|^$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 255, message = "Địa chỉ giao hàng không được vượt quá 255 ký tự")
    private String deliveryAddress;

    @NotNull(message = "Ngày lập đơn hàng không được để trống")
    @PastOrPresent(message = "Ngày lập đơn hàng phải là hiện tại hoặc trong quá khứ")
    private LocalDateTime orderDate;


    private LocalDateTime deliveryDate;

    @NotBlank(message = "Trạng thái thanh toán không được để trống")
    @Pattern(regexp = "^(PENDING|PAID|FAILED|REFUNDED)$",
            message = "Trạng thái thanh toán phải là PENDING, PAID, FAILED hoặc REFUNDED")
    private String paymentStatus;


    @Pattern(regexp = "^(PENDING|CONFIRMED|SHIPPING|DELIVERED|CANCELLED|CANCEL_REQUESTED)$",
            message = "Trạng thái đơn hàng phải là PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED hoặc CANCEL_REQUESTED")
    private String orderStatus;

    @NotBlank(message = "Hình thức thanh toán không được để trống")

    private String paymentMethod;

    @Min(value = 0, message = "Tổng giá phải lớn hơn hoặc bằng 0")
    @Max(value = 1000000000, message = "Tổng giá không được vượt quá 1,000,000,000")
    private double totalAmount;

    @NotEmpty(message = "Danh sách mặt hàng không được để trống")
    private List<@Valid OrderItemDTO> orderItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }
}
