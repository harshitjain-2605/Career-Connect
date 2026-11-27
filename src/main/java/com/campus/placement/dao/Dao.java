package com.campus.placement.dao;

import com.campus.placement.exception.DataAccessException;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object (DAO) contract defining standard CRUD operations.
 *
 * @param <T>  entity type
 * @param <ID> primary key type
 */
// [Unit 1: Interfaces] & [Unit 2: Generics & Optional] & [Unit 4: DAO pattern]
public interface Dao<T, ID> {

    /**
     * Persists a new entity and returns it with its generated identifier.
     *
     * @param entity entity to insert
     * @return persisted entity
     * @throws DataAccessException if database access fails
     */
    T save(T entity) throws DataAccessException;

    /**
     * Finds an entity by its primary key.
     *
     * @param id primary key
     * @return Optional containing the entity if found
     * @throws DataAccessException if database access fails
     */
    Optional<T> findById(ID id) throws DataAccessException;

    /**
     * Retrieves all entities of this type.
     *
     * @return list of entities
     * @throws DataAccessException if database access fails
     */
    List<T> findAll() throws DataAccessException;

    /**
     * Updates an existing entity.
     *
     * @param entity entity with updated fields
     * @return true if a row was updated
     * @throws DataAccessException if database access fails
     */
    boolean update(T entity) throws DataAccessException;

    /**
     * Deletes an entity by its primary key.
     *
     * @param id primary key
     * @return true if a row was deleted
     * @throws DataAccessException if database access fails
     */
    boolean deleteById(ID id) throws DataAccessException;
}
