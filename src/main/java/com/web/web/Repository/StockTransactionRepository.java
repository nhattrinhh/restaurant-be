package com.web.web.Repository;

import com.web.web.Entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByIngredientIdAndIsDeletedFalse(Long ingredientId);
    List<StockTransaction> findByRecordedById(Long userId);
    List<StockTransaction> findByRefItemIdAndIsDeletedFalse(Long refItemId);
}
