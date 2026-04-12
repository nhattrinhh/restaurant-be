package com.web.web.Dto;

import lombok.Data;
import java.util.Date;

@Data
public class RecipeHistoryResponse {
    private Long id;
    private Long productId;
    private int version;
    private String snapshot;
    private Long changedById;
    private String changedByName;
    private String changeNote;
    private boolean isRestored;
    private Date createdAt;
}
