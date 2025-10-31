package com.web.web.Controller;
import com.web.web.Dto.ProductTypeDTO;
import com.web.web.Service.ProductTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-types")
public class ProductTypeController {

    @Autowired
    private ProductTypeService productTypeService;

    // Tạo loại sản phẩm mới
    @PostMapping
    public ResponseEntity<ProductTypeDTO> createProductType(@RequestBody ProductTypeDTO productTypeDTO) {
        ProductTypeDTO createdProductType = productTypeService.createProductType(productTypeDTO);
        return ResponseEntity.ok(createdProductType);
    }

    // Lấy tất cả loại sản phẩm
    @GetMapping
    public ResponseEntity<List<ProductTypeDTO>> getAllProductTypes() {
        List<ProductTypeDTO> productTypes = productTypeService.getAllProductTypes();
        return ResponseEntity.ok(productTypes);
    }

    // Lấy loại sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductTypeDTO> getProductTypeById(@PathVariable Long id) {
        ProductTypeDTO productType = productTypeService.getProductTypeById(id);
        return ResponseEntity.ok(productType);
    }

    // Cập nhật loại sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<ProductTypeDTO> updateProductType(@PathVariable Long id, @RequestBody ProductTypeDTO productTypeDTO) {
        ProductTypeDTO updatedProductType = productTypeService.updateProductType(id, productTypeDTO);
        return ResponseEntity.ok(updatedProductType);
    }

    // Xóa loại sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductType(@PathVariable Long id) {
        productTypeService.deleteProductType(id);
        return ResponseEntity.noContent().build();
    }
}
