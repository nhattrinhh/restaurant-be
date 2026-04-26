package com.web.web.Config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties cho SePay Payment Gateway.
 *
 * Map từ block sepay.* trong application.yaml (biến env SEPAY_*).
 * Sandbox URL: https://pay-sandbox.sepay.vn/v1/checkout/init
 * Production URL: https://pay.sepay.vn/v1/checkout/init
 *
 * Dùng @Component để đảm bảo Spring scan ra bean + bind properties trực tiếp.
 */
@Component
@ConfigurationProperties(prefix = "sepay")
public class SePayProperties {

    /** Merchant ID được SePay cấp (khác giữa sandbox & production) */
    private String merchantId;

    /** Secret key dùng để ký HMAC-SHA256 */
    private String secretKey;

    /**
     * URL API của SePay để tạo session checkout (BE gọi với Basic Auth).
     * Sandbox: https://pgapi-sandbox.sepay.vn/v1/checkout/init
     * Production: https://pgapi.sepay.vn/v1/checkout/init
     */
    private String apiUrl = "https://pgapi-sandbox.sepay.vn/v1/checkout/init";

    /**
     * URL Checkout UI (user được redirect đến) — thường trả về từ API response.
     * Giữ để fallback/reference.
     */
    private String checkoutUrl = "https://pay-sandbox.sepay.vn";

    /** Base URL của FE để build success_url / error_url / cancel_url */
    private String returnBaseUrl = "http://localhost:5173";

    /** URL IPN (Instant Payment Notification) — phải reach được từ internet */
    private String ipnUrl = "http://localhost:8080/api/payment/sepay/ipn";

    /** Mã tiền tệ, mặc định VND */
    private String currency = "VND";

    private static final Logger log = LoggerFactory.getLogger(SePayProperties.class);

    @PostConstruct
    public void logLoaded() {
        String maskedKey = (secretKey == null || secretKey.isEmpty())
                ? "(EMPTY)"
                : secretKey.length() <= 4 ? "****"
                        : secretKey.substring(0, 2) + "****"
                                + secretKey.substring(secretKey.length() - 2);
        log.info(
                "[SePay Config] merchantId={} secretKey={} apiUrl={} checkoutUrl={} returnBaseUrl={} ipnUrl={} currency={}",
                merchantId == null || merchantId.isEmpty() ? "(EMPTY)" : merchantId,
                maskedKey, apiUrl, checkoutUrl, returnBaseUrl, ipnUrl, currency);
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getReturnBaseUrl() {
        return returnBaseUrl;
    }

    public void setReturnBaseUrl(String returnBaseUrl) {
        this.returnBaseUrl = returnBaseUrl;
    }

    public String getIpnUrl() {
        return ipnUrl;
    }

    public void setIpnUrl(String ipnUrl) {
        this.ipnUrl = ipnUrl;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
