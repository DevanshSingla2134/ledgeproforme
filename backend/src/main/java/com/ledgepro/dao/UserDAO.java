package com.ledgepro.dao;

import com.ledgepro.model.Business;
import com.ledgepro.model.User;

import java.util.Optional;

public interface UserDAO extends GenericDAO<User, Long> {
    Optional<User> findByUsernameOrEmail(String identifier);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Business createBusinessForUser(Long userId, String businessName, String phone);
    Optional<Business> getBusinessByUserId(Long userId);
}
