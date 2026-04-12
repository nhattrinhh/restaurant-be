package com.web.web.Dto;

import lombok.Data;
import java.util.Date;

@Data
public class CookingInstructionResponse {
    private Long id;
    private Long productId;
    private String content;
    private Long updatedById;
    private String updatedByName;
    private Date updatedAt;
}
