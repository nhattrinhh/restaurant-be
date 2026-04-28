package com.web.web.Dto;

/**
 * Request body khi FE gọi POST /api/payment/sepay/init
 *
 * Các field trùng với createOrder hiện tại:
 * - tableId: bàn đã chọn
 * - specialRequests: ghi chú
 * - paymentMethod: luôn là "ONLINE_PAYMENT" khi đi qua SePay
 *
 * Nếu là "Đặt ngay" (orderNow) thì FE gửi thêm productId + quantity.
 */
public class SePayCheckoutRequest {

    private Long tableId;
    private String specialRequests;
    private String paymentMethod = "ONLINE_PAYMENT";

    // optional — dùng cho luồng "Đặt ngay" (không qua giỏ hàng)
    private Long productId;
    private Integer quantity;

    private Long bookingId;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
