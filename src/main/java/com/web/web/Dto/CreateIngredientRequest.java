package com.web.web.Dto;

import lombok.Data;

@Data
public class CreateIngredientRequest {
    private String name;
    private String unit;
    private double stockQuantity;
    private double minThreshold;
    private boolean isFresh;
}
