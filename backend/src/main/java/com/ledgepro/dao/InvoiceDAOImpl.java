package com.ledgepro.dao;

import com.ledgepro.config.DatabaseConnectionFactory;
import com.ledgepro.exception.DatabaseOperationException;
import com.ledgepro.exception.DuplicateInvoiceException;
import com.ledgepro.exception.InsufficientStockException;
import com.ledgepro.exception.LedgeProException;
import com.ledgepro.model.Invoice;
import com.ledgepro.model.InvoiceItem;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InvoiceDAOImpl implements InvoiceDAO {

    private final DatabaseConnectionFactory connectionFactory;

    public InvoiceDAOImpl(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        String sql = """
            SELECT i.*, c.name as customer_name, c.phone as customer_phone 
            FROM invoices i 
            LEFT JOIN customers c ON i.customer_id = c.id 
            WHERE i.id = ?
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice invoice = mapResultSetToInvoice(rs);
                    invoice.setItems(findItemsByInvoiceId(invoice.getId()));
                    return Optional.of(invoice);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding invoice by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        String sql = """
            SELECT i.*, c.name as customer_name, c.phone as customer_phone 
            FROM invoices i 
            LEFT JOIN customers c ON i.customer_id = c.id 
            WHERE i.invoice_number = ?
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice invoice = mapResultSetToInvoice(rs);
                    invoice.setItems(findItemsByInvoiceId(invoice.getId()));
                    return Optional.of(invoice);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding invoice by number: " + invoiceNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Invoice> findAll(Long businessId) {
        String sql = """
            SELECT i.*, c.name as customer_name, c.phone as customer_phone 
            FROM invoices i 
            LEFT JOIN customers c ON i.customer_id = c.id 
            WHERE i.business_id = ? 
            ORDER BY i.id DESC
        """;
        List<Invoice> invoices = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapResultSetToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all invoices", e);
        }
        return invoices;
    }

    @Override
    public List<Invoice> findRecent(Long businessId, int limit) {
        String sql = """
            SELECT i.*, c.name as customer_name, c.phone as customer_phone 
            FROM invoices i 
            LEFT JOIN customers c ON i.customer_id = c.id 
            WHERE i.business_id = ? 
            ORDER BY i.id DESC 
            LIMIT ?
        """;
        List<Invoice> invoices = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapResultSetToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching recent invoices", e);
        }
        return invoices;
    }

    @Override
    public List<Invoice> findByCustomer(Long businessId, Long customerId) {
        String sql = """
            SELECT i.*, c.name as customer_name, c.phone as customer_phone 
            FROM invoices i 
            LEFT JOIN customers c ON i.customer_id = c.id 
            WHERE i.business_id = ? AND i.customer_id = ? 
            ORDER BY i.id DESC
        """;
        List<Invoice> invoices = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            ps.setLong(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapResultSetToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching invoices for customer: " + customerId, e);
        }
        return invoices;
    }

    @Override
    public List<InvoiceItem> findItemsByInvoiceId(Long invoiceId) {
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ? ORDER BY id ASC";
        List<InvoiceItem> items = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceItem item = new InvoiceItem();
                    item.setId(rs.getLong("id"));
                    item.setInvoiceId(rs.getLong("invoice_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setSku(rs.getString("sku"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCostPrice(rs.getBigDecimal("cost_price"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching items for invoice: " + invoiceId, e);
        }
        return items;
    }

    /**
     * Complete Atomic POS Transaction demonstrating:
     * - setAutoCommit(false)
     * - PreparedStatement batching
     * - Atomic stock deduction with verification
     * - Stock movement audit logging
     * - Khata ledger entry creation for Udhaar/Credit sales
     * - Instant payment registration
     * - commit() and rollback()
     */
    @Override
    public Invoice executeAtomicBillingTransaction(Invoice invoice) throws SQLException {
        Connection conn = null;
        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            // 1. Verify invoice number uniqueness
            String checkInvoiceSql = "SELECT COUNT(*) FROM invoices WHERE invoice_number = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkInvoiceSql)) {
                psCheck.setString(1, invoice.getInvoiceNumber());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new DuplicateInvoiceException(invoice.getInvoiceNumber());
                    }
                }
            }

            // 2. Validate inventory availability & lock / verify stock
            for (InvoiceItem item : invoice.getItems()) {
                if (item.getProductId() != null && item.getProductId() > 0) {
                    String checkStockSql = "SELECT name, quantity, cost_price FROM products WHERE id = ?";
                    try (PreparedStatement psStock = conn.prepareStatement(checkStockSql)) {
                        psStock.setLong(1, item.getProductId());
                        try (ResultSet rs = psStock.executeQuery()) {
                            if (rs.next()) {
                                String prodName = rs.getString("name");
                                int available = rs.getInt("quantity");
                                BigDecimal currentCostPrice = rs.getBigDecimal("cost_price");
                                if (available < item.getQuantity()) {
                                    throw new InsufficientStockException(prodName, item.getQuantity(), available);
                                }
                                if (item.getCostPrice() == null || item.getCostPrice().compareTo(BigDecimal.ZERO) == 0) {
                                    item.setCostPrice(currentCostPrice);
                                }
                            }
                        }
                    }
                }
            }

            // 3. Insert Invoice
            String insertInvoiceSql = """
                INSERT INTO invoices (business_id, invoice_number, customer_id, subtotal, 
                                     discount_rate, discount_amount, tax_rate, tax_amount, 
                                     grand_total, paid_amount, balance_amount, payment_method, 
                                     status, notes, created_at) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
            try (PreparedStatement psInv = conn.prepareStatement(insertInvoiceSql, Statement.RETURN_GENERATED_KEYS)) {
                psInv.setLong(1, invoice.getBusinessId());
                psInv.setString(2, invoice.getInvoiceNumber());
                if (invoice.getCustomerId() != null && invoice.getCustomerId() > 0) {
                    psInv.setLong(3, invoice.getCustomerId());
                } else {
                    psInv.setNull(3, Types.BIGINT);
                }
                psInv.setBigDecimal(4, invoice.getSubtotal());
                psInv.setBigDecimal(5, invoice.getDiscountRate());
                psInv.setBigDecimal(6, invoice.getDiscountAmount());
                psInv.setBigDecimal(7, invoice.getTaxRate());
                psInv.setBigDecimal(8, invoice.getTaxAmount());
                psInv.setBigDecimal(9, invoice.getGrandTotal());
                psInv.setBigDecimal(10, invoice.getPaidAmount());
                psInv.setBigDecimal(11, invoice.getBalanceAmount());
                psInv.setString(12, invoice.getPaymentMethod());
                psInv.setString(13, invoice.getStatus());
                psInv.setString(14, invoice.getNotes());

                psInv.executeUpdate();
                try (ResultSet keys = psInv.getGeneratedKeys()) {
                    if (keys.next()) {
                        invoice.setId(keys.getLong(1));
                    }
                }
            }

            // 4. Insert Invoice Items, Deduct Stock, and Record Stock Movements
            String insertItemSql = """
                INSERT INTO invoice_items (invoice_id, product_id, product_name, sku, quantity, cost_price, unit_price, total_price) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            String updateStockSql = "UPDATE products SET quantity = quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            String insertMovementSql = """
                INSERT INTO stock_movements (business_id, product_id, movement_type, quantity_changed, 
                                            resulting_quantity, reference_type, reference_id, notes, created_at) 
                VALUES (?, ?, 'SALE', ?, (SELECT quantity FROM products WHERE id = ?), 'INVOICE', ?, ?, CURRENT_TIMESTAMP)
            """;

            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psStock = conn.prepareStatement(updateStockSql);
                 PreparedStatement psMove = conn.prepareStatement(insertMovementSql)) {

                for (InvoiceItem item : invoice.getItems()) {
                    item.setInvoiceId(invoice.getId());
                    psItem.setLong(1, invoice.getId());
                    if (item.getProductId() != null) psItem.setLong(2, item.getProductId()); else psItem.setNull(2, Types.BIGINT);
                    psItem.setString(3, item.getProductName());
                    psItem.setString(4, item.getSku());
                    psItem.setInt(5, item.getQuantity());
                    psItem.setBigDecimal(6, item.getCostPrice());
                    psItem.setBigDecimal(7, item.getUnitPrice());
                    psItem.setBigDecimal(8, item.getTotalPrice());
                    psItem.executeUpdate();

                    try (ResultSet keys = psItem.getGeneratedKeys()) {
                        if (keys.next()) {
                            item.setId(keys.getLong(1));
                        }
                    }

                    // Deduct stock if linked to inventory
                    if (item.getProductId() != null && item.getProductId() > 0) {
                        psStock.setInt(1, item.getQuantity());
                        psStock.setLong(2, item.getProductId());
                        psStock.executeUpdate();

                        // Log movement
                        psMove.setLong(1, invoice.getBusinessId());
                        psMove.setLong(2, item.getProductId());
                        psMove.setInt(3, -item.getQuantity());
                        psMove.setLong(4, item.getProductId());
                        psMove.setLong(5, invoice.getId());
                        psMove.setString(6, "POS Billing Sale #" + invoice.getInvoiceNumber());
                        psMove.executeUpdate();
                    }
                }
            }

            // 5. Handle Digital Khata Ledger if credit / balance amount exists
            if (invoice.getCustomerId() != null && invoice.getCustomerId() > 0) {
                BigDecimal creditAmount = invoice.getBalanceAmount();
                if (creditAmount != null && creditAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // Update Customer's current balance (Increase udhaar)
                    String updateCustSql = "UPDATE customers SET current_balance = current_balance + ? WHERE id = ?";
                    try (PreparedStatement psCust = conn.prepareStatement(updateCustSql)) {
                        psCust.setBigDecimal(1, creditAmount);
                        psCust.setLong(2, invoice.getCustomerId());
                        psCust.executeUpdate();
                    }

                    // Insert Khata Ledger entry
                    String insertLedgerSql = """
                        INSERT INTO ledger_entries (business_id, customer_id, entry_type, amount, 
                                                   running_balance, reference_invoice_id, description, created_at) 
                        VALUES (?, ?, 'CREDIT_SALE', ?, 
                               (SELECT current_balance FROM customers WHERE id = ?), ?, ?, CURRENT_TIMESTAMP)
                    """;
                    try (PreparedStatement psLedger = conn.prepareStatement(insertLedgerSql)) {
                        psLedger.setLong(1, invoice.getBusinessId());
                        psLedger.setLong(2, invoice.getCustomerId());
                        psLedger.setBigDecimal(3, creditAmount);
                        psLedger.setLong(4, invoice.getCustomerId());
                        psLedger.setLong(5, invoice.getId());
                        psLedger.setString(6, "Credit Sale billed on Invoice #" + invoice.getInvoiceNumber());
                        psLedger.executeUpdate();
                    }
                }
            }

            // 6. Record Payment if paid amount > 0
            if (invoice.getPaidAmount() != null && invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                String insertPaySql = """
                    INSERT INTO payments (business_id, customer_id, invoice_id, amount, payment_method, 
                                         reference_number, notes, created_at) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
                try (PreparedStatement psPay = conn.prepareStatement(insertPaySql)) {
                    psPay.setLong(1, invoice.getBusinessId());
                    if (invoice.getCustomerId() != null) psPay.setLong(2, invoice.getCustomerId()); else psPay.setNull(2, Types.BIGINT);
                    psPay.setLong(3, invoice.getId());
                    psPay.setBigDecimal(4, invoice.getPaidAmount());
                    psPay.setString(5, invoice.getPaymentMethod());
                    psPay.setString(6, "PAY-" + invoice.getInvoiceNumber());
                    psPay.setString(7, "Settlement for Invoice #" + invoice.getInvoiceNumber());
                    psPay.executeUpdate();
                }
            }

            conn.commit(); // COMMIT TRANSACTION ATOMICALLY
            invoice.setCreatedAt(LocalDateTime.now());
            return invoice;

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // ROLLBACK ON ANY ERROR
                } catch (SQLException rbEx) {
                    // Suppress or log
                }
            }
            if (e instanceof LedgeProException) {
                throw (LedgeProException) e;
            }
            throw new DatabaseOperationException("Billing transaction failed and was rolled back: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // Ignored
                }
            }
        }
    }

    @Override
    public int getTotalOrderCount(Long businessId) {
        String sql = "SELECT COUNT(*) FROM invoices WHERE business_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting orders", e);
        }
        return 0;
    }

    @Override
    public BigDecimal getTotalSalesToday(Long businessId) {
        String sql = """
            SELECT COALESCE(SUM(grand_total), 0) 
            FROM invoices 
            WHERE business_id = ? AND CAST(created_at AS DATE) = CURRENT_DATE
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error calculating total sales today", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalRevenueThisMonth(Long businessId) {
        String sql = """
            SELECT COALESCE(SUM(grand_total), 0) 
            FROM invoices 
            WHERE business_id = ? 
              AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE) 
              AND EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE)
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error calculating monthly revenue", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getEstimatedGrossProfit(Long businessId) {
        String sql = """
            SELECT COALESCE(SUM(ii.total_price - (ii.quantity * ii.cost_price)), 0) 
            FROM invoice_items ii 
            JOIN invoices i ON ii.invoice_id = i.id 
            WHERE i.business_id = ?
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error calculating estimated gross profit", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Invoice save(Invoice entity) {
        throw new UnsupportedOperationException("Use executeAtomicBillingTransaction() for transactional invoice creation.");
    }

    @Override
    public boolean update(Invoice entity) {
        String sql = "UPDATE invoices SET notes = ?, status = ? WHERE id = ? AND business_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getNotes());
            ps.setString(2, entity.getStatus());
            ps.setLong(3, entity.getId());
            ps.setLong(4, entity.getBusinessId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating invoice: " + entity.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM invoices WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting invoice: " + id, e);
        }
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getLong("id"));
        inv.setBusinessId(rs.getLong("business_id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));

        long custId = rs.getLong("customer_id");
        if (!rs.wasNull()) {
            inv.setCustomerId(custId);
        }
        inv.setCustomerName(rs.getString("customer_name"));
        inv.setCustomerPhone(rs.getString("customer_phone"));

        inv.setSubtotal(rs.getBigDecimal("subtotal"));
        inv.setDiscountRate(rs.getBigDecimal("discount_rate"));
        inv.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        inv.setTaxRate(rs.getBigDecimal("tax_rate"));
        inv.setTaxAmount(rs.getBigDecimal("tax_amount"));
        inv.setGrandTotal(rs.getBigDecimal("grand_total"));
        inv.setPaidAmount(rs.getBigDecimal("paid_amount"));
        inv.setBalanceAmount(rs.getBigDecimal("balance_amount"));
        inv.setPaymentMethod(rs.getString("payment_method"));
        inv.setStatus(rs.getString("status"));
        inv.setNotes(rs.getString("notes"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            inv.setCreatedAt(createdAt.toLocalDateTime());
        }
        return inv;
    }
}
