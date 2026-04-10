package com.web.web.Service;

import com.web.web.Dto.*;
import com.web.web.Entity.*;
import com.web.web.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableOrderService {

    private final TableOrderRepository orderRepo;
    private final TableOrderItemRepository itemRepo;
    private final ProductRepository productRepo;
    private final RestaurantTableRepository tableRepo;
    private final KitchenSseService kitchenSse;

    /**
     * table_orders has UNIQUE(table_id), so each table must reuse the same row.
     * Re-open CLOSED order rows instead of inserting a new one.
     */
    private TableOrder getOrCreateOpenOrder(Long tableId) {
        return orderRepo.findByTableIdAndStatus(tableId, TableOrder.OrderStatus.OPEN)
                .orElseGet(() -> orderRepo.findByTableId(tableId)
                        .map(existing -> {
                            existing.setStatus(TableOrder.OrderStatus.OPEN);
                            if (existing.getItems() != null) {
                                existing.getItems().clear();
                            }
                            return existing;
                        })
                        .orElseGet(() -> {
                            TableOrder o = new TableOrder();
                            o.setTableId(tableId);
                            o.setStatus(TableOrder.OrderStatus.OPEN);
                            return o;
                        }));
    }

    // ───────────────────────────────────────────────── GET helpers ──

    /** Get active (OPEN) order for a table, or empty Optional if none */
    public Optional<TableOrder> getActiveOrder(Long tableId) {
        return orderRepo.findByTableIdAndStatus(tableId, TableOrder.OrderStatus.OPEN);
    }

    /** Build KitchenOrderResponse from a TableOrder entity */
    private KitchenOrderResponse toResponse(TableOrder order) {
        KitchenOrderResponse resp = new KitchenOrderResponse();
        resp.setTableOrderId(order.getId());
        resp.setTableId(order.getTableId());
        resp.setOrderStatus(order.getStatus().name());
        resp.setDiscount(order.getDiscount());
        resp.setSurcharge(order.getSurcharge());
        resp.setPromo(order.getPromo());
        resp.setCustomerPhone(order.getCustomerPhone());
        resp.setPaid(order.getPaid());
        resp.setEntryTime(order.getEntryTime());
        resp.setEntryDate(order.getEntryDate());
        resp.setItemsJson(order.getItemsJson());

        // Enrich with table name + area name
        tableRepo.findById(order.getTableId()).ifPresent(table -> {
            resp.setTableName(table.getName());
            if (table.getArea() != null) {
                resp.setAreaName(table.getArea().getName());
            }
        });

        // Map items
        List<TableOrderItemDto> itemDtos = order.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
        resp.setItems(itemDtos);

        return resp;
    }

    private TableOrderItemDto toItemDto(TableOrderItem item) {
        TableOrderItemDto dto = new TableOrderItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setQuantity(item.getQuantity());
        dto.setNote(item.getNote());
        dto.setBatchNumber(item.getBatchNumber());
        dto.setStatus(item.getStatus().name());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    // ───────────────────────────────────────── UPSERT (backward compat for POS) ──

    /**
     * Upsert order metadata (discount, surcharge, etc.) for backward compat
     * with existing POS. Does NOT touch items — items are managed via
     * sendToKitchen.
     */
    @Transactional
    public KitchenOrderResponse saveOrderMeta(Long tableId, TableOrderDto dto) {
        TableOrder order = getOrCreateOpenOrder(tableId);

        order.setDiscount(dto.getDiscount());
        order.setSurcharge(dto.getSurcharge());
        order.setPromo(dto.getPromo());
        order.setCustomerPhone(dto.getCustomerPhone() != null ? dto.getCustomerPhone() : "");
        order.setPaid(dto.getPaid());
        order.setEntryTime(dto.getEntryTime() != null ? dto.getEntryTime() : "");
        order.setEntryDate(dto.getEntryDate() != null ? dto.getEntryDate() : "");
        order.setItemsJson(dto.getItemsJson());

        order = orderRepo.save(order);
        // Force lazy load
        order.getItems().size();
        return toResponse(order);
    }

    // ───────────────────────────────────────── SEND TO KITCHEN ──

    /**
     * Send items to kitchen: creates a new batch of TableOrderItems with PENDING
     * status.
     * Pushes SSE event to all connected KDS.
     */
    @Transactional
    public KitchenOrderResponse sendToKitchen(SendToKitchenRequest request) {
        Long tableId = request.getTableId();

        // table_orders is one-row-per-table; reopen CLOSED row instead of inserting
        TableOrder order = getOrCreateOpenOrder(tableId);
        if (order.getEntryTime() == null || order.getEntryTime().isBlank()) {
            java.time.LocalTime now = java.time.LocalTime.now();
            order.setEntryTime(String.format("%02d:%02d", now.getHour(), now.getMinute()));
        }
        if (order.getEntryDate() == null || order.getEntryDate().isBlank()) {
            order.setEntryDate(java.time.LocalDate.now().toString());
        }
        order = orderRepo.save(order);

        // Calculate next batch number
        int maxBatch = order.getItems().stream()
                .mapToInt(TableOrderItem::getBatchNumber)
                .max()
                .orElse(0);
        int newBatch = maxBatch + 1;

        // Create items for this batch
        List<TableOrderItem> newItems = new ArrayList<>();
        for (SendToKitchenRequest.KitchenItem reqItem : request.getItems()) {
            Product product = productRepo.findById(reqItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + reqItem.getProductId()));

            TableOrderItem item = new TableOrderItem();
            item.setTableOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            // Snapshot price: use discounted price if > 0, else original
            double rawPrice = product.getDiscountedPrice() > 0
                    ? product.getDiscountedPrice()
                    : product.getOriginalPrice();
            item.setUnitPrice(BigDecimal.valueOf(rawPrice));
            item.setQuantity(reqItem.getQuantity());
            item.setNote(reqItem.getNote() != null ? reqItem.getNote() : "");
            item.setBatchNumber(newBatch);
            item.setStatus(TableOrderItem.ItemStatus.PENDING);

            newItems.add(item);
        }

        itemRepo.saveAll(newItems);

        // STALE DATA FIX: Manually add new items to the order collection in memory
        // This ensures toResponse(order) includes them even if Hibernate session hasn't
        // refreshed
        if (order.getItems() == null) {
            order.setItems(new ArrayList<>());
        }
        order.getItems().addAll(newItems);

        // Draft items are now promoted to real kitchen items.
        order.setItemsJson(null);
        orderRepo.save(order);

        KitchenOrderResponse response = toResponse(order);

        // Push SSE event to KDS
        kitchenSse.pushEvent("new-order", response);

        return response;
    }

    // ───────────────────────────────────────── UPDATE ITEM STATUS ──

    /**
     * Update status of a single item (PENDING → COOKING → DONE).
     * Pushes SSE event to KDS.
     */
    @Transactional
    public TableOrderItemDto updateItemStatus(Long itemId, String newStatus) {
        TableOrderItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Order item not found: " + itemId));

        TableOrderItem.ItemStatus status = TableOrderItem.ItemStatus.valueOf(newStatus.toUpperCase());
        item.setStatus(status);
        item = itemRepo.save(item);

        TableOrderItemDto dto = toItemDto(item);

        // Build a lightweight update event for KDS
        Map<String, Object> event = new HashMap<>();
        event.put("itemId", dto.getId());
        event.put("status", dto.getStatus());
        event.put("tableOrderId", item.getTableOrder().getId());
        event.put("tableId", item.getTableOrder().getTableId());

        // Enrich with table name
        tableRepo.findById(item.getTableOrder().getTableId()).ifPresent(table -> {
            event.put("tableName", table.getName());
        });

        kitchenSse.pushEvent("status-update", event);

        return dto;
    }

    // ───────────────────────────────────────── GET CURRENT ORDER (for checkout) ──

    /**
     * Get all items for a table's active order (used by POS for checkout).
     */
    public KitchenOrderResponse getCurrentOrder(Long tableId) {
        TableOrder order = orderRepo.findByTableIdAndStatus(tableId, TableOrder.OrderStatus.OPEN)
                .orElseThrow(() -> new RuntimeException("No active order for table: " + tableId));

        order.getItems().size(); // force lazy load
        return toResponse(order);
    }

    // ───────────────────────────────────────── GET ALL ACTIVE (for KDS) ──

    /**
     * Get all OPEN orders with items — for KDS initial load.
     */
    public List<KitchenOrderResponse> getAllActiveOrders() {
        List<TableOrder> orders = orderRepo.findAllByStatusWithItems(TableOrder.OrderStatus.OPEN);
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ───────────────────────────────────────── CLOSE ORDER (checkout) ──

    /**
     * Close an order after payment: marks as CLOSED, resets table to EMPTY.
     * Pushes SSE event to KDS.
     */
    @Transactional
    public void closeOrder(Long tableId) {
        TableOrder order = orderRepo.findByTableIdAndStatus(tableId, TableOrder.OrderStatus.OPEN)
                .orElse(null);

        if (order != null) {
            order.setStatus(TableOrder.OrderStatus.CLOSED);
            orderRepo.save(order);

            // Push SSE to KDS — remove all cards for this table
            Map<String, Object> event = new HashMap<>();
            event.put("tableId", tableId);
            event.put("tableOrderId", order.getId());
            event.put("action", "closed");
            kitchenSse.pushEvent("order-closed", event);
        }

        // Reset table status
        tableRepo.findById(tableId).ifPresent(table -> {
            table.setStatus(RestaurantTable.TableStatus.EMPTY);
            tableRepo.save(table);
        });
    }

    /** Delete active order (legacy — used by old POS clear flow) */
    @Transactional
    public void deleteOrder(Long tableId) {
        orderRepo.deleteByTableId(tableId);
    }
}
