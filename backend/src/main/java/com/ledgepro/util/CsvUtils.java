package com.ledgepro.util;

import com.ledgepro.model.Customer;
import com.ledgepro.model.LedgerEntry;
import com.ledgepro.model.Product;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * File handling utility demonstrating BufferedReader, BufferedWriter,
 * StringWriter, FileReader, FileWriter, and try-with-resources.
 */
public class CsvUtils {

    /**
     * Parse CSV input into a list of Products with strict validation
     */
    public static List<Product> parseProductCsv(InputStream inputStream, Long businessId) throws IOException {
        List<Product> products = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || (lineNumber == 1 && line.toLowerCase().contains("name"))) {
                    continue; // Skip empty lines and header
                }
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length >= 6) {
                    Product product = new Product();
                    product.setBusinessId(businessId);
                    product.setSku(cleanQuotes(parts[0]));
                    product.setName(cleanQuotes(parts[1]));
                    product.setCostPrice(new BigDecimal(cleanQuotes(parts[2])));
                    product.setSellingPrice(new BigDecimal(cleanQuotes(parts[3])));
                    product.setQuantity(Integer.parseInt(cleanQuotes(parts[4])));
                    product.setMinStockLevel(Integer.parseInt(cleanQuotes(parts[5])));
                    if (parts.length > 6) {
                        product.setDescription(cleanQuotes(parts[6]));
                    }
                    products.add(product);
                }
            }
        }
        return products;
    }

    /**
     * Export a list of products to a CSV formatted string
     */
    public static String exportProductsToCsv(List<Product> products) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (BufferedWriter writer = new BufferedWriter(stringWriter)) {
            writer.write("SKU,Product Name,Cost Price,Selling Price,Available Quantity,Min Stock Level,Category,Status\n");
            for (Product p : products) {
                writer.write(String.format("\"%s\",\"%s\",%.2f,%.2f,%d,%d,\"%s\",\"%s\"\n",
                        escape(p.getSku()),
                        escape(p.getName()),
                        p.getCostPrice(),
                        p.getSellingPrice(),
                        p.getQuantity(),
                        p.getMinStockLevel(),
                        escape(p.getCategoryName() != null ? p.getCategoryName() : "General"),
                        p.isActive() ? "ACTIVE" : "INACTIVE"
                ));
            }
            writer.flush();
        }
        return stringWriter.toString();
    }

    /**
     * Export customer ledger timeline statement to CSV
     */
    public static String exportCustomerStatementCsv(Customer customer, List<LedgerEntry> entries) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (BufferedWriter writer = new BufferedWriter(stringWriter)) {
            writer.write("====================================================\n");
            writer.write(String.format("CUSTOMER KHATA STATEMENT: %s (Phone: %s)\n", escape(customer.getName()), escape(customer.getPhone())));
            writer.write(String.format("Current Outstanding Balance: Rs. %.2f\n", customer.getCurrentBalance()));
            writer.write("====================================================\n");
            writer.write("Transaction ID,Date & Time,Type,Description,Amount,Running Balance\n");
            for (LedgerEntry entry : entries) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",%.2f,%.2f\n",
                        entry.getId(),
                        entry.getCreatedAt() != null ? entry.getCreatedAt().toString() : "",
                        entry.getEntryType(),
                        escape(entry.getDescription()),
                        entry.getAmount(),
                        entry.getRunningBalance()
                ));
            }
            writer.flush();
        }
        return stringWriter.toString();
    }

    private static String cleanQuotes(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }
}
