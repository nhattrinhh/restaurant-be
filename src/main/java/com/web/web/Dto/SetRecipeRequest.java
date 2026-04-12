package com.web.web.Dto;

import lombok.Data;
import java.util.List;

@Data
public class SetRecipeRequest {
    private List<RecipeItem> items;
    private String changeNote;

    @Data
    public static class RecipeItem {
        private Long ingredientId;
        private double quantityPerServing;
    }
}
