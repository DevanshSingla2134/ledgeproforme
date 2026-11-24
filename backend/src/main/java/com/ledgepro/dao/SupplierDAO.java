package com.ledgepro.dao;

import com.ledgepro.model.Supplier;

import java.util.List;

public interface SupplierDAO extends GenericDAO<Supplier, Long> {
    List<Supplier> search(Long businessId, String query);
}
