package com.ledgepro.controller;

import com.ledgepro.model.Customer;
import com.ledgepro.service.CustomerService;
import com.ledgepro.util.GenericResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public GenericResponse<List<Customer>> getCustomers(
            @RequestParam(defaultValue = "1") Long businessId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean outstandingOnly) {

        List<Customer> customers;
        if (outstandingOnly) {
            customers = customerService.getCustomersWithOutstanding(businessId);
        } else if (search != null && !search.trim().isEmpty()) {
            customers = customerService.searchCustomers(businessId, search);
        } else {
            customers = customerService.getAllCustomers(businessId);
        }
        return GenericResponse.success(customers);
    }

    @GetMapping("/{id}")
    public GenericResponse<Customer> getCustomerById(@PathVariable Long id) {
        return GenericResponse.success(customerService.getCustomerById(id));
    }

    @PostMapping
    public GenericResponse<Customer> createCustomer(@RequestBody Customer customer) {
        if (customer.getBusinessId() == null) customer.setBusinessId(1L);
        Customer created = customerService.createCustomer(customer);
        return GenericResponse.success(created, "Customer profile created successfully");
    }

    @PutMapping("/{id}")
    public GenericResponse<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        if (customer.getBusinessId() == null) customer.setBusinessId(1L);
        Customer updated = customerService.updateCustomer(customer);
        return GenericResponse.success(updated, "Customer profile updated successfully");
    }

    @DeleteMapping("/{id}")
    public GenericResponse<Boolean> deleteCustomer(@PathVariable Long id) {
        boolean deleted = customerService.deleteCustomer(id);
        return GenericResponse.success(deleted, "Customer account deactivated/deleted successfully");
    }
}
