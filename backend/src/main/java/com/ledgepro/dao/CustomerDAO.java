package com.ledgepro.dao;

import com.ledgepro.model.Customer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO extends GenericDAO<Customer, Long> {
    Optional<Customer> findByPhone(Long businessId, String phone);
    List<Customer> search(Long businessId, String query);
    List<Customer> findWithOutstandingBalance(Long businessId);
    boolean updateBalance(Connection conn, Long customerId, BigDecimal balanceDelta) throws SQLException;
    int getTotalCustomerCount(Long businessId);
    BigDecimal getTotalOutstandingBalance(Long businessId);
}
