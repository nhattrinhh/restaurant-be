package com.web.web.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.web.Dto.RecipeHistoryResponse;
import com.web.web.Dto.RecipeResponse;
import com.web.web.Dto.RestoreRecipeRequest;
import com.web.web.Dto.SetRecipeRequest;
import com.web.web.Entity.Ingredient;
import com.web.web.Entity.Product;
import com.web.web.Entity.ProductIngredient;
import com.web.web.Entity.RecipeHistory;
import com.web.web.Entity.StockTransaction;
import com.web.web.Entity.TableOrderItem;
import com.web.web.Entity.User;
import com.web.web.Exception.DataNotFoundException;
import com.web.web.Repository.IngredientRepository;
import com.web.web.Repository.ProductIngredientRepository;
import com.web.web.Repository.ProductRepository;
import com.web.web.Repository.RecipeHistoryRepository;
import com.web.web.Repository.StockTransactionRepository;
import com.web.web.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    @Autowired
    private ProductIngredientRepository productIngredientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeHistoryRepository recipeHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public RecipeResponse getRecipe(Long productId) throws DataNotFoundException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        List<ProductIngredient> items = productIngredientRepository.findByProductId(productId);
        
        RecipeResponse response = new RecipeResponse();
        response.setProductId(productId);
        
        List<RecipeResponse.RecipeItemResponse> itemResponses = new ArrayList<>();
        for (ProductIngredient pi : items) {
            RecipeResponse.RecipeItemResponse r = new RecipeResponse.RecipeItemResponse();
            r.setIngredientId(pi.getIngredient().getId());
            r.setIngredientName(pi.getIngredient().getName());
            r.setUnit(pi.getIngredient().getUnit());
            r.setQuantityPerServing(pi.getQuantityPerServing());
            itemResponses.add(r);
        }
        response.setItems(itemResponses);
        return response;
    }

    @Transactional
    public RecipeResponse setRecipe(Long productId, SetRecipeRequest request, Long adminId) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new DataNotFoundException("Admin not found"));

        productIngredientRepository.deleteByProductId(productId);

        List<ProductIngredient> savedItems = new ArrayList<>();
        
        List<Map<String, Object>> snapshotItems = new ArrayList<>();

        for (SetRecipeRequest.RecipeItem item : request.getItems()) {
            Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                    .orElseThrow(() -> new DataNotFoundException("Ingredient not found"));

            if (!ingredient.isActive()) {
                throw new IllegalArgumentException("Cannot use inactive ingredient: " + ingredient.getName());
            }

            ProductIngredient pi = new ProductIngredient();
            pi.setProduct(product);
            pi.setIngredient(ingredient);
            pi.setQuantityPerServing(item.getQuantityPerServing());
            savedItems.add(productIngredientRepository.save(pi));

            // Prepare snapshot
            Map<String, Object> map = new HashMap<>();
            map.put("ingredientId", ingredient.getId());
            map.put("ingredientName", ingredient.getName());
            map.put("unit", ingredient.getUnit());
            map.put("quantityPerServing", item.getQuantityPerServing());
            snapshotItems.add(map);
        }

        // Get max version
        List<RecipeHistory> history = recipeHistoryRepository.findByProductIdOrderByVersionDesc(productId);
        int nextVersion = history.isEmpty() ? 1 : history.get(0).getVersion() + 1;

        Map<String, Object> snapshotObj = new HashMap<>();
        snapshotObj.put("version", nextVersion);
        snapshotObj.put("items", snapshotItems);
        String snapshotJson = objectMapper.writeValueAsString(snapshotObj);

        RecipeHistory rh = new RecipeHistory();
        rh.setProduct(product);
        rh.setVersion(nextVersion);
        rh.setSnapshot(snapshotJson);
        rh.setChangedBy(admin);
        rh.setChangeNote(request.getChangeNote());
        rh.setRestored(false);
        recipeHistoryRepository.save(rh);

        checkAvailability(productId);

        return getRecipe(productId);
    }

    @Transactional(readOnly = true)
    public List<RecipeHistoryResponse> getHistory(Long productId) {
        return recipeHistoryRepository.findByProductIdOrderByVersionDesc(productId).stream()
                .map(this::mapHistoryToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RecipeResponse restoreRecipe(Long productId, RestoreRecipeRequest request, Long adminId) throws Exception {
        RecipeHistory oldHistory = recipeHistoryRepository.findById(request.getHistoryId())
                .orElseThrow(() -> new DataNotFoundException("History not found"));
        
        // This is a naive implementation. In a real app we parse the snapshot JSON and call setRecipe.
        // Since we are mocking here, we do exactly that.
        Map<String, Object> snapshot = objectMapper.readValue(oldHistory.getSnapshot(), Map.class);
        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) snapshot.get("items");

        SetRecipeRequest setRequest = new SetRecipeRequest();
        setRequest.setChangeNote(request.getChangeNote() == null ? "Restored from version " + oldHistory.getVersion() : request.getChangeNote());
        
        List<SetRecipeRequest.RecipeItem> newItems = new ArrayList<>();
        for (Map<String, Object> itemMap : itemsList) {
            SetRecipeRequest.RecipeItem ri = new SetRecipeRequest.RecipeItem();
            ri.setIngredientId(((Number) itemMap.get("ingredientId")).longValue());
            ri.setQuantityPerServing(((Number) itemMap.get("quantityPerServing")).doubleValue());
            newItems.add(ri);
        }
        setRequest.setItems(newItems);

        RecipeResponse response = setRecipe(productId, setRequest, adminId);
        
        // Mark the newly created history as a restore
        List<RecipeHistory> recentHistories = recipeHistoryRepository.findByProductIdOrderByVersionDesc(productId);
        if (!recentHistories.isEmpty()) {
            RecipeHistory latest = recentHistories.get(0);
            latest.setRestored(true);
            recipeHistoryRepository.save(latest);
        }

        return response;
    }

    @Transactional
    public void deductStock(Long productId, int multiplier, User recordedBy, TableOrderItem item, String batchInfo) {
        List<ProductIngredient> ingredients = productIngredientRepository.findByProductId(productId);
        if (ingredients.isEmpty()) return; // No recipe

        for (ProductIngredient pi : ingredients) {
            double deductQuantity = pi.getQuantityPerServing() * multiplier;
            Ingredient ing = pi.getIngredient();
            ing.setStockQuantity(ing.getStockQuantity() - deductQuantity);
            ingredientRepository.save(ing);

            StockTransaction tx = new StockTransaction();
            tx.setIngredient(ing);
            tx.setType("OUT");
            tx.setQuantity(deductQuantity);
            tx.setNote(batchInfo);
            tx.setRecordedBy(recordedBy);
            tx.setRefItem(item);
            stockTransactionRepository.save(tx);
        }
        
        checkAvailability(productId);
        
        // We also need to check availability for any other product that uses these ingredients
        for (ProductIngredient pi : ingredients) {
            List<ProductIngredient> otherProducts = productIngredientRepository.findAll().stream()
                .filter(otherPi -> otherPi.getIngredient().getId().equals(pi.getIngredient().getId()))
                .collect(Collectors.toList());
            for (ProductIngredient otherPi : otherProducts) {
                if (!otherPi.getProduct().getId().equals(productId)) {
                    checkAvailability(otherPi.getProduct().getId());
                }
            }
        }
    }

    @Transactional
    public void checkAvailability(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return;

        List<ProductIngredient> ingredients = productIngredientRepository.findByProductId(productId);
        if (ingredients.isEmpty()) {
            product.setAvailable(true);
            productRepository.save(product);
            return;
        }

        boolean canMake = true;
        for (ProductIngredient pi : ingredients) {
            Ingredient ing = pi.getIngredient();
            if (!ing.isActive() || ing.getStockQuantity() < pi.getQuantityPerServing()) {
                canMake = false;
                break;
            }
        }

        product.setAvailable(canMake);
        productRepository.save(product);
    }

    private RecipeHistoryResponse mapHistoryToResponse(RecipeHistory history) {
        RecipeHistoryResponse response = new RecipeHistoryResponse();
        response.setId(history.getId());
        response.setProductId(history.getProduct().getId());
        response.setVersion(history.getVersion());
        response.setSnapshot(history.getSnapshot());
        response.setChangedById(history.getChangedBy().getId());
        response.setChangedByName(history.getChangedBy().getUsername());
        response.setChangeNote(history.getChangeNote());
        response.setRestored(history.isRestored());
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }
}
