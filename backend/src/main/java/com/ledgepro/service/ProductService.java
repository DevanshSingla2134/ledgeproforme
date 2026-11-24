package com.ledgepro.service;

import com.ledgepro.dao.ProductDAO;
import com.ledgepro.exception.ProductNotFoundException;
import com.ledgepro.model.Product;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Product Service demonstrating:
 * - Collections Framework: HashMap cache, ArrayList, Comparator
 * - In-memory fast lookups
 * - Custom Exceptions
 */
@Service
public class ProductService {

    private final ProductDAO productDAO;
    // In-memory catalog cache demonstrating Java Collections HashMap / ConcurrentHashMap
    private final Map<Long, Product> productCache = new ConcurrentHashMap<>();

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public Product getProductById(Long id) {
        if (productCache.containsKey(id)) {
            return productCache.get(id);
        }
        Product product = productDAO.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productCache.put(id, product);
        return product;
    }

    public List<Product> getAllProducts(Long businessId) {
        List<Product> products = productDAO.findAll(businessId);
        // Refresh cache
        for (Product p : products) {
            productCache.put(p.getId(), p);
        }
        return products;
    }

    public List<Product> searchProducts(Long businessId, String query, Long categoryId) {
        return productDAO.search(businessId, query, categoryId);
    }

    public List<Product> getLowStockProducts(Long businessId) {
        return productDAO.findLowStock(businessId);
    }

    public Product createProduct(Product product) {
        validateProduct(product);
        Product saved = productDAO.save(product);
        productCache.put(saved.getId(), saved);
        return saved;
    }

    public Product updateProduct(Product product) {
        validateProduct(product);
        if (product.getId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null for updates.");
        }
        boolean updated = productDAO.update(product);
        if (!updated) {
            throw new ProductNotFoundException(product.getId());
        }
        productCache.put(product.getId(), product);
        return product;
    }

    public boolean deleteProduct(Long id) {
        boolean deleted = productDAO.deleteById(id);
        if (deleted) {
            productCache.remove(id);
        }
        return deleted;
    }

    /**
     * Demonstrates Collections sorting with Comparator
     */
    public List<Product> getSortedProducts(Long businessId, String sortBy, boolean ascending) {
        List<Product> products = new ArrayList<>(getAllProducts(businessId));
        Comparator<Product> comparator = switch (sortBy != null ? sortBy.toLowerCase() : "name") {
            case "price" -> Comparator.comparing(Product::getSellingPrice);
            case "stock" -> Comparator.comparingInt(Product::getQuantity);
            case "cost" -> Comparator.comparing(Product::getCostPrice);
            default -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
        };

        if (!ascending) {
            comparator = comparator.reversed();
        }
        products.sort(comparator);
        return products;
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU is required.");
        }
        if (product.getSellingPrice() == null || product.getSellingPrice().signum() < 0) {
            throw new IllegalArgumentException("Selling price must be non-negative.");
        }
        if (product.getCostPrice() == null || product.getCostPrice().signum() < 0) {
            throw new IllegalArgumentException("Cost price must be non-negative.");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Initial quantity cannot be negative.");
        }
    }
}
