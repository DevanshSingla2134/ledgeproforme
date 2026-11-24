package com.ledgepro.controller;

import com.ledgepro.model.Invoice;
import com.ledgepro.service.POSBillingService;
import com.ledgepro.util.GenericResponse;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class POSBillingController {

    private final POSBillingService billingService;

    public POSBillingController(POSBillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public GenericResponse<List<Invoice>> getAllInvoices(@RequestParam(defaultValue = "1") Long businessId) {
        List<Invoice> invoices = billingService.getAllInvoices(businessId);
        return GenericResponse.success(invoices);
    }

    @GetMapping("/{id}")
    public GenericResponse<Invoice> getInvoiceById(@PathVariable Long id) {
        Invoice invoice = billingService.getInvoiceById(id);
        return GenericResponse.success(invoice);
    }

    @GetMapping("/number/{invoiceNumber}")
    public GenericResponse<Invoice> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        Invoice invoice = billingService.getInvoiceByNumber(invoiceNumber);
        return GenericResponse.success(invoice);
    }

    @PostMapping("/checkout")
    public GenericResponse<Invoice> checkout(@RequestBody Invoice invoiceRequest) throws SQLException {
        if (invoiceRequest.getBusinessId() == null) {
            invoiceRequest.setBusinessId(1L);
        }
        Invoice generatedInvoice = billingService.checkout(invoiceRequest);
        return GenericResponse.success(generatedInvoice, "Invoice generated and atomic billing transaction committed successfully.");
    }
}
