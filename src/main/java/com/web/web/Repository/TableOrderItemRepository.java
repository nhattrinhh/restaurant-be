package com.web.web.Repository;

import com.web.web.Entity.TableOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableOrderItemRepository extends JpaRepository<TableOrderItem, Long> {
}
