package com.ledgepro.service;

import com.ledgepro.dao.InvoiceDAO;
import com.ledgepro.dao.ProductDAO;
import com.ledgepro.exception.InsufficientStockException;
import com.ledgepro.exception.InvalidPaymentException;
import com.ledgepro.exception.ProductNotFoundException;
import com.ledgepro.model.Invoice;
import com.ledgepro.model.InvoiceItem;
import com.ledgepro.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class POSBillingService {

    private final InvoiceDAO invoiceDAO;
    private final ProductDAO productDAO;

    public POSBillingService(InvoiceDAO invoiceDAO, ProductDAO productDAO) {
        this.invoiceDAO = invoiceDAO;
        this.productDAO = productDAO;
    }

    public List<Invoice> getAllInvoices(Long businessId) {
        return invoiceDAO.findAll(businessId);
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice with ID " + id + " not found."));
    }

    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceDAO.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invoice " + invoiceNumber + " not found."));
    }

    /**
     * Complete POS checkout workflow:
     * - Validates cart items and stock
     * - Computes accurate subtotal, discounts, GST
     * - Generates unique invoice number
     * - Invokes atomic database transaction
     */
    public Invoice checkout(Invoice invoiceRequest) throws SQLException {
        if (invoiceRequest.getItems() == null || invoiceRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("Billing cart cannot be empty.");
        }

        // 1. Validate & populate items from current inventory
        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceItem item : invoiceRequest.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be greater than zero.");
            }
            if (item.getProductId() != null) {
                Product product = productDAO.findById(item.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

                if (product.getQuantity() < item.getQuantity()) {
                    throw new InsufficientStockException(product.getName(), item.getQuantity(), product.getQuantity());
                }

                item.setProductName(product.getName());
                item.setSku(product.getSku());
                item.setCostPrice(product.getCostPrice());
                if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) == 0) {
                    item.setUnitPrice(product.getSellingPrice());
                }
            }

            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setTotalPrice(itemTotal);
            subtotal = subtotal.add(itemTotal);
        }

        invoiceRequest.setSubtotal(subtotal);

        // 2. Calculate Discount
        BigDecimal discountRate = invoiceRequest.getDiscountRate() != null ? invoiceRequest.getDiscountRate() : BigDecimal.ZERO;
        BigDecimal discountAmount = invoiceRequest.getDiscountAmount();
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) == 0) {
            discountAmount = subtotal.multiply(discountRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        invoiceRequest.setDiscountRate(discountRate);
        invoiceRequest.setDiscountAmount(discountAmount);

        BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
            taxableAmount = BigDecimal.ZERO;
        }

        // 3. Calculate GST Tax
        BigDecimal taxRate = invoiceRequest.getTaxRate() != null ? invoiceRequest.getTaxRate() : BigDecimal.ZERO;
        BigDecimal taxAmount = taxableAmount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        invoiceRequest.setTaxRate(taxRate);
        invoiceRequest.setTaxAmount(taxAmount);

        // 4. Calculate Grand Total
        BigDecimal grandTotal = taxableAmount.add(taxAmount);
        invoiceRequest.setGrandTotal(grandTotal);

        // 5. Payment validation and balance calculation
        String paymentMethod = invoiceRequest.getPaymentMethod() != null ? invoiceRequest.getPaymentMethod().toUpperCase() : "CASH";
        invoiceRequest.setPaymentMethod(paymentMethod);

        BigDecimal paidAmount = invoiceRequest.getPaidAmount();
        if ("KHATA".equals(paymentMethod)) {
            // Customer required for Khata credit sale
            if (invoiceRequest.getCustomerId() == null || invoiceRequest.getCustomerId() <= 0) {
                throw new InvalidPaymentException("A registered customer must be selected for Credit / Khata billing.");
            }
            if (paidAmount == null) {
                paidAmount = BigDecimal.ZERO;
            }
        } else if ("SPLIT".equals(paymentMethod)) {
            if (paidAmount == null) {
                throw new InvalidPaymentException("Paid amount is required for Split payment.");
            }
        } else {
            // Full cash / card / upi payment
            if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) == 0) {
                paidAmount = grandTotal;
            }
        }

        BigDecimal balanceAmount = grandTotal.subtract(paidAmount);
        if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
            balanceAmount = BigDecimal.ZERO;
        }

        invoiceRequest.setPaidAmount(paidAmount);
        invoiceRequest.setBalanceAmount(balanceAmount);

        // Status
        if (balanceAmount.compareTo(BigDecimal.ZERO) == 0) {
            invoiceRequest.setStatus("PAID");
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoiceRequest.setStatus("PARTIAL");
        } else {
            invoiceRequest.setStatus("UNPAID");
        }

        // 6. Generate Unique Invoice Number if not provided
        if (invoiceRequest.getInvoiceNumber() == null || invoiceRequest.getInvoiceNumber().trim().isEmpty()) {
            String timePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            invoiceRequest.setInvoiceNumber("INV-" + timePrefix + "-" + randomSuffix);
        }

        // 7. Execute Atomic Transaction in DAO
        return invoiceDAO.executeAtomicBillingTransaction(invoiceRequest);
    }
}
