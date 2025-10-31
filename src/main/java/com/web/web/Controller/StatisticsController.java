package com.web.web.Controller;
import com.web.web.Dto.OrderItemDTO;
import com.web.web.Entity.Category;
import com.web.web.Entity.ProductType;
import com.web.web.Service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(statisticsService.getAllCategories());
    }

    @GetMapping("/product-types")
    public ResponseEntity<List<ProductType>> getAllProductTypes() {
        return ResponseEntity.ok(statisticsService.getAllProductTypes());
    }

    @GetMapping("/top-dishes")
    public ResponseEntity<List<OrderItemDTO>> getTopPopularDishes(@RequestParam(defaultValue = "4") int limit) {
        return ResponseEntity.ok(statisticsService.getTopPopularDishes(limit));
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<List<String>> getRecentActivities(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statisticsService.getRecentActivities(limit));
    }

    @GetMapping("/top-users")
    public ResponseEntity<List<Map<String, Object>>> getTopUsers(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(statisticsService.getTopUsers(limit));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getQuickSummary() {
        return ResponseEntity.ok(statisticsService.getQuickSummary());
    }
}
