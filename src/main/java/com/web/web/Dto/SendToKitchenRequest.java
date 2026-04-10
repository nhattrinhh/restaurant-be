package com.web.web.Dto;

import lombok.Data;
import java.util.List;

@Data
public class SendToKitchenRequest {
    private Long tableId;
    private List<KitchenItem> items;

    @Data
    public static class KitchenItem {
        private Long productId;
        private int quantity;
        private String note;
    }
}
