package com.web.web.Dto;

import lombok.Data;
import java.util.List;

@Data
public class RecipeResponse {
    private Long productId;
    private List<RecipeItemResponse> items;

    @Data
    public static class RecipeItemResponse {
        private Long ingredientId;
        private String ingredientName;
        private String unit;
        private double quantityPerServing;
    }
}
