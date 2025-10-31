package com.web.web.Controller;
import com.web.web.Dto.ProductDTO;
import com.web.web.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Tạo sản phẩm mới
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty() ||
                productDTO.getProductTypeId() == null ||
                productDTO.getStatus() == null || productDTO.getStatus().trim().isEmpty() ||
                productDTO.getOriginalPrice() <= 0) {
            return ResponseEntity.badRequest().body("Tên sản phẩm, ID loại sản phẩm, trạng thái và giá gốc không được để trống hoặc không hợp lệ");
        }

        if (productDTO.getImg() != null && !productDTO.getImg().trim().isEmpty()) {
            try {
                new URL(productDTO.getImg()).toURI();
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("URL hình ảnh không hợp lệ");
            }
        }

        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tạo sản phẩm thành công");
            response.put("product", createdProduct);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo sản phẩm: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi tạo sản phẩm: " + e.getMessage());
        }
    }

    // Lấy danh sách tất cả sản phẩm
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            List<ProductDTO> products = productService.getAllProducts();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy danh sách sản phẩm thành công");
            response.put("products", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    // Tìm sản phẩm theo tên (gần đúng)
    @GetMapping("/search")
    public ResponseEntity<?> searchProductsByName(@RequestParam("name") String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên sản phẩm không được để trống");
        }

        try {
            List<ProductDTO> products = productService.searchProductsByName(name);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tìm kiếm sản phẩm thành công");
            response.put("products", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi tìm kiếm sản phẩm: " + e.getMessage());
        }
    }

    // Lấy sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID sản phẩm không hợp lệ");
        }

        try {
            ProductDTO product = productService.getProductById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy thông tin sản phẩm thành công");
            response.put("product", product);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Sản phẩm không tồn tại với ID: " + id);
        }
    }

    // Lấy sản phẩm theo ID loại sản phẩm
    @GetMapping("/by-product-type/{productTypeId}")
    public ResponseEntity<?> getProductsByProductTypeId(@PathVariable Long productTypeId) {
        if (productTypeId == null || productTypeId <= 0) {
            return ResponseEntity.badRequest().body("ID loại sản phẩm không hợp lệ");
        }

        try {
            List<ProductDTO> products = productService.getProductsByProductTypeId(productTypeId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy danh sách sản phẩm theo loại sản phẩm thành công");
            response.put("products", products);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    // Lấy sản phẩm theo ID danh mục
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<?> getProductsByCategoryId(@PathVariable Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return ResponseEntity.badRequest().body("ID danh mục không hợp lệ");
        }

        try {
            List<ProductDTO> products = productService.getProductsByCategoryId(categoryId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy danh sách sản phẩm theo danh mục thành công");
            response.put("products", products);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    // Cập nhật sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        if (id == null || id <= 0 || productDTO.getName() == null || productDTO.getName().trim().isEmpty() ||
                productDTO.getProductTypeId() == null || productDTO.getStatus() == null ||
                productDTO.getStatus().trim().isEmpty() || productDTO.getOriginalPrice() <= 0) {
            return ResponseEntity.badRequest().body("ID sản phẩm, tên sản phẩm, ID loại sản phẩm, trạng thái hoặc giá gốc không hợp lệ");
        }

        if (productDTO.getImg() != null && !productDTO.getImg().trim().isEmpty()) {
            try {
                new URL(productDTO.getImg()).toURI();
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("URL hình ảnh không hợp lệ");
            }
        }

        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cập nhật sản phẩm thành công");
            response.put("product", updatedProduct);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Sản phẩm không tồn tại với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    // Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID sản phẩm không hợp lệ");
        }

        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Sản phẩm không tồn tại với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi xóa sản phẩm: " + e.getMessage());
        }
    }
}

