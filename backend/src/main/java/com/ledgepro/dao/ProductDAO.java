package com.ledgepro.dao;

import com.ledgepro.model.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductDAO extends GenericDAO<Product, Long> {
    Optional<Product> findBySku(Long businessId, String sku);
    List<Product> search(Long businessId, String query, Long categoryId);
    List<Product> findLowStock(Long businessId);
    int getLowStockCount(Long businessId);
    boolean updateQuantity(Connection conn, Long productId, int quantityChange) throws SQLException;
    int getTotalProductCount(Long businessId);
}
