package com.web.web.Service;

import com.web.web.Dto.TableOrderDto;
import com.web.web.Entity.TableOrder;
import com.web.web.Repository.TableOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TableOrderService {

    private final TableOrderRepository repo;

    /** Get active order for a table, or empty Optional if none */
    public Optional<TableOrder> getOrder(Long tableId) {
        return repo.findByTableId(tableId);
    }

    /** Upsert (create or update) an active order for a table */
    @Transactional
    public TableOrder saveOrder(Long tableId, TableOrderDto dto) {
        TableOrder order = repo.findByTableId(tableId)
                .orElseGet(() -> {
                    TableOrder o = new TableOrder();
                    o.setTableId(tableId);
                    return o;
                });

        order.setItemsJson(dto.getItemsJson());
        order.setDiscount(dto.getDiscount());
        order.setSurcharge(dto.getSurcharge());
        order.setPromo(dto.getPromo());
        order.setCustomerPhone(dto.getCustomerPhone() != null ? dto.getCustomerPhone() : "");
        order.setPaid(dto.getPaid());
        order.setEntryTime(dto.getEntryTime() != null ? dto.getEntryTime() : "");
        order.setEntryDate(dto.getEntryDate() != null ? dto.getEntryDate() : "");

        return repo.save(order);
    }

    /** Delete active order when table is checked out or cancelled */
    @Transactional
    public void deleteOrder(Long tableId) {
        repo.deleteByTableId(tableId);
    }
}
