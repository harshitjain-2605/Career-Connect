package com.campus.placement.dao;

import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Company;

import java.util.List;
import java.util.Optional;

/**
 * DAO contract for {@link Company} entities.
 */
public interface CompanyDao extends Dao<Company, Integer> {

    Optional<Company> findByName(String name) throws DataAccessException;

    List<Company> search(String keyword) throws DataAccessException;
}
