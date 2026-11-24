package com.ledgepro.controller;

import com.ledgepro.concurrency.ReportThreadPoolManager;
import com.ledgepro.model.Product;
import com.ledgepro.service.ProductService;
import com.ledgepro.util.CsvUtils;
import com.ledgepro.util.GenericResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Controller demonstrating:
 * - Asynchronous multithreaded processing with ExecutorService & Future
 * - File I/O with BufferedReader & BufferedWriter
 */
@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {

    private final ProductService productService;
    private final ReportThreadPoolManager threadPoolManager;

    public ExportController(ProductService productService, ReportThreadPoolManager threadPoolManager) {
        this.productService = productService;
        this.threadPoolManager = threadPoolManager;
    }

    /**
     * Multithreaded asynchronous CSV export:
     * Generates CSV in a separate worker thread using Callable & Future
     */
    @GetMapping("/products/csv")
    public ResponseEntity<String> exportProductsCsv(@RequestParam(defaultValue = "1") Long businessId) 
            throws ExecutionException, InterruptedException {

        Future<String> exportFuture = threadPoolManager.submitTask(() -> {
            List<Product> products = productService.getAllProducts(businessId);
            return CsvUtils.exportProductsToCsv(products);
        });

        String csvData = exportFuture.get(); // Await result from concurrency worker thread

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products-inventory-" + System.currentTimeMillis() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    /**
     * CSV file upload & import
     */
    @PostMapping("/products/csv/import")
    public GenericResponse<Integer> importProductsCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "1") Long businessId) throws IOException {

        List<Product> parsed = CsvUtils.parseProductCsv(file.getInputStream(), businessId);
        int successCount = 0;
        for (Product p : parsed) {
            try {
                productService.createProduct(p);
                successCount++;
            } catch (Exception e) {
                // Continue with remaining records
            }
        }
        return GenericResponse.success(successCount, "Successfully imported " + successCount + " products from CSV.");
    }
}
