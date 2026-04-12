package com.web.web.Dto;

import lombok.Data;
import java.util.Date;

@Data
public class StockTransactionResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private String type;
    private double quantity;
    private String note;
    private Long recordedById;
    private String recordedByName;
    private Long refItemId;
    private boolean isDeleted;
    private Date createdAt;
}
