package com.web.web.Controller;

import com.web.web.Dto.TableOrderDto;
import com.web.web.Entity.TableOrder;
import com.web.web.Service.TableOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/table-orders")
@RequiredArgsConstructor
public class TableOrderController {

    private final TableOrderService service;

    /** GET active order for a table — returns 204 if none */
    @GetMapping("/{tableId}")
    public ResponseEntity<?> get(@PathVariable Long tableId) {
        return service.getOrder(tableId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /** PUT (upsert) active order for a table */
    @PutMapping("/{tableId}")
    public ResponseEntity<TableOrder> save(
            @PathVariable Long tableId,
            @RequestBody TableOrderDto dto) {
        return ResponseEntity.ok(service.saveOrder(tableId, dto));
    }

    /** DELETE active order (on checkout or cancel) */
    @DeleteMapping("/{tableId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long tableId) {
        service.deleteOrder(tableId);
        return ResponseEntity.ok(Map.of("message", "Order cleared"));
    }
}
