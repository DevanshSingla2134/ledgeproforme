package com.ledgepro.service;

import com.ledgepro.dao.SupplierDAO;
import com.ledgepro.model.Supplier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierDAO supplierDAO;

    public SupplierService(SupplierDAO supplierDAO) {
        this.supplierDAO = supplierDAO;
    }

    public List<Supplier> getAllSuppliers(Long businessId) {
        return supplierDAO.findAll(businessId);
    }

    public Supplier getSupplierById(Long id) {
        return supplierDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supplier with ID " + id + " not found."));
    }

    public List<Supplier> searchSuppliers(Long businessId, String query) {
        return supplierDAO.search(businessId, query);
    }

    public Supplier createSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name is required.");
        }
        if (supplier.getPhone() == null || supplier.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier phone is required.");
        }
        return supplierDAO.save(supplier);
    }

    public Supplier updateSupplier(Supplier supplier) {
        supplierDAO.update(supplier);
        return supplier;
    }

    public boolean deleteSupplier(Long id) {
        return supplierDAO.deleteById(id);
    }
}
