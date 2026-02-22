package com.web.web.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "table_invoices")
public class TableInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String invoiceNo;

    @Column(nullable = false)
    private String tableName;

    @Column
    private String areaName;

    @Column(nullable = false)
    private String paymentMethod; // CASH | TRANSFER

    // Financial fields
    @Column(nullable = false)
    private double subtotal;

    @Column
    private int discountPct;

    @Column
    private double discountAmt;

    @Column
    private double promoAmt;

    @Column
    private double surcharge;

    @Column
    private double vatAmt;

    @Column(nullable = false)
    private double total;

    // Items stored as JSON string: [{name, qty, price, subtotal}, ...]
    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    @Column
    private String cashier;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}
