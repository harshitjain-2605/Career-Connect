package com.campus.placement.dao;

import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Offer;
import com.campus.placement.model.OfferStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO contract for {@link Offer} entities and placement aggregate queries.
 */
public interface OfferDao extends Dao<Offer, Integer> {

    Optional<Offer> findByApplicationId(int applicationId) throws DataAccessException;

    List<Offer> findByStudentId(int studentId) throws DataAccessException;

    Optional<Offer> findAcceptedOfferByStudentId(int studentId) throws DataAccessException;

    void updateStatusInTransaction(Connection conn, int offerId, OfferStatus status) throws DataAccessException;

    /**
     * Returns count of distinct placed students grouped by branch.
     */
    Map<String, Integer> countPlacedStudentsByBranch() throws DataAccessException;

    /**
     * Returns count of offers grouped by company name.
     */
    Map<String, Integer> countOffersByCompany() throws DataAccessException;
}
