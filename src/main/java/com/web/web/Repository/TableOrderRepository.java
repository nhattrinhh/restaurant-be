package com.web.web.Repository;

import com.web.web.Entity.TableOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableOrderRepository extends JpaRepository<TableOrder, Long> {


    Optional<TableOrder> findByTableIdAndStatus(Long tableId, TableOrder.OrderStatus status);

    @Query("SELECT o FROM TableOrder o LEFT JOIN FETCH o.items WHERE o.tableId = :tableId AND o.status = :status")
    Optional<TableOrder> findByTableIdAndStatusWithItems(Long tableId, TableOrder.OrderStatus status);

    @Query("SELECT o FROM TableOrder o LEFT JOIN FETCH o.items WHERE o.status = :status")
    List<TableOrder> findAllByStatusWithItems(TableOrder.OrderStatus status);

    void deleteByTableId(Long tableId);
}
