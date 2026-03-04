package com.web.web.Service;

import com.web.web.Entity.Promotion.PromotionStatus;
import com.web.web.Repository.PromotionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class PromotionScheduler {

    @Autowired
    private PromotionRepository promotionRepository;

    /**
     * Scheduled task to check and update expired promotions.
     * Runs every hour (3600000 ms).
     */
    @Scheduled(fixedRate = 10000)
    public void expirePromotions() {
        log.info("Running scheduled task: expirePromotions");
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = promotionRepository.updateStatusForExpiredPromotions(now, PromotionStatus.EXPIRED);

        if (updatedCount > 0) {
            log.info("Successfully expired {} promotions", updatedCount);
        } else {
            log.debug("No promotions expired in this run");
        }
    }
}
