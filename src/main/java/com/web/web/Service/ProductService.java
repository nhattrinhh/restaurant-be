package com.web.web.Service;

import com.web.web.Dto.ProductDTO;
import com.web.web.Entity.Category;
import com.web.web.Entity.Product;
import com.web.web.Entity.ProductType;
import com.web.web.Repository.CategoryRepository;
import com.web.web.Repository.ProductRepository;
import com.web.web.Repository.ProductTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Ánh xạ từ Product sang ProductDTO
    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setDiscountedPrice(product.getDiscountedPrice());
        dto.setDiscount(product.getDiscount());
        dto.setProductTypeId(product.getProductType() != null ? product.getProductType().getId() : null);
        dto.setProductTypeName(product.getProductType() != null ? product.getProductType().getName() : null);
        dto.setImg(product.getImg());
        dto.setStatus(product.getStatus());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        return dto;
    }

    // Ánh xạ từ ProductDTO sang Product
    private Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setDiscountedPrice(dto.getDiscountedPrice());
        product.setDiscount(dto.getDiscount());

        // Tìm ProductType theo ID
        if (dto.getProductTypeId() != null) {
            Optional<ProductType> productType = productTypeRepository.findById(dto.getProductTypeId());
            if (productType.isPresent()) {
                product.setProductType(productType.get());
            } else {
                throw new IllegalArgumentException("Loại sản phẩm không tồn tại với ID: " + dto.getProductTypeId());
            }
        } else {
            throw new IllegalArgumentException("ID loại sản phẩm không được để trống");
        }

        product.setImg(dto.getImg());
        product.setStatus(dto.getStatus());

        // Tìm Category theo ID (nếu có)
        if (dto.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findById(dto.getCategoryId());
            if (category.isPresent()) {
                product.setCategory(category.get());
            } else {
                throw new IllegalArgumentException("Danh mục không tồn tại với ID: " + dto.getCategoryId());
            }
        }

        return product;
    }

    // Tạo sản phẩm mới
    public ProductDTO createProduct(ProductDTO productDTO) {
        // Kiểm tra các trường bắt buộc
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty() ||
                productDTO.getProductTypeId() == null ||
                productDTO.getStatus() == null || productDTO.getStatus().trim().isEmpty() ||
                productDTO.getOriginalPrice() <= 0) {
            throw new IllegalArgumentException("Tên sản phẩm, ID loại sản phẩm, trạng thái và giá gốc không được để trống hoặc không hợp lệ");
        }

        // Kiểm tra giá trị trạng thái hợp lệ
        if (!List.of("AVAILABLE", "OUT_OF_STOCK", "DISCONTINUED").contains(productDTO.getStatus())) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Phải là một trong: AVAILABLE, OUT_OF_STOCK, DISCONTINUED");
        }

        // Kiểm tra danh mục nếu có
        if (productDTO.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findById(productDTO.getCategoryId());
            if (!category.isPresent()) {
                throw new IllegalArgumentException("Danh mục không tồn tại với ID: " + productDTO.getCategoryId());
            }
            // Kiểm tra tên danh mục nếu có
            if (productDTO.getCategoryName() != null && !productDTO.getCategoryName().equals(category.get().getName())) {
                throw new IllegalArgumentException("Tên danh mục không khớp với ID danh mục");
            }
        }

        // Kiểm tra loại sản phẩm
        Optional<ProductType> productType = productTypeRepository.findById(productDTO.getProductTypeId());
        if (!productType.isPresent()) {
            throw new IllegalArgumentException("Loại sản phẩm không tồn tại với ID: " + productDTO.getProductTypeId());
        }
        // Kiểm tra tên loại sản phẩm nếu có
        if (productDTO.getProductTypeName() != null && !productDTO.getProductTypeName().equals(productType.get().getName())) {
            throw new IllegalArgumentException("Tên loại sản phẩm không khớp với ID loại sản phẩm");
        }

        // Kiểm tra giá giảm và tỷ lệ giảm giá
        if (productDTO.getDiscountedPrice() < 0 || productDTO.getDiscount() < 0) {
            throw new IllegalArgumentException("Giá giảm và tỷ lệ giảm giá không được âm");
        }

        // Kiểm tra sự tồn tại của sản phẩm theo tên
        if (productRepository.existsByName(productDTO.getName())) {
            throw new IllegalArgumentException("Sản phẩm với tên '" + productDTO.getName() + "' đã tồn tại");
        }

        Product product = toEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return toDTO(savedProduct);
    }

    // Lấy tất cả sản phẩm
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy sản phẩm theo ID
    public ProductDTO getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID sản phẩm không hợp lệ");
        }
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return toDTO(product.get());
        } else {
            throw new RuntimeException("Sản phẩm không tồn tại với ID: " + id);
        }
    }

    // Lấy sản phẩm theo ID loại sản phẩm
    public List<ProductDTO> getProductsByProductTypeId(Long productTypeId) {
        if (productTypeId == null || productTypeId <= 0) {
            throw new IllegalArgumentException("ID loại sản phẩm không hợp lệ");
        }
        return productRepository.findByProductTypeId(productTypeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy sản phẩm theo ID danh mục
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Tìm sản phẩm theo tên (gần đúng, không phân biệt hoa thường)
    public List<ProductDTO> searchProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Cập nhật sản phẩm
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID sản phẩm không hợp lệ");
        }
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty() ||
                productDTO.getProductTypeId() == null ||
                productDTO.getStatus() == null || productDTO.getStatus().trim().isEmpty() ||
                productDTO.getOriginalPrice() <= 0) {
            throw new IllegalArgumentException("Tên sản phẩm, ID loại sản phẩm, trạng thái và giá gốc không được để trống hoặc không hợp lệ");
        }

        // Kiểm tra giá trị trạng thái hợp lệ
        if (!List.of("AVAILABLE", "OUT_OF_STOCK", "DISCONTINUED").contains(productDTO.getStatus())) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Phải là một trong: AVAILABLE, OUT_OF_STOCK, DISCONTINUED");
        }

        // Kiểm tra danh mục nếu có
        if (productDTO.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findById(productDTO.getCategoryId());
            if (!category.isPresent()) {
                throw new IllegalArgumentException("Danh mục không tồn tại với ID: " + productDTO.getCategoryId());
            }
            // Kiểm tra tên danh mục nếu có
            if (productDTO.getCategoryName() != null && !productDTO.getCategoryName().equals(category.get().getName())) {
                throw new IllegalArgumentException("Tên danh mục không khớp với ID danh mục");
            }
        }

        // Kiểm tra loại sản phẩm
        Optional<ProductType> productType = productTypeRepository.findById(productDTO.getProductTypeId());
        if (!productType.isPresent()) {
            throw new IllegalArgumentException("Loại sản phẩm không tồn tại với ID: " + productDTO.getProductTypeId());
        }
        // Kiểm tra tên loại sản phẩm nếu có
        if (productDTO.getProductTypeName() != null && !productDTO.getProductTypeName().equals(productType.get().getName())) {
            throw new IllegalArgumentException("Tên loại sản phẩm không khớp với ID loại sản phẩm");
        }

        // Kiểm tra giá giảm và tỷ lệ giảm giá
        if (productDTO.getDiscountedPrice() < 0 || productDTO.getDiscount() < 0) {
            throw new IllegalArgumentException("Giá giảm và tỷ lệ giảm giá không được âm");
        }

        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            // Kiểm tra sự tồn tại của tên sản phẩm (trừ sản phẩm hiện tại)
            if (!product.getName().equals(productDTO.getName()) && productRepository.existsByName(productDTO.getName())) {
                throw new IllegalArgumentException("Sản phẩm với tên '" + productDTO.getName() + "' đã tồn tại");
            }
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setOriginalPrice(productDTO.getOriginalPrice());
            product.setDiscountedPrice(productDTO.getDiscountedPrice());
            product.setDiscount(productDTO.getDiscount());
            product.setProductType(productType.get());
            product.setImg(productDTO.getImg());
            product.setStatus(productDTO.getStatus());
            if (productDTO.getCategoryId() != null) {
                Optional<Category> category = categoryRepository.findById(productDTO.getCategoryId());
                product.setCategory(category.orElse(null));
            } else {
                product.setCategory(null);
            }
            Product updatedProduct = productRepository.save(product);
            return toDTO(updatedProduct);
        } else {
            throw new RuntimeException("Sản phẩm không tồn tại với ID: " + id);
        }
    }

    // Xóa sản phẩm
    public void deleteProduct(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID sản phẩm không hợp lệ");
        }
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        } else {
            throw new RuntimeException("Sản phẩm không tồn tại với ID: " + id);
        }
    }
}
