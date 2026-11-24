package com.ledgepro.controller;

import com.ledgepro.model.Product;
import com.ledgepro.service.ProductService;
import com.ledgepro.util.GenericResponse;
import com.ledgepro.util.TenantUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public GenericResponse<List<Product>> getProducts(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {

        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        List<Product> products;
        if (search != null || categoryId != null) {
            products = productService.searchProducts(targetBizId, search, categoryId);
        } else if (sortBy != null) {
            products = productService.getSortedProducts(targetBizId, sortBy, ascending);
        } else {
            products = productService.getAllProducts(targetBizId);
        }
        return GenericResponse.success(products);
    }

    @GetMapping("/{id}")
    public GenericResponse<Product> getProductById(@PathVariable Long id) {
        return GenericResponse.success(productService.getProductById(id));
    }

    @GetMapping("/low-stock")
    public GenericResponse<List<Product>> getLowStockProducts(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        return GenericResponse.success(productService.getLowStockProducts(targetBizId));
    }

    @PostMapping
    public GenericResponse<Product> createProduct(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestBody Product product) {
        if (product.getBusinessId() == null || product.getBusinessId() <= 0) {
            product.setBusinessId(TenantUtil.resolveBusinessId(null, headerBizId));
        }
        Product created = productService.createProduct(product);
        return GenericResponse.success(created, "Product created successfully");
    }

    @PutMapping("/{id}")
    public GenericResponse<Product> updateProduct(
            @PathVariable Long id,
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestBody Product product) {
        product.setId(id);
        if (product.getBusinessId() == null || product.getBusinessId() <= 0) {
            product.setBusinessId(TenantUtil.resolveBusinessId(null, headerBizId));
        }
        Product updated = productService.updateProduct(product);
        return GenericResponse.success(updated, "Product updated successfully");
    }

    @DeleteMapping("/{id}")
    public GenericResponse<Boolean> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return GenericResponse.success(deleted, "Product deleted successfully");
    }
}
