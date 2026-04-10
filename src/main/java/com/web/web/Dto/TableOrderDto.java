package com.web.web.Dto;

import lombok.Data;

/**
 * DTO for saving/updating order metadata (discount, surcharge, etc.)
 * Items are now managed via SendToKitchenRequest / table_order_items.
 */
@Data
public class TableOrderDto {
    private int discount;
    private double surcharge;
    private double promo;
    private String customerPhone;
    private double paid;
    private String entryTime;
    private String entryDate;
    private String itemsJson;
}
