package com.web.web.Controller;

import com.web.web.Dto.PromotionDto;
import com.web.web.Service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    // GET /api/promotions — Lấy tất cả khuyến mãi
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<PromotionDto> list = promotionService.getAll();
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Lấy danh sách khuyến mãi thành công");
            res.put("promotions", list);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    // POST /api/promotions — Tạo mới
    @PostMapping
    public ResponseEntity<?> create(@RequestBody PromotionDto dto) {
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Mã voucher không được để trống");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên chương trình không được để trống");
        }
        try {
            PromotionDto created = promotionService.create(dto);
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Tạo khuyến mãi thành công");
            res.put("promotion", created);
            return ResponseEntity.status(201).body(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    // PUT /api/promotions/{id} — Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PromotionDto dto) {
        try {
            PromotionDto updated = promotionService.update(id, dto);
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Cập nhật khuyến mãi thành công");
            res.put("promotion", updated);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    // DELETE /api/promotions/{id} — Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            promotionService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    // PATCH /api/promotions/{id}/toggle — Bật/Tắt nhanh
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        try {
            PromotionDto toggled = promotionService.toggleStatus(id);
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Cập nhật trạng thái thành công");
            res.put("promotion", toggled);
            return ResponseEntity.ok(res);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }
}
