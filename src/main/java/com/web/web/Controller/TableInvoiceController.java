package com.web.web.Controller;

import com.web.web.Dto.TableInvoiceRequest;
import com.web.web.Entity.TableInvoice;
import com.web.web.Service.TableInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/table-invoices")
@RequiredArgsConstructor
public class TableInvoiceController {

    private final TableInvoiceService service;

    /** Save invoice on checkout */
    @PostMapping
    public ResponseEntity<TableInvoice> create(@RequestBody TableInvoiceRequest req) {
        return ResponseEntity.ok(service.save(req));
    }

    /** List invoices by date range */
    @GetMapping
    public ResponseEntity<List<TableInvoice>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);
        return ResponseEntity.ok(service.findByDateRange(start, end));
    }

    /** Revenue summary for a date range */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);
        return ResponseEntity.ok(service.getSummary(start, end));
    }

    /** Single invoice detail */
    @GetMapping("/{id}")
    public ResponseEntity<TableInvoice> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
