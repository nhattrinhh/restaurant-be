package com.web.web.Dto;

import lombok.Data;
import java.util.Date;

@Data
public class WasteRequest {
    private Long ingredientId;
    private double quantity;
    private String note;
    private Date createdAt;
}
