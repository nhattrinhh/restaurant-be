package com.web.web.Controller;

import com.web.web.Dto.TableAreaDTO;
import com.web.web.Service.TableAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/table-areas")
public class TableAreaController {

    private final TableAreaService tableAreaService;

    @Autowired
    public TableAreaController(TableAreaService tableAreaService) {
        this.tableAreaService = tableAreaService;
    }

    @GetMapping
    public ResponseEntity<?> getAllAreas() {
        try {
            List<TableAreaDTO> areas = tableAreaService.getAllAreas();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy danh sách khu vực thành công");
            response.put("areas", areas);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAreaById(@PathVariable Long id) {
        try {
            TableAreaDTO area = tableAreaService.getAreaById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy khu vực thành công");
            response.put("area", area);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createArea(@RequestBody TableAreaDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên khu vực không được để trống");
        }
        try {
            TableAreaDTO created = tableAreaService.createArea(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tạo khu vực thành công");
            response.put("area", created);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateArea(@PathVariable Long id, @RequestBody TableAreaDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên khu vực không được để trống");
        }
        try {
            TableAreaDTO updated = tableAreaService.updateArea(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cập nhật khu vực thành công");
            response.put("area", updated);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArea(@PathVariable Long id) {
        try {
            tableAreaService.deleteArea(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }
}
