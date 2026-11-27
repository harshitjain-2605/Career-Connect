package com.campus.placement.dao;

import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.User;

import java.util.Optional;

/**
 * DAO contract for {@link User} accounts.
 */
public interface UserDao extends Dao<User, Integer> {

    /**
     * Finds a user by unique email address.
     *
     * @param email user email
     * @return Optional containing user if found
     * @throws DataAccessException if query fails
     */
    Optional<User> findByEmail(String email) throws DataAccessException;

    /**
     * Updates a user's active flag.
     *
     * @param userId user ID
     * @param active true to activate, false to deactivate
     * @return true if updated
     * @throws DataAccessException if update fails
     */
    boolean setActive(int userId, boolean active) throws DataAccessException;
}
