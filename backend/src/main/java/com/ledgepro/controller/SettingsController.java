package com.ledgepro.controller;

import com.ledgepro.config.DatabaseConnectionFactory;
import com.ledgepro.exception.DatabaseOperationException;
import com.ledgepro.model.Business;
import com.ledgepro.util.GenericResponse;
import com.ledgepro.util.TenantUtil;
import org.springframework.web.bind.annotation.*;

import java.sql.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SettingsController {

    private final DatabaseConnectionFactory connectionFactory;

    public SettingsController(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @GetMapping("/business")
    public GenericResponse<Business> getBusinessProfile(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        String sql = "SELECT * FROM businesses WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, targetBizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Business b = new Business();
                    b.setId(rs.getLong("id"));
                    b.setUserId(rs.getLong("user_id"));
                    b.setName(rs.getString("name"));
                    b.setTagline(rs.getString("tagline"));
                    b.setTaxId(rs.getString("tax_id"));
                    b.setPhone(rs.getString("phone"));
                    b.setEmail(rs.getString("email"));
                    b.setAddress(rs.getString("address"));
                    b.setCurrency(rs.getString("currency"));
                    b.setCurrencySymbol(rs.getString("currency_symbol"));
                    b.setInvoicePrefix(rs.getString("invoice_prefix"));
                    b.setNextInvoiceNumber(rs.getInt("next_invoice_number"));
                    b.setTaxRate(rs.getBigDecimal("tax_rate"));
                    b.setLowStockThreshold(rs.getInt("low_stock_threshold"));
                    return GenericResponse.success(b);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching business settings", e);
        }
        return GenericResponse.error("NOT_FOUND", "Business settings not found.");
    }

    @PutMapping("/business")
    public GenericResponse<Business> updateBusinessProfile(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestBody Business b) {
        if (b.getId() == null || b.getId() <= 0) {
            b.setId(TenantUtil.resolveBusinessId(null, headerBizId));
        }
        String sql = """
            UPDATE businesses 
            SET name = ?, tagline = ?, tax_id = ?, phone = ?, email = ?, 
                address = ?, invoice_prefix = ?, tax_rate = ?, low_stock_threshold = ? 
            WHERE id = ?
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getName());
            ps.setString(2, b.getTagline());
            ps.setString(3, b.getTaxId());
            ps.setString(4, b.getPhone());
            ps.setString(5, b.getEmail());
            ps.setString(6, b.getAddress());
            ps.setString(7, b.getInvoicePrefix());
            ps.setBigDecimal(8, b.getTaxRate());
            ps.setInt(9, b.getLowStockThreshold());
            ps.setLong(10, b.getId());
            ps.executeUpdate();
            return GenericResponse.success(b, "Business settings updated successfully.");
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating business settings", e);
        }
    }
}
