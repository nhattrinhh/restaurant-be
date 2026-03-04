package com.web.web.Repository;

import com.web.web.Entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web.web.Entity.Promotion.PromotionStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Promotion p SET p.status = :status WHERE p.endDate < :now AND p.status != :status")
    int updateStatusForExpiredPromotions(@Param("now") LocalDateTime now, @Param("status") PromotionStatus status);
}
