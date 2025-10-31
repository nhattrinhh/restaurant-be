package com.web.web.Service;
import com.web.web.Dto.NewsDTO;
import com.web.web.Entity.News;
import com.web.web.Repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    // Ánh xạ từ News sang NewsDTO
    private NewsDTO toDTO(News news) {
        NewsDTO dto = new NewsDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setTimestamp(news.getTimestamp());
        dto.setDescription(news.getDescription());
        dto.setImageUrl(news.getImageUrl());
        return dto;
    }

    // Ánh xạ từ NewsDTO sang News
    private News toEntity(NewsDTO dto) {
        News news = new News();
        news.setId(dto.getId());
        news.setTitle(dto.getTitle());
        news.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
        news.setDescription(dto.getDescription());
        news.setImageUrl(dto.getImageUrl());
        return news;
    }

    // Tạo tin tức mới
    public NewsDTO createNews(NewsDTO newsDTO) {
        // Kiểm tra các trường bắt buộc
        if (newsDTO.getTitle() == null || newsDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề tin tức không được để trống");
        }

        // Kiểm tra URL ảnh nếu có
        if (newsDTO.getImageUrl() != null && !newsDTO.getImageUrl().trim().isEmpty()) {
            try {
                new URL(newsDTO.getImageUrl()).toURI();
            } catch (Exception e) {
                throw new IllegalArgumentException("URL hình ảnh không hợp lệ");
            }
        }

        // Kiểm tra sự tồn tại của tiêu đề
        if (newsRepository.findByTitleContainingIgnoreCase(newsDTO.getTitle()).stream()
                .anyMatch(news -> !news.getId().equals(newsDTO.getId()))) {
            throw new IllegalArgumentException("Tin tức với tiêu đề '" + newsDTO.getTitle() + "' đã tồn tại");
        }

        News news = toEntity(newsDTO);
        News savedNews = newsRepository.save(news);
        return toDTO(savedNews);
    }

    // Lấy tất cả tin tức
    public List<NewsDTO> getAllNews() {
        return newsRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy tin tức theo ID
    public NewsDTO getNewsById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID tin tức không hợp lệ");
        }
        Optional<News> news = newsRepository.findById(id);
        if (news.isPresent()) {
            return toDTO(news.get());
        } else {
            throw new RuntimeException("Tin tức không tồn tại với ID: " + id);
        }
    }

    // Tìm tin tức theo tiêu đề
    public List<NewsDTO> searchNewsByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề tin tức không được để trống");
        }
        return newsRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Cập nhật tin tức
    public NewsDTO updateNews(Long id, NewsDTO newsDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID tin tức không hợp lệ");
        }
        if (newsDTO.getTitle() == null || newsDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề tin tức không được để trống");
        }

        // Kiểm tra URL ảnh nếu có
        if (newsDTO.getImageUrl() != null && !newsDTO.getImageUrl().trim().isEmpty()) {
            try {
                new URL(newsDTO.getImageUrl()).toURI();
            } catch (Exception e) {
                throw new IllegalArgumentException("URL hình ảnh không hợp lệ");
            }
        }

        Optional<News> existingNews = newsRepository.findById(id);
        if (existingNews.isPresent()) {
            News news = existingNews.get();
            // Kiểm tra sự tồn tại của tiêu đề (trừ tin tức hiện tại)
            if (!news.getTitle().equals(newsDTO.getTitle()) &&
                    newsRepository.findByTitleContainingIgnoreCase(newsDTO.getTitle()).stream()
                            .anyMatch(n -> !n.getId().equals(id))) {
                throw new IllegalArgumentException("Tin tức với tiêu đề '" + newsDTO.getTitle() + "' đã tồn tại");
            }
            news.setTitle(newsDTO.getTitle());
            news.setTimestamp(newsDTO.getTimestamp() != null ? newsDTO.getTimestamp() : LocalDateTime.now());
            news.setDescription(newsDTO.getDescription());
            news.setImageUrl(newsDTO.getImageUrl());
            News updatedNews = newsRepository.save(news);
            return toDTO(updatedNews);
        } else {
            throw new RuntimeException("Tin tức không tồn tại với ID: " + id);
        }
    }

    // Xóa tin tức
    public void deleteNews(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID tin tức không hợp lệ");
        }
        if (newsRepository.existsById(id)) {
            newsRepository.deleteById(id);
        } else {
            throw new RuntimeException("Tin tức không tồn tại với ID: " + id);
        }
    }
}
