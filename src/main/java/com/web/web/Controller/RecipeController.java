package com.web.web.Controller;

import com.web.web.Dto.RecipeHistoryResponse;
import com.web.web.Dto.RecipeResponse;
import com.web.web.Dto.RestoreRecipeRequest;
import com.web.web.Dto.SetRecipeRequest;
import com.web.web.Entity.User;
import com.web.web.Exception.DataNotFoundException;
import com.web.web.Service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping
    public ResponseEntity<RecipeResponse> getRecipe(@PathVariable Long productId) throws DataNotFoundException {
        return ResponseEntity.ok(recipeService.getRecipe(productId));
    }

    @PutMapping
    public ResponseEntity<RecipeResponse> setRecipe(
            @PathVariable Long productId,
            @RequestBody SetRecipeRequest request,
            @AuthenticationPrincipal User user) throws Exception {
        return ResponseEntity.ok(recipeService.setRecipe(productId, request, user.getId()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<RecipeHistoryResponse>> getRecipeHistory(@PathVariable Long productId) {
        return ResponseEntity.ok(recipeService.getHistory(productId));
    }

    @PostMapping("/restore")
    public ResponseEntity<RecipeResponse> restoreRecipe(
            @PathVariable Long productId,
            @RequestBody RestoreRecipeRequest request,
            @AuthenticationPrincipal User user) throws Exception {
        return ResponseEntity.ok(recipeService.restoreRecipe(productId, request, user.getId()));
    }
}
