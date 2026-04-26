package com.web.web.Dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response trả về FE sau khi init SePay checkout.
 *
 * - Nếu BE gọi API SePay thành công → `checkoutUrl` là URL có token để FE
 * redirect thẳng (window.location.href = checkoutUrl).
 * - Các field `paymentUrl` + `formFields` giữ lại cho backward compat
 * (FE cũ dùng form POST) — có thể null khi flow mới.
 */
public class SePayCheckoutResponse {

    /**
     * URL đầy đủ để FE redirect user tới trang thanh toán SePay.
     * Ví dụ: https://pay-sandbox.sepay.vn/pay/xxxxxxxx
     */
    private String checkoutUrl;

    /** [Deprecated] URL endpoint + form fields cho form-POST flow */
    private String paymentUrl;
    private Map<String, String> formFields = new LinkedHashMap<>();

    /** Order ID vừa tạo */
    private Long orderId;

    /** Tổng tiền thanh toán */
    private Double amount;

    /** Response thô từ SePay (debug) — có thể null */
    private Object rawResponse;

    public SePayCheckoutResponse() {
    }

    public SePayCheckoutResponse(String checkoutUrl, Long orderId, Double amount) {
        this.checkoutUrl = checkoutUrl;
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public Map<String, String> getFormFields() {
        return formFields;
    }

    public void setFormFields(Map<String, String> formFields) {
        this.formFields = formFields;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Object getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(Object rawResponse) {
        this.rawResponse = rawResponse;
    }
}
