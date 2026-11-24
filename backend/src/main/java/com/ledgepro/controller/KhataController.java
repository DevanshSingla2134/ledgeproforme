package com.ledgepro.controller;

import com.ledgepro.model.LedgerEntry;
import com.ledgepro.model.Payment;
import com.ledgepro.service.KhataLedgerService;
import com.ledgepro.util.GenericResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/khata")
@CrossOrigin(origins = "*")
public class KhataController {

    private final KhataLedgerService khataLedgerService;

    public KhataController(KhataLedgerService khataLedgerService) {
        this.khataLedgerService = khataLedgerService;
    }

    @GetMapping("/customer/{customerId}")
    public GenericResponse<List<LedgerEntry>> getCustomerTimeline(@PathVariable Long customerId) {
        List<LedgerEntry> timeline = khataLedgerService.getCustomerLedgerTimeline(customerId);
        return GenericResponse.success(timeline);
    }

    @GetMapping("/activity")
    public GenericResponse<List<LedgerEntry>> getRecentActivity(
            @RequestParam(defaultValue = "1") Long businessId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LedgerEntry> activity = khataLedgerService.getRecentLedgerActivity(businessId, limit);
        return GenericResponse.success(activity);
    }

    @PostMapping("/payment")
    public GenericResponse<Payment> recordKhataPayment(@RequestBody Map<String, Object> payload) throws SQLException {
        Long businessId = payload.get("businessId") != null ? Long.parseLong(payload.get("businessId").toString()) : 1L;
        Long customerId = Long.parseLong(payload.get("customerId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String paymentMethod = payload.get("paymentMethod") != null ? payload.get("paymentMethod").toString() : "CASH";
        String referenceNumber = payload.get("referenceNumber") != null ? payload.get("referenceNumber").toString() : "";
        String notes = payload.get("notes") != null ? payload.get("notes").toString() : "Khata debt settlement";

        Payment payment = khataLedgerService.recordPayment(businessId, customerId, amount, paymentMethod, referenceNumber, notes);
        return GenericResponse.success(payment, "Khata payment received and balance recalculated successfully.");
    }

    @PostMapping("/adjustment")
    public GenericResponse<LedgerEntry> recordAdjustment(@RequestBody Map<String, Object> payload) throws SQLException {
        Long businessId = payload.get("businessId") != null ? Long.parseLong(payload.get("businessId").toString()) : 1L;
        Long customerId = Long.parseLong(payload.get("customerId").toString());
        String entryType = payload.get("entryType").toString();
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String description = payload.get("description").toString();

        LedgerEntry entry = khataLedgerService.recordAdjustment(businessId, customerId, entryType, amount, description);
        return GenericResponse.success(entry, "Adjustment recorded in customer ledger.");
    }

    @GetMapping("/customer/{customerId}/statement")
    public ResponseEntity<String> downloadCustomerStatement(@PathVariable Long customerId) throws IOException {
        String csvContent = khataLedgerService.generateCustomerStatementCsv(customerId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=khata-statement-" + customerId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvContent);
    }
}
