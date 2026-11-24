package com.ledgepro;

import com.ledgepro.dao.CustomerDAO;
import com.ledgepro.dao.InvoiceDAO;
import com.ledgepro.dao.ProductDAO;
import com.ledgepro.exception.InsufficientStockException;
import com.ledgepro.exception.InvalidPaymentException;
import com.ledgepro.model.Customer;
import com.ledgepro.model.Invoice;
import com.ledgepro.model.InvoiceItem;
import com.ledgepro.model.Product;
import com.ledgepro.service.CustomerService;
import com.ledgepro.service.KhataLedgerService;
import com.ledgepro.service.POSBillingService;
import com.ledgepro.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BusinessOperationsTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private POSBillingService billingService;

    @Autowired
    private KhataLedgerService khataLedgerService;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CustomerDAO customerDAO;

    @Autowired
    private InvoiceDAO invoiceDAO;

    @Autowired
    private com.ledgepro.dao.UserDAO userDAO;

    @Test
    @DisplayName("Test 1: Product Creation and Retrieval")
    public void testProductLifecycle() {
        String uniqueSku = "TEST-SKU-" + UUID.randomUUID().toString().substring(0, 6);
        Product p = new Product();
        p.setBusinessId(1L);
        p.setSku(uniqueSku);
        p.setName("Test Wireless Optical Mouse");
        p.setCostPrice(new BigDecimal("250.00"));
        p.setSellingPrice(new BigDecimal("399.00"));
        p.setQuantity(25);
        p.setMinStockLevel(5);

        Product created = productService.createProduct(p);
        assertNotNull(created.getId(), "Created product must have an auto-generated primary key");

        Product retrieved = productService.getProductById(created.getId());
        assertEquals("Test Wireless Optical Mouse", retrieved.getName());
        assertEquals(25, retrieved.getQuantity());
    }

    @Test
    @DisplayName("Test 2: Complete Atomic POS Billing & Inventory Stock Deduction")
    public void testPOSBillingAndStockDeduction() throws SQLException {
        // 1. Create a product with known quantity = 30
        String sku = "POS-TEST-" + UUID.randomUUID().toString().substring(0, 6);
        Product p = new Product();
        p.setBusinessId(1L);
        p.setSku(sku);
        p.setName("Haldiram Bhujia Sev 1kg");
        p.setCostPrice(new BigDecimal("180.00"));
        p.setSellingPrice(new BigDecimal("220.00"));
        p.setQuantity(30);
        p.setMinStockLevel(5);
        Product saved = productService.createProduct(p);

        // 2. Perform POS Checkout for 4 units
        Invoice invoice = new Invoice();
        invoice.setBusinessId(1L);
        invoice.setPaymentMethod("CASH");

        InvoiceItem item = new InvoiceItem();
        item.setProductId(saved.getId());
        item.setQuantity(4);
        item.setUnitPrice(new BigDecimal("220.00"));
        invoice.setItems(Collections.singletonList(item));

        Invoice completedInvoice = billingService.checkout(invoice);
        assertNotNull(completedInvoice.getId());
        assertEquals(new BigDecimal("880.00"), completedInvoice.getGrandTotal());

        // 3. Verify that stock was decremented from 30 to 26
        Product afterSale = productDAO.findById(saved.getId()).orElseThrow();
        assertEquals(26, afterSale.getQuantity(), "Inventory stock must be reduced by sold quantity");
    }

    @Test
    @DisplayName("Test 3: Insufficient Stock Prevention & Transaction Safety")
    public void testInsufficientStockThrowsException() {
        // Create product with quantity = 2
        String sku = "LOW-STOCK-" + UUID.randomUUID().toString().substring(0, 6);
        Product p = new Product();
        p.setBusinessId(1L);
        p.setSku(sku);
        p.setName("Limited Edition Headphones");
        p.setCostPrice(new BigDecimal("800.00"));
        p.setSellingPrice(new BigDecimal("1200.00"));
        p.setQuantity(2);
        p.setMinStockLevel(1);
        Product saved = productService.createProduct(p);

        // Attempt to bill 5 units (exceeds 2 available)
        Invoice invoice = new Invoice();
        invoice.setBusinessId(1L);
        invoice.setPaymentMethod("CASH");

        InvoiceItem item = new InvoiceItem();
        item.setProductId(saved.getId());
        item.setQuantity(5);
        item.setUnitPrice(new BigDecimal("1200.00"));
        invoice.setItems(Collections.singletonList(item));

        assertThrows(InsufficientStockException.class, () -> {
            billingService.checkout(invoice);
        }, "Selling more than available stock must throw InsufficientStockException and abort transaction");

        // Verify stock was NOT deducted
        Product check = productDAO.findById(saved.getId()).orElseThrow();
        assertEquals(2, check.getQuantity(), "Stock must remain intact when transaction is rejected");
    }

    @Test
    @DisplayName("Test 4: Digital Khata Credit Sale & Customer Balance Recalculation")
    public void testKhataCreditSaleAndPaymentSettlement() throws SQLException {
        // 1. Create a customer with initial balance 0
        Customer cust = new Customer();
        cust.setBusinessId(1L);
        cust.setName("Test Khata Customer " + UUID.randomUUID().toString().substring(0, 4));
        cust.setPhone("+91 99999 " + (int)(Math.random() * 89999 + 10000));
        cust.setOpeningBalance(BigDecimal.ZERO);
        Customer createdCust = customerService.createCustomer(cust);

        // 2. Create product for sale
        Product p = new Product();
        p.setBusinessId(1L);
        p.setSku("KHATA-PRD-" + UUID.randomUUID().toString().substring(0, 6));
        p.setName("Fortune Mustard Oil 1L");
        p.setCostPrice(new BigDecimal("130.00"));
        p.setSellingPrice(new BigDecimal("160.00"));
        p.setQuantity(50);
        Product savedPrd = productService.createProduct(p);

        // 3. Bill on Khata / Udhaar (paymentMethod = KHATA, paidAmount = 0)
        Invoice invoice = new Invoice();
        invoice.setBusinessId(1L);
        invoice.setCustomerId(createdCust.getId());
        invoice.setPaymentMethod("KHATA");
        invoice.setPaidAmount(BigDecimal.ZERO);

        InvoiceItem item = new InvoiceItem();
        item.setProductId(savedPrd.getId());
        item.setQuantity(5); // 5 * 160 = 800
        item.setUnitPrice(new BigDecimal("160.00"));
        invoice.setItems(Collections.singletonList(item));

        billingService.checkout(invoice);

        // 4. Verify Customer's current balance increased to 800.00
        Customer afterCredit = customerService.getCustomerById(createdCust.getId());
        assertEquals(new BigDecimal("800.00"), afterCredit.getCurrentBalance(), "Udhaar purchase must increase balance");

        // 5. Customer makes partial settlement payment of 300.00
        khataLedgerService.recordPayment(1L, createdCust.getId(), new BigDecimal("300.00"), "UPI", "UPI-TEST-123", "Part payment");

        // 6. Verify Customer's balance is now exactly 500.00
        Customer afterPayment = customerService.getCustomerById(createdCust.getId());
        assertEquals(new BigDecimal("500.00"), afterPayment.getCurrentBalance(), "Payment must deduct from balance");
    }

    @Test
    @DisplayName("Test 5: Khata Sale Requires Registered Customer")
    public void testKhataRequiresCustomer() {
        Invoice invoice = new Invoice();
        invoice.setBusinessId(1L);
        invoice.setPaymentMethod("KHATA"); // No customer ID specified
        invoice.setCustomerId(null);

        InvoiceItem item = new InvoiceItem();
        item.setProductName("Miscellaneous item");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("100.00"));
        invoice.setItems(Collections.singletonList(item));

        assertThrows(InvalidPaymentException.class, () -> {
            billingService.checkout(invoice);
        }, "Credit/Khata sale without customer must be rejected");
    }

    @Test
    @DisplayName("Test 6: Multi-Tenant Business Data Isolation — New User Starts Fresh with No Data Leakage")
    public void testMultiTenantDataIsolation() {
        // Business 1 (Sharma Supermart) has seeded products
        java.util.List<Product> b1Products = productService.getAllProducts(1L);
        assertFalse(b1Products.isEmpty(), "Business 1 must have pre-seeded demo inventory");

        // Simulate a newly registered merchant with their own isolated business row
        com.ledgepro.model.Business createdBiz = userDAO.createBusinessForUser(1L, "Sharma Electronics New Branch", "9999999999");
        Long newBizId = createdBiz.getId();
        assertNotNull(newBizId, "New business must have an auto-generated ID");

        java.util.List<Product> freshProducts = productService.getAllProducts(newBizId);
        assertTrue(freshProducts.isEmpty(), "Newly registered business must start with 0 products (zero data leakage)");

        java.util.List<Customer> freshCustomers = customerService.getAllCustomers(newBizId);
        assertTrue(freshCustomers.isEmpty(), "Newly registered business must start with 0 customers");

        // Add a product specifically to the new business
        Product newProduct = new Product();
        newProduct.setBusinessId(newBizId);
        newProduct.setSku("NEWBIZ-" + UUID.randomUUID().toString().substring(0, 5));
        newProduct.setName("Isolated Merchant Product");
        newProduct.setCostPrice(new BigDecimal("50.00"));
        newProduct.setSellingPrice(new BigDecimal("80.00"));
        newProduct.setQuantity(10);
        newProduct.setMinStockLevel(2);
        productService.createProduct(newProduct);

        // Verify only newBizId sees this product
        java.util.List<Product> newBizList = productService.getAllProducts(newBizId);
        assertEquals(1, newBizList.size(), "New business should have exactly 1 product");
        assertEquals("Isolated Merchant Product", newBizList.get(0).getName());

        // Verify Business 1 does NOT see this product
        boolean leakedToB1 = productService.getAllProducts(1L).stream()
                .anyMatch(p -> "Isolated Merchant Product".equals(p.getName()));
        assertFalse(leakedToB1, "Data must NOT leak to Business 1 (Sharma Supermart)");
    }
}
