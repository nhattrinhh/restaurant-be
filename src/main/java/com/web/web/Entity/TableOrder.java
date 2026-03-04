package com.web.web.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "table_orders")
public class TableOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id", nullable = false, unique = true)
    private Long tableId;

    @Column(columnDefinition = "TEXT")
    private String itemsJson; // JSON array of order items

    @Column
    private int discount = 0; // discount percentage

    @Column
    private double surcharge = 0;

    @Column
    private double promo = 0;

    @Column(length = 20)
    private String customerPhone = "";

    @Column
    private double paid = 0;

    @Column(length = 10)
    private String entryTime = "";

    @Column(length = 20)
    private String entryDate = "";

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
