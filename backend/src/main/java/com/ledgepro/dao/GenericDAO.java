package com.ledgepro.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object interface demonstrating Java Generics.
 * @param <T> Entity type
 * @param <ID> Primary key identifier type
 */
public interface GenericDAO<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll(Long businessId);
    T save(T entity);
    boolean update(T entity);
    boolean deleteById(ID id);
}
