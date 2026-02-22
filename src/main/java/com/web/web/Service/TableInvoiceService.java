package com.web.web.Service;

import com.web.web.Dto.TableInvoiceRequest;
import com.web.web.Entity.TableInvoice;
import com.web.web.Repository.TableInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TableInvoiceService {

    private final TableInvoiceRepository repo;

    public TableInvoice save(TableInvoiceRequest req) {
        TableInvoice inv = new TableInvoice();
        inv.setInvoiceNo(req.getInvoiceNo());
        inv.setTableName(req.getTableName());
        inv.setAreaName(req.getAreaName());
        inv.setPaymentMethod(req.getPaymentMethod());
        inv.setSubtotal(req.getSubtotal());
        inv.setDiscountPct(req.getDiscountPct());
        inv.setDiscountAmt(req.getDiscountAmt());
        inv.setPromoAmt(req.getPromoAmt());
        inv.setSurcharge(req.getSurcharge());
        inv.setVatAmt(req.getVatAmt());
        inv.setTotal(req.getTotal());
        inv.setItemsJson(req.getItemsJson());
        inv.setCashier(req.getCashier() != null ? req.getCashier() : "Admin");
        return repo.save(inv);
    }

    public List<TableInvoice> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return repo.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    public Map<String, Object> getSummary(LocalDateTime start, LocalDateTime end) {
        double totalRevenue = repo.sumTotalByDateRange(start, end);
        long count = repo.countByDateRange(start, end);
        double average = count > 0 ? totalRevenue / count : 0;
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("invoiceCount", count);
        summary.put("averagePerInvoice", average);
        return summary;
    }

    public TableInvoice findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Invoice not found"));
    }
}
