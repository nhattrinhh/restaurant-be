package com.web.web.Dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Payload IPN từ SePay gửi về POST /api/payment/sepay/ipn.
 *
 * SePay có thể gửi nhiều định dạng key khác nhau (snake_case hoặc camelCase),
 * dùng @JsonAlias để nhận được cả 2.
 */
public class SePayIpnPayload {

    @JsonAlias({ "merchant_id", "merchantId" })
    private String merchantId;

    @JsonAlias({ "order_id", "orderId" })
    private String orderId;

    @JsonAlias({ "transaction_id", "transactionId", "trans_id" })
    private String transactionId;

    private String amount;

    private String currency;

    /** SUCCESS | FAILED | CANCELLED */
    private String status;

    @JsonAlias({ "payment_method", "paymentMethod" })
    private String paymentMethod;

    /** HMAC-SHA256 signature từ SePay */
    private String signature;

    /** Mô tả / message phụ (optional) */
    private String message;

    @JsonAlias({ "transaction_date", "transactionDate" })
    private String transactionDate;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        return "SePayIpnPayload{" +
                "merchantId='" + merchantId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", signature='" + signature + '\'' +
                ", message='" + message + '\'' +
                ", transactionDate='" + transactionDate + '\'' +
                '}';
    }
}
