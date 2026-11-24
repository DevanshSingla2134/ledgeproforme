package com.ledgepro.service;

import com.ledgepro.dao.CustomerDAO;
import com.ledgepro.exception.CustomerNotFoundException;
import com.ledgepro.model.Customer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public Customer getCustomerById(Long id) {
        return customerDAO.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public List<Customer> getAllCustomers(Long businessId) {
        return customerDAO.findAll(businessId);
    }

    public List<Customer> searchCustomers(Long businessId, String query) {
        return customerDAO.search(businessId, query);
    }

    public List<Customer> getCustomersWithOutstanding(Long businessId) {
        return customerDAO.findWithOutstandingBalance(businessId);
    }

    public Customer createCustomer(Customer customer) {
        validateCustomer(customer);
        if (customer.getOpeningBalance() == null) {
            customer.setOpeningBalance(BigDecimal.ZERO);
        }
        customer.setCurrentBalance(customer.getOpeningBalance());
        return customerDAO.save(customer);
    }

    public Customer updateCustomer(Customer customer) {
        validateCustomer(customer);
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer ID cannot be null for updates.");
        }
        boolean updated = customerDAO.update(customer);
        if (!updated) {
            throw new CustomerNotFoundException(customer.getId());
        }
        return customer;
    }

    public boolean deleteCustomer(Long id) {
        return customerDAO.deleteById(id);
    }

    private void validateCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer phone number is required.");
        }
    }
}
