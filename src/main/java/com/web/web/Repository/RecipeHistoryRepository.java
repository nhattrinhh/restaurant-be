package com.web.web.Repository;

import com.web.web.Entity.RecipeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeHistoryRepository extends JpaRepository<RecipeHistory, Long> {
    List<RecipeHistory> findByProductIdOrderByVersionDesc(Long productId);
}
