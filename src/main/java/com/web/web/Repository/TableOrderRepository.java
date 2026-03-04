package com.web.web.Repository;

import com.web.web.Entity.TableOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableOrderRepository extends JpaRepository<TableOrder, Long> {

    Optional<TableOrder> findByTableId(Long tableId);

    void deleteByTableId(Long tableId);
}
