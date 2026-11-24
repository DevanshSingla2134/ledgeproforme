package com.ledgepro.controller;

import com.ledgepro.model.Supplier;
import com.ledgepro.service.SupplierService;
import com.ledgepro.util.GenericResponse;
import com.ledgepro.util.TenantUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public GenericResponse<List<Supplier>> getSuppliers(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) String search) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        List<Supplier> suppliers;
        if (search != null && !search.trim().isEmpty()) {
            suppliers = supplierService.searchSuppliers(targetBizId, search);
        } else {
            suppliers = supplierService.getAllSuppliers(targetBizId);
        }
        return GenericResponse.success(suppliers);
    }

    @GetMapping("/{id}")
    public GenericResponse<Supplier> getSupplierById(@PathVariable Long id) {
        return GenericResponse.success(supplierService.getSupplierById(id));
    }

    @PostMapping
    public GenericResponse<Supplier> createSupplier(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestBody Supplier supplier) {
        if (supplier.getBusinessId() == null || supplier.getBusinessId() <= 0) {
            supplier.setBusinessId(TenantUtil.resolveBusinessId(null, headerBizId));
        }
        return GenericResponse.success(supplierService.createSupplier(supplier), "Supplier registered successfully");
    }

    @PutMapping("/{id}")
    public GenericResponse<Supplier> updateSupplier(
            @PathVariable Long id,
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestBody Supplier supplier) {
        supplier.setId(id);
        if (supplier.getBusinessId() == null || supplier.getBusinessId() <= 0) {
            supplier.setBusinessId(TenantUtil.resolveBusinessId(null, headerBizId));
        }
        return GenericResponse.success(supplierService.updateSupplier(supplier), "Supplier updated successfully");
    }

    @DeleteMapping("/{id}")
    public GenericResponse<Boolean> deleteSupplier(@PathVariable Long id) {
        return GenericResponse.success(supplierService.deleteSupplier(id), "Supplier deleted successfully");
    }
}
