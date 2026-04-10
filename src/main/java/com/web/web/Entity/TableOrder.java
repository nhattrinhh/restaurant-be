package com.web.web.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "table_orders")
public class TableOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.OPEN;

    @OneToMany(mappedBy = "tableOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TableOrderItem> items = new ArrayList<>();

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

    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        OPEN, CLOSED
    }
}
