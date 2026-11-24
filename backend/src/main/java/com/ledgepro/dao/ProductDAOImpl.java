package com.ledgepro.dao;

import com.ledgepro.config.DatabaseConnectionFactory;
import com.ledgepro.exception.DatabaseOperationException;
import com.ledgepro.model.Product;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductDAOImpl implements ProductDAO {

    private final DatabaseConnectionFactory connectionFactory;

    public ProductDAOImpl(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Product> findById(Long id) {
        String sql = """
            SELECT p.*, c.name as category_name, s.name as supplier_name 
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            LEFT JOIN suppliers s ON p.supplier_id = s.id 
            WHERE p.id = ?
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching product by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> findBySku(Long businessId, String sku) {
        String sql = """
            SELECT p.*, c.name as category_name, s.name as supplier_name 
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            LEFT JOIN suppliers s ON p.supplier_id = s.id 
            WHERE p.business_id = ? AND p.sku = ?
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            ps.setString(2, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching product by SKU: " + sku, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll(Long businessId) {
        String sql = """
            SELECT p.*, c.name as category_name, s.name as supplier_name 
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            LEFT JOIN suppliers s ON p.supplier_id = s.id 
            WHERE p.business_id = ? 
            ORDER BY p.name ASC
        """;
        List<Product> products = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all products", e);
        }
        return products;
    }

    @Override
    public List<Product> search(Long businessId, String query, Long categoryId) {
        StringBuilder sql = new StringBuilder("""
            SELECT p.*, c.name as category_name, s.name as supplier_name 
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            LEFT JOIN suppliers s ON p.supplier_id = s.id 
            WHERE p.business_id = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(businessId);

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.sku) LIKE ? OR p.barcode LIKE ?)");
            String pattern = "%" + query.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }

        sql.append(" ORDER BY p.name ASC");

        List<Product> products = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error searching products", e);
        }
        return products;
    }

    @Override
    public List<Product> findLowStock(Long businessId) {
        String sql = """
            SELECT p.*, c.name as category_name, s.name as supplier_name 
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            LEFT JOIN suppliers s ON p.supplier_id = s.id 
            WHERE p.business_id = ? AND p.quantity <= p.min_stock_level 
            ORDER BY p.quantity ASC
        """;
        List<Product> products = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching low stock products", e);
        }
        return products;
    }

    @Override
    public int getLowStockCount(Long businessId) {
        String sql = "SELECT COUNT(*) FROM products WHERE business_id = ? AND quantity <= min_stock_level";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching low stock count", e);
        }
        return 0;
    }

    @Override
    public int getTotalProductCount(Long businessId) {
        String sql = "SELECT COUNT(*) FROM products WHERE business_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching product count", e);
        }
        return 0;
    }

    @Override
    public Product save(Product product) {
        String sql = """
            INSERT INTO products (business_id, sku, barcode, name, category_id, supplier_id, 
                                 cost_price, selling_price, quantity, min_stock_level, description, is_active, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, product.getBusinessId());
            ps.setString(2, product.getSku());
            ps.setString(3, product.getBarcode());
            ps.setString(4, product.getName());
            if (product.getCategoryId() != null) ps.setLong(5, product.getCategoryId()); else ps.setNull(5, Types.BIGINT);
            if (product.getSupplierId() != null) ps.setLong(6, product.getSupplierId()); else ps.setNull(6, Types.BIGINT);
            ps.setBigDecimal(7, product.getCostPrice());
            ps.setBigDecimal(8, product.getSellingPrice());
            ps.setInt(9, product.getQuantity());
            ps.setInt(10, product.getMinStockLevel());
            ps.setString(11, product.getDescription());
            ps.setBoolean(12, product.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        product.setId(keys.getLong(1));
                    }
                }
            }
            return product;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error saving product: " + product.getName(), e);
        }
    }

    @Override
    public boolean update(Product product) {
        String sql = """
            UPDATE products 
            SET sku = ?, barcode = ?, name = ?, category_id = ?, supplier_id = ?, 
                cost_price = ?, selling_price = ?, quantity = ?, min_stock_level = ?, 
                description = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE id = ? AND business_id = ?
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getSku());
            ps.setString(2, product.getBarcode());
            ps.setString(3, product.getName());
            if (product.getCategoryId() != null) ps.setLong(4, product.getCategoryId()); else ps.setNull(4, Types.BIGINT);
            if (product.getSupplierId() != null) ps.setLong(5, product.getSupplierId()); else ps.setNull(5, Types.BIGINT);
            ps.setBigDecimal(6, product.getCostPrice());
            ps.setBigDecimal(7, product.getSellingPrice());
            ps.setInt(8, product.getQuantity());
            ps.setInt(9, product.getMinStockLevel());
            ps.setString(10, product.getDescription());
            ps.setBoolean(11, product.isActive());
            ps.setLong(12, product.getId());
            ps.setLong(13, product.getBusinessId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating product: " + product.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting product: " + id, e);
        }
    }

    @Override
    public boolean updateQuantity(Connection conn, Long productId, int quantityChange) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityChange);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setBusinessId(rs.getLong("business_id"));
        p.setSku(rs.getString("sku"));
        p.setBarcode(rs.getString("barcode"));
        p.setName(rs.getString("name"));

        long catId = rs.getLong("category_id");
        if (!rs.wasNull()) {
            p.setCategoryId(catId);
        }
        p.setCategoryName(rs.getString("category_name"));

        long supId = rs.getLong("supplier_id");
        if (!rs.wasNull()) {
            p.setSupplierId(supId);
        }
        p.setSupplierName(rs.getString("supplier_name"));

        p.setCostPrice(rs.getBigDecimal("cost_price"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setMinStockLevel(rs.getInt("min_stock_level"));
        p.setDescription(rs.getString("description"));
        p.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) p.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) p.setUpdatedAt(updatedAt.toLocalDateTime());

        return p;
    }
}
