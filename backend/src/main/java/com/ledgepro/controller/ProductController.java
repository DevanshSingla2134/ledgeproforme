package com.ledgepro.controller;

import com.ledgepro.model.Product;
import com.ledgepro.service.ProductService;
import com.ledgepro.util.GenericResponse;
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
            @RequestParam(defaultValue = "1") Long businessId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {

        List<Product> products;
        if (search != null || categoryId != null) {
            products = productService.searchProducts(businessId, search, categoryId);
        } else if (sortBy != null) {
            products = productService.getSortedProducts(businessId, sortBy, ascending);
        } else {
            products = productService.getAllProducts(businessId);
        }
        return GenericResponse.success(products);
    }

    @GetMapping("/{id}")
    public GenericResponse<Product> getProductById(@PathVariable Long id) {
        return GenericResponse.success(productService.getProductById(id));
    }

    @GetMapping("/low-stock")
    public GenericResponse<List<Product>> getLowStockProducts(@RequestParam(defaultValue = "1") Long businessId) {
        return GenericResponse.success(productService.getLowStockProducts(businessId));
    }

    @PostMapping
    public GenericResponse<Product> createProduct(@RequestBody Product product) {
        if (product.getBusinessId() == null) product.setBusinessId(1L);
        Product created = productService.createProduct(product);
        return GenericResponse.success(created, "Product created successfully");
    }

    @PutMapping("/{id}")
    public GenericResponse<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        if (product.getBusinessId() == null) product.setBusinessId(1L);
        Product updated = productService.updateProduct(product);
        return GenericResponse.success(updated, "Product updated successfully");
    }

    @DeleteMapping("/{id}")
    public GenericResponse<Boolean> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return GenericResponse.success(deleted, "Product deleted successfully");
    }
}
