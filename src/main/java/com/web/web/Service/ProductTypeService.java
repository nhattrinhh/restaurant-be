package com.web.web.Service;

import com.web.web.Dto.ProductTypeDTO;
import com.web.web.Entity.ProductType;
import com.web.web.Repository.ProductTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductTypeService {

    @Autowired
    private ProductTypeRepository productTypeRepository;

    // Ánh xạ từ ProductType sang ProductTypeDTO
    private ProductTypeDTO toDTO(ProductType productType) {
        ProductTypeDTO dto = new ProductTypeDTO();
        dto.setId(productType.getId());
        dto.setName(productType.getName());
        return dto;
    }

    // Ánh xạ từ ProductTypeDTO sang ProductType
    private ProductType toEntity(ProductTypeDTO dto) {
        ProductType productType = new ProductType();
        productType.setId(dto.getId());
        productType.setName(dto.getName());
        return productType;
    }

    // Tạo loại sản phẩm mới
    public ProductTypeDTO createProductType(ProductTypeDTO productTypeDTO) {
        // Kiểm tra trường bắt buộc
        if (productTypeDTO.getName() == null || productTypeDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại sản phẩm không được để trống");
        }

        // Kiểm tra xem tên loại sản phẩm đã tồn tại chưa
        if (productTypeRepository.findByName(productTypeDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Loại sản phẩm với tên '" + productTypeDTO.getName() + "' đã tồn tại");
        }

        ProductType productType = toEntity(productTypeDTO);
        ProductType savedProductType = productTypeRepository.save(productType);
        return toDTO(savedProductType);
    }

    // Lấy tất cả loại sản phẩm
    public List<ProductTypeDTO> getAllProductTypes() {
        return productTypeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy loại sản phẩm theo ID
    public ProductTypeDTO getProductTypeById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID loại sản phẩm không hợp lệ");
        }
        Optional<ProductType> productType = productTypeRepository.findById(id);
        if (productType.isPresent()) {
            return toDTO(productType.get());
        } else {
            throw new RuntimeException("Loại sản phẩm không tồn tại với ID: " + id);
        }
    }

    // Cập nhật loại sản phẩm
    public ProductTypeDTO updateProductType(Long id, ProductTypeDTO productTypeDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID loại sản phẩm không hợp lệ");
        }
        if (productTypeDTO.getName() == null || productTypeDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại sản phẩm không được để trống");
        }

        // Kiểm tra xem tên loại sản phẩm đã tồn tại chưa (trừ loại sản phẩm hiện tại)
        Optional<ProductType> existingTypeWithName = productTypeRepository.findByName(productTypeDTO.getName());
        if (existingTypeWithName.isPresent() && !existingTypeWithName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Loại sản phẩm với tên '" + productTypeDTO.getName() + "' đã tồn tại");
        }

        Optional<ProductType> existingProductType = productTypeRepository.findById(id);
        if (existingProductType.isPresent()) {
            ProductType productType = existingProductType.get();
            productType.setName(productTypeDTO.getName());
            ProductType updatedProductType = productTypeRepository.save(productType);
            return toDTO(updatedProductType);
        } else {
            throw new RuntimeException("Loại sản phẩm không tồn tại với ID: " + id);
        }
    }

    // Xóa loại sản phẩm
    public void deleteProductType(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID loại sản phẩm không hợp lệ");
        }
        if (productTypeRepository.existsById(id)) {
            productTypeRepository.deleteById(id);
        } else {
            throw new RuntimeException("Loại sản phẩm không tồn tại với ID: " + id);
        }
    }
}
