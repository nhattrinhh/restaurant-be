package com.web.web.Dto;

import lombok.Data;

@Data
public class StockImportRequest {
    private Long ingredientId;
    private double quantity;
    private String note;
}
