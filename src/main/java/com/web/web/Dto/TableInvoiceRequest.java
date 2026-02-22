package com.web.web.Dto;

import lombok.Data;

@Data
public class TableInvoiceRequest {
    private String invoiceNo;
    private String tableName;
    private String areaName;
    private String paymentMethod;
    private double subtotal;
    private int discountPct;
    private double discountAmt;
    private double promoAmt;
    private double surcharge;
    private double vatAmt;
    private double total;
    private String itemsJson;
    private String cashier;
}
