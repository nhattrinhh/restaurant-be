package com.web.web.Dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TableOrderItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private String note;
    private int batchNumber;
    private String status; // PENDING, COOKING, DONE, CANCELLED
    private LocalDateTime createdAt;
}
