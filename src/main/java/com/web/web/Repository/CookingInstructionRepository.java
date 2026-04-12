package com.web.web.Repository;

import com.web.web.Entity.CookingInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CookingInstructionRepository extends JpaRepository<CookingInstruction, Long> {
    Optional<CookingInstruction> findByProductId(Long productId);
}
