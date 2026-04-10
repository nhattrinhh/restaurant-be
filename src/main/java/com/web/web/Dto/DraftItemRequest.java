package com.web.web.Dto;

import lombok.Data;

@Data
public class DraftItemRequest {
    private Long productId;
    private int quantity;
    private String note;
}
