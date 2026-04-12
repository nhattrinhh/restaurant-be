package com.web.web.Service;

import com.web.web.Dto.CreateIngredientRequest;
import com.web.web.Dto.IngredientResponse;
import com.web.web.Entity.Ingredient;
import com.web.web.Entity.StockTransaction;
import com.web.web.Entity.User;
import com.web.web.Exception.DataNotFoundException;
import com.web.web.Repository.IngredientRepository;
import com.web.web.Repository.StockTransactionRepository;
import com.web.web.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IngredientService {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public IngredientResponse createIngredient(CreateIngredientRequest request, Long adminId) throws DataNotFoundException {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new DataNotFoundException("Admin not found"));

        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName());
        ingredient.setUnit(request.getUnit());
        ingredient.setStockQuantity(request.getStockQuantity());
        ingredient.setMinThreshold(request.getMinThreshold());
        ingredient.setFresh(request.isFresh());
        ingredient.setActive(true);

        ingredient = ingredientRepository.save(ingredient);

        if (request.getStockQuantity() > 0) {
            StockTransaction transaction = new StockTransaction();
            transaction.setIngredient(ingredient);
            transaction.setType("IN");
            transaction.setQuantity(request.getStockQuantity());
            transaction.setNote("Tồn kho khởi tạo");
            transaction.setRecordedBy(admin);
            stockTransactionRepository.save(transaction);
        }

        return mapToResponse(ingredient);
    }

    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public IngredientResponse getIngredientById(Long id) throws DataNotFoundException {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Ingredient not found"));
        return mapToResponse(ingredient);
    }

    @Transactional
    public IngredientResponse updateIngredient(Long id, CreateIngredientRequest request) throws DataNotFoundException {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Ingredient not found"));
        
        ingredient.setName(request.getName());
        ingredient.setUnit(request.getUnit());
        ingredient.setMinThreshold(request.getMinThreshold());
        ingredient.setFresh(request.isFresh());

        return mapToResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void deleteIngredient(Long id) throws DataNotFoundException {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Ingredient not found"));
        ingredient.setActive(false);
        ingredientRepository.save(ingredient);
    }

    private IngredientResponse mapToResponse(Ingredient ingredient) {
        IngredientResponse response = new IngredientResponse();
        response.setId(ingredient.getId());
        response.setName(ingredient.getName());
        response.setUnit(ingredient.getUnit());
        response.setStockQuantity(ingredient.getStockQuantity());
        response.setMinThreshold(ingredient.getMinThreshold());
        response.setFresh(ingredient.isFresh());
        response.setActive(ingredient.isActive());
        response.setBelowThreshold(ingredient.getStockQuantity() < ingredient.getMinThreshold());
        response.setCreatedAt(ingredient.getCreatedAt());
        response.setUpdatedAt(ingredient.getUpdatedAt());
        return response;
    }
}
