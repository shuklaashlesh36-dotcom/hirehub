package com.hirehub.dao;

import java.util.List;
import java.util.Optional;

/**
 * Common persistence operations. JDBC implementations will be added in a later phase.
 */
public interface GenericDao<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    T save(T entity);

    void update(T entity);

    void delete(ID id);
}
