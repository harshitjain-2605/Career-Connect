package com.campus.placement.dao;

import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * DAO contract for {@link Student} profiles and custom search queries.
 */
public interface StudentDao extends Dao<Student, Integer> {

    Optional<Student> findByUserId(int userId) throws DataAccessException;

    Optional<Student> findByRollNo(String rollNo) throws DataAccessException;

    Optional<Student> findByEmail(String email) throws DataAccessException;

    /**
     * Searches students by optional branch, minimum CGPA, and keyword (name/rollNo/skills).
     *
     * @param branch  optional branch filter (null or "ALL" for all)
     * @param minCgpa minimum CGPA (0.0 for all)
     * @param keyword optional text search
     * @return matching students ordered by CGPA descending
     * @throws DataAccessException if query fails
     */
    List<Student> search(String branch, double minCgpa, String keyword) throws DataAccessException;

    boolean setBlocked(int studentId, boolean blocked) throws DataAccessException;
}
