package com.web.web.Service;
import com.web.web.Dto.CategoryDTO;
import com.web.web.Entity.Category;
import com.web.web.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Ánh xạ từ Category sang CategoryDTO
    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

    // Ánh xạ từ CategoryDTO sang Category
    private Category toEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        return category;
    }

    // Tạo danh mục mới
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Kiểm tra trường bắt buộc
        if (categoryDTO.getName() == null || categoryDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }

        // Kiểm tra xem tên danh mục đã tồn tại chưa
        if (categoryRepository.findByName(categoryDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Danh mục với tên '" + categoryDTO.getName() + "' đã tồn tại");
        }

        Category category = toEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        return toDTO(savedCategory);
    }

    // Lấy tất cả danh mục
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy danh mục theo ID
    public CategoryDTO getCategoryById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            return toDTO(category.get());
        } else {
            throw new RuntimeException("Danh mục không tồn tại với ID: " + id);
        }
    }

    // Cập nhật danh mục
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        if (categoryDTO.getName() == null || categoryDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }

        // Kiểm tra xem tên danh mục đã tồn tại chưa (trừ danh mục hiện tại)
        Optional<Category> existingCategoryWithName = categoryRepository.findByName(categoryDTO.getName());
        if (existingCategoryWithName.isPresent() && !existingCategoryWithName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Danh mục với tên '" + categoryDTO.getName() + "' đã tồn tại");
        }

        Optional<Category> existingCategory = categoryRepository.findById(id);
        if (existingCategory.isPresent()) {
            Category category = existingCategory.get();
            category.setName(categoryDTO.getName());
            Category updatedCategory = categoryRepository.save(category);
            return toDTO(updatedCategory);
        } else {
            throw new RuntimeException("Danh mục không tồn tại với ID: " + id);
        }
    }

    // Xóa danh mục
    public void deleteCategory(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
        } else {
            throw new RuntimeException("Danh mục không tồn tại với ID: " + id);
        }
    }
}
