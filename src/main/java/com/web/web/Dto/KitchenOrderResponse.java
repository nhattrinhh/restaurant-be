package com.web.web.Dto;

import lombok.Data;
import java.util.List;

/**
 * Response DTO for kitchen-related order data.
 * Used by KDS (Kitchen Display) and POS checkout.
 */
@Data
public class KitchenOrderResponse {
    private Long tableOrderId;
    private Long tableId;
    private String tableName;
    private String areaName;
    private String orderStatus; // OPEN / CLOSED
    private int discount;
    private double surcharge;
    private double promo;
    private String customerPhone;
    private double paid;
    private String entryTime;
    private String entryDate;
    private String itemsJson;
    private List<TableOrderItemDto> items;
}
