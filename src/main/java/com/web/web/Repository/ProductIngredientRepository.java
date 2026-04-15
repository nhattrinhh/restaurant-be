package com.web.web.Repository;

import com.web.web.Entity.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {
    List<ProductIngredient> findByProductId(Long productId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ProductIngredient pi WHERE pi.product.id = :productId")
    void deleteByProductId(Long productId);
}
