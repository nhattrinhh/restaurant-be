package com.web.web.Dto;

import lombok.Data;
import java.util.Date;

@Data
public class IngredientResponse {
    private Long id;
    private String name;
    private String unit;
    private double stockQuantity;
    private double minThreshold;
    private boolean isFresh;
    private boolean isActive;
    private boolean belowThreshold;
    private Date createdAt;
    private Date updatedAt;
}
