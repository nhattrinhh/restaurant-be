package com.web.web.Dto;

import lombok.Data;

@Data
public class RestoreRecipeRequest {
    private Long historyId;
    private String changeNote;
}
