package com.web.web.Dto;

import lombok.Data;

@Data
public class TableOrderDto {
    private String itemsJson;
    private int discount;
    private double surcharge;
    private double promo;
    private String customerPhone;
    private double paid;
    private String entryTime;
    private String entryDate;
}
