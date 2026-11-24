package com.ledgepro.controller;

import com.ledgepro.concurrency.ReportThreadPoolManager;
import com.ledgepro.model.Product;
import com.ledgepro.service.ProductService;
import com.ledgepro.util.CsvUtils;
import com.ledgepro.util.GenericResponse;
import com.ledgepro.util.TenantUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    @GetMapping("/products/csv")
    public ResponseEntity<String> exportProductsCsv(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId) 
            throws ExecutionException, InterruptedException {

        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        Future<String> exportFuture = threadPoolManager.submitTask(() -> {
            List<Product> products = productService.getAllProducts(targetBizId);
            return CsvUtils.exportProductsToCsv(products);
        });

        String csvData = exportFuture.get();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products-inventory-" + System.currentTimeMillis() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @PostMapping("/products/csv/import")
    public GenericResponse<Integer> importProductsCsv(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId) throws IOException {

        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        List<Product> parsed = CsvUtils.parseProductCsv(file.getInputStream(), targetBizId);
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
