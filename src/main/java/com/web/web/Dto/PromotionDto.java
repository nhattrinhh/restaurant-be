package com.web.web.Dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionDto {
    private Long id;

    private String name;
    private String description;
    private String type; // "PERCENT" | "FIXED"
    private BigDecimal value;
    private BigDecimal minOrderValue;

    private String startDate; // ISO string, e.g. "2025-01-01T00:00:00"
    private String endDate;
    private String status; // "ACTIVE" | "PAUSED" | "EXPIRED"
    private String createdAt;
    private String updatedAt;
}
