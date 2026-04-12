package com.web.web.Controller;

import com.web.web.Dto.StockImportRequest;
import com.web.web.Dto.StockTransactionResponse;
import com.web.web.Dto.WasteRequest;
import com.web.web.Entity.Role;
import com.web.web.Entity.User;
import com.web.web.Exception.DataNotFoundException;
import com.web.web.Service.StockTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

    @Autowired
    private StockTransactionService stockService;

    @PostMapping("/import")
    public ResponseEntity<StockTransactionResponse> recordImport(
            @RequestBody StockImportRequest request,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        return ResponseEntity.ok(stockService.recordImport(request, user.getId()));
    }

    @PostMapping("/waste")
    public ResponseEntity<StockTransactionResponse> recordWaste(
            @RequestBody WasteRequest request,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        return ResponseEntity.ok(stockService.recordWaste(request, user.getId()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<StockTransactionResponse>> getTransactions(@AuthenticationPrincipal User user) {
        // Mock role check. Will fetch true roles later.
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("BOSS"));
        return ResponseEntity.ok(stockService.getTransactions(user.getId(), isAdmin));
    }

    @PatchMapping("/transactions/{id}")
    public ResponseEntity<StockTransactionResponse> updateTransaction(
            @PathVariable Long id,
            @RequestBody WasteRequest request, // reusing request wrapper to hold quantity/note
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        return ResponseEntity.ok(stockService.updateTransaction(id, request.getQuantity(), request.getNote(), user.getId()));
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        stockService.deleteTransaction(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
