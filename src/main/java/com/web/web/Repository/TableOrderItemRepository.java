package com.web.web.Repository;

import com.web.web.Entity.TableOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableOrderItemRepository extends JpaRepository<TableOrderItem, Long> {
	Optional<TableOrderItem> findByTableOrderIdAndProductIdAndStatus(
			Long tableOrderId,
			Long productId,
			TableOrderItem.ItemStatus status);

	List<TableOrderItem> findByTableOrderIdAndStatusOrderByCreatedAtAsc(Long tableOrderId, TableOrderItem.ItemStatus status);
}
