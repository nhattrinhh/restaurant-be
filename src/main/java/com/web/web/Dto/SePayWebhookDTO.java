package com.web.web.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SePayWebhookDTO {
    private Long id;
    private String gateway;

    @JsonProperty("transaction_date")
    private String transactionDate;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("transfer_type")
    private String transferType;

    @JsonProperty("transfer_amount")
    private Double transferAmount;

    private String content;

    @JsonProperty("reference_number")
    private String referenceNumber;
}
