package com.web.web.Repository;

import com.web.web.Entity.TableInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TableInvoiceRepository extends JpaRepository<TableInvoice, Long> {

    List<TableInvoice> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM TableInvoice i WHERE i.createdAt BETWEEN :start AND :end")
    double sumTotalByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(i) FROM TableInvoice i WHERE i.createdAt BETWEEN :start AND :end")
    long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
