package com.web.web.Controller;

import com.web.web.Dto.*;
import com.web.web.Entity.TableOrder;
import com.web.web.Service.KitchenSseService;
import com.web.web.Service.TableOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/table-orders")
@RequiredArgsConstructor
public class TableOrderController {

    private final TableOrderService service;
    private final KitchenSseService kitchenSseService;

    // ─────────────────────────── EXISTING ENDPOINTS (backward compat) ──

    /** GET active order for a table — returns 204 if none */
    @GetMapping("/{tableId}")
    public ResponseEntity<?> get(@PathVariable Long tableId) {
        try {
            KitchenOrderResponse response = service.getCurrentOrder(tableId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.noContent().build();
        }
    }

    /** PUT — update order metadata (discount, surcharge, etc.) */
    @PutMapping("/{tableId}")
    public ResponseEntity<KitchenOrderResponse> saveMeta(
            @PathVariable Long tableId,
            @RequestBody TableOrderDto dto) {
        return ResponseEntity.ok(service.saveOrderMeta(tableId, dto));
    }

    /** DELETE active order (on cancel) */
    @DeleteMapping("/{tableId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long tableId) {
        service.deleteOrder(tableId);
        return ResponseEntity.ok(Map.of("message", "Order cleared"));
    }

    // ─────────────────────────── NEW ENDPOINTS (KDS feature) ──

    /** POST — Send items to kitchen (creates new batch with PENDING status) */
    @PostMapping("/send-kitchen")
    public ResponseEntity<KitchenOrderResponse> sendToKitchen(
            @RequestBody SendToKitchenRequest request) {
        KitchenOrderResponse response = service.sendToKitchen(request);
        return ResponseEntity.ok(response);
    }

    /** PATCH — Kitchen staff updates item status (PENDING → COOKING → DONE) */
    @PatchMapping("/items/{itemId}/status")
    public ResponseEntity<TableOrderItemDto> updateItemStatus(
            @PathVariable Long itemId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        TableOrderItemDto updated = service.updateItemStatus(itemId, status);
        return ResponseEntity.ok(updated);
    }

    /** GET — Get full order with all items for a table (for checkout / POS) */
    @GetMapping("/{tableId}/current")
    public ResponseEntity<KitchenOrderResponse> getCurrentOrder(@PathVariable Long tableId) {
        try {
            KitchenOrderResponse response = service.getCurrentOrder(tableId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.noContent().build();
        }
    }

    /** GET — Get all active orders (for KDS initial load) */
    @GetMapping("/kitchen/active")
    public ResponseEntity<List<KitchenOrderResponse>> getActiveOrders() {
        return ResponseEntity.ok(service.getAllActiveOrders());
    }

    /** GET (SSE) — Real-time stream for KDS */
    @GetMapping(value = "/kitchen/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamKitchenOrders() {
        return kitchenSseService.addEmitter();
    }

    /** POST — Close order after payment */
    @PostMapping("/{tableId}/close")
    public ResponseEntity<Map<String, String>> closeOrder(@PathVariable Long tableId) {
        service.closeOrder(tableId);
        return ResponseEntity.ok(Map.of("message", "Order closed, table reset"));
    }
}
