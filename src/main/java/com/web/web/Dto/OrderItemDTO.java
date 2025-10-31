package com.web.web.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class OrderItemDTO {
    private Long id;

    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    private String productName;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;

    private String productImage;

    @Min(value = 0, message = "Giá đơn vị phải lớn hơn hoặc bằng 0")
    private double unitPrice;

    @Min(value = 0, message = "Tổng phụ phải lớn hơn hoặc bằng 0")
    private double subtotal;

    private int totalOrdered;

    public int getTotalOrdered() {
        return totalOrdered;
    }

    public void setTotalOrdered(int totalOrdered) {
        this.totalOrdered = totalOrdered;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
