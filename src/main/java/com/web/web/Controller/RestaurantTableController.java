package com.web.web.Controller;

import com.web.web.Dto.RestaurantTableDTO;
import com.web.web.Service.RestaurantTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
public class RestaurantTableController {

    private final RestaurantTableService tableService;

    @Autowired
    public RestaurantTableController(RestaurantTableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public ResponseEntity<?> getAllTables() {
        try {
            List<RestaurantTableDTO> tables = tableService.getAllTables();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy danh sách bàn thành công");
            response.put("tables", tables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    @GetMapping("/by-area/{areaId}")
    public ResponseEntity<?> getTablesByArea(@PathVariable Long areaId) {
        try {
            List<RestaurantTableDTO> tables = tableService.getTablesByArea(areaId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy danh sách bàn theo khu vực thành công");
            response.put("tables", tables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTableById(@PathVariable Long id) {
        try {
            RestaurantTableDTO table = tableService.getTableById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy bàn thành công");
            response.put("table", table);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createTable(@RequestBody RestaurantTableDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty() || dto.getAreaId() == null) {
            return ResponseEntity.badRequest().body("Tên bàn và khu vực không được để trống");
        }
        try {
            RestaurantTableDTO created = tableService.createTable(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tạo bàn thành công");
            response.put("table", created);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTable(@PathVariable Long id, @RequestBody RestaurantTableDTO dto) {
        try {
            RestaurantTableDTO updated = tableService.updateTable(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cập nhật bàn thành công");
            response.put("table", updated);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTableStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Trạng thái không được để trống");
        }
        try {
            RestaurantTableDTO updated = tableService.updateTableStatus(id, status);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cập nhật trạng thái bàn thành công");
            response.put("table", updated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTable(@PathVariable Long id) {
        try {
            tableService.deleteTable(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }
}
