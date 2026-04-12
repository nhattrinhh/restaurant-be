package com.web.web.Controller;

import com.web.web.Dto.CreateIngredientRequest;
import com.web.web.Dto.IngredientResponse;
import com.web.web.Entity.Role;
import com.web.web.Entity.User;
import com.web.web.Exception.DataNotFoundException;
import com.web.web.Service.IngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
public class IngredientController {

    @Autowired
    private IngredientService ingredientService;

    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getAllIngredients() {
        return ResponseEntity.ok(ingredientService.getAllIngredients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponse> getIngredient(@PathVariable Long id) throws DataNotFoundException {
        return ResponseEntity.ok(ingredientService.getIngredientById(id));
    }

    @PostMapping
    public ResponseEntity<IngredientResponse> createIngredient(
            @RequestBody CreateIngredientRequest request,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        // user.getId() from principal
        return ResponseEntity.ok(ingredientService.createIngredient(request, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientResponse> updateIngredient(
            @PathVariable Long id,
            @RequestBody CreateIngredientRequest request) throws DataNotFoundException {
        return ResponseEntity.ok(ingredientService.updateIngredient(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) throws DataNotFoundException {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }
}
