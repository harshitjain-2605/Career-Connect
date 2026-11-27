package com.campus.placement.dao;

import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Drive;
import com.campus.placement.model.DriveStatus;

import java.util.List;

/**
 * DAO contract for {@link Drive} entities and their {@code drive_branches} join table.
 */
public interface DriveDao extends Dao<Drive, Integer> {

    List<Drive> findByStatus(DriveStatus status) throws DataAccessException;

    List<Drive> findByCompanyId(int companyId) throws DataAccessException;

    List<Drive> search(String keyword, DriveStatus status) throws DataAccessException;
}
