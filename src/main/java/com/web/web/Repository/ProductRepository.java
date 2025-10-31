package com.web.web.Repository;
import com.web.web.Entity.Category;
import com.web.web.Entity.Product;
import com.web.web.Entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Tìm sản phẩm theo danh mục (Category entity)
    List<Product> findByCategory(Category category);

    // Tìm sản phẩm theo ID danh mục
    List<Product> findByCategoryId(Long categoryId);

    // Tìm sản phẩm theo loại sản phẩm (ProductType entity)
    List<Product> findByProductType(ProductType productType);

    // Tìm sản phẩm theo ID loại sản phẩm
    List<Product> findByProductTypeId(Long productTypeId);

    // Tìm sản phẩm theo tên (gần đúng, không phân biệt hoa thường)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Kiểm tra sự tồn tại của sản phẩm theo tên
    boolean existsByName(String name);

    // Kiểm tra sự tồn tại của sản phẩm theo ID loại sản phẩm
    boolean existsByProductTypeId(Long productTypeId);

    // Kiểm tra sự tồn tại của sản phẩm theo ID danh mục
    boolean existsByCategoryId(Long categoryId);

}
