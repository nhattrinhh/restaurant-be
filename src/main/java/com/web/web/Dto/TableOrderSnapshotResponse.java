package com.web.web.Dto;

import lombok.Data;

import java.util.List;

@Data
public class TableOrderSnapshotResponse {
    private Long tableOrderId;
    private Long tableId;
    private String tableName;
    private String areaName;
    private String orderStatus;
    private int discount;
    private double surcharge;
    private double promo;
    private String customerPhone;
    private double paid;
    private String entryTime;
    private String entryDate;
    private List<TableOrderItemDto> items;
}
