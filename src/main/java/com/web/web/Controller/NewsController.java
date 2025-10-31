package com.web.web.Controller;
import com.web.web.Dto.NewsDTO;
import com.web.web.Service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    // Create new news
    @PostMapping
    public ResponseEntity<?> createNews(@RequestBody NewsDTO newsDTO) {
        if (newsDTO.getTitle() == null || newsDTO.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tiêu đề tin tức không được để trống");
        }

        try {
            NewsDTO createdNews = newsService.createNews(newsDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tạo tin tức thành công");
            response.put("news", createdNews);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo tin tức: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi tạo tin tức: " + e.getMessage());
        }
    }

    // Get all news
    @GetMapping
    public ResponseEntity<?> getAllNews() {
        try {
            List<NewsDTO> newsList = newsService.getAllNews();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy danh sách tin tức thành công");
            response.put("news", newsList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi lấy danh sách tin tức: " + e.getMessage());
        }
    }

    // Search news by title
    @GetMapping("/search")
    public ResponseEntity<?> searchNewsByTitle(@RequestParam("title") String title) {
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tiêu đề tin tức không được để trống");
        }

        try {
            List<NewsDTO> newsList = newsService.searchNewsByTitle(title);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tìm kiếm tin tức thành công");
            response.put("news", newsList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi tìm kiếm tin tức: " + e.getMessage());
        }
    }

    // Get news by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getNewsById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID tin tức không hợp lệ");
        }

        try {
            NewsDTO news = newsService.getNewsById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy thông tin tin tức thành công");
            response.put("news", news);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Tin tức không tồn tại với ID: " + id);
        }
    }

    // Update news
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNews(@PathVariable Long id, @RequestBody NewsDTO newsDTO) {
        if (id == null || id <= 0 || newsDTO.getTitle() == null || newsDTO.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("ID hoặc tiêu đề tin tức không hợp lệ");
        }

        try {
            NewsDTO updatedNews = newsService.updateNews(id, newsDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cập nhật tin tức thành công");
            response.put("news", updatedNews);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật tin tức: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Tin tức không tồn tại với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi cập nhật tin tức: " + e.getMessage());
        }
    }

    // Delete news
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID tin tức không hợp lệ");
        }

        try {
            newsService.deleteNews(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Tin tức không tồn tại với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi xóa tin tức: " + e.getMessage());
        }
    }
}
