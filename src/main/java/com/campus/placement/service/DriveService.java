package com.campus.placement.service;

import com.campus.placement.dao.DriveDao;
import com.campus.placement.dao.JdbcDriveDao;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.exception.ValidationException;
import com.campus.placement.model.Drive;
import com.campus.placement.model.DriveStatus;
import com.campus.placement.util.ValidationUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service managing placement {@link Drive} CRUD, eligibility criteria, and search/sort.
 */
public class DriveService {

    private final DriveDao driveDao;
    private final AuditService auditService;

    public DriveService() {
        this(new JdbcDriveDao(), new AuditService());
    }

    public DriveService(DriveDao driveDao, AuditService auditService) {
        this.driveDao = driveDao;
        this.auditService = auditService;
    }

    public List<Drive> getAllDrives() throws DataAccessException {
        return driveDao.findAll();
    }

    public Optional<Drive> getById(int driveId) throws DataAccessException {
        return driveDao.findById(driveId);
    }

    public List<Drive> getDrivesByCompany(int companyId) throws DataAccessException {
        return driveDao.findByCompanyId(companyId);
    }

    public List<Drive> searchDrives(String keyword, DriveStatus status, String sortBy) throws DataAccessException {
        List<Drive> list = driveDao.search(keyword, status);
        if ("PACKAGE_DESC".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparingDouble(Drive::getPackageLpa).reversed());
        } else if ("LAST_DATE_ASC".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing(Drive::getLastDate));
        }
        return list;
    }

    public Drive createDrive(Drive drive, Integer actorUserId) throws ValidationException, DataAccessException {
        validate(drive);
        Drive saved = driveDao.save(drive);
        auditService.log(actorUserId, "CREATE", "DRIVE", saved.getId(),
                "Created drive " + saved.getJobRole() + " (" + saved.getPackageLpa() + " LPA)");
        return saved;
    }

    public void updateDrive(Drive drive, Integer actorUserId) throws ValidationException, DataAccessException {
        validate(drive);
        driveDao.update(drive);
        auditService.log(actorUserId, "UPDATE", "DRIVE", drive.getId(),
                "Updated drive " + drive.getJobRole());
    }

    public void deleteDrive(int driveId, Integer actorUserId) throws DataAccessException {
        driveDao.deleteById(driveId);
        auditService.log(actorUserId, "DELETE", "DRIVE", driveId, "Deleted drive id=" + driveId);
    }

    private void validate(Drive d) throws ValidationException {
        if (d.getCompanyId() <= 0) {
            throw new ValidationException("Please select a company.");
        }
        ValidationUtil.requireNonBlank(d.getJobRole(), "Job Role");
        ValidationUtil.requireNonBlank(d.getLocation(), "Location");
        if (d.getPackageLpa() < 0) {
            throw new ValidationException("Package (LPA) cannot be negative.");
        }
        ValidationUtil.validateCgpa(d.getMinCgpa());
        ValidationUtil.validateBacklogs(d.getMaxBacklogs());
        if (d.getDriveDate() == null || d.getLastDate() == null) {
            throw new ValidationException("Drive Date and Last Date to Apply are required.");
        }
        if (d.getLastDate().isAfter(d.getDriveDate())) {
            throw new ValidationException("Last Date to Apply cannot be after the Drive Date.");
        }
        if (d.getAllowedBranches() == null || d.getAllowedBranches().isEmpty()) {
            throw new ValidationException("Select at least one allowed branch.");
        }
        if (d.getJobType() == null || d.getStatus() == null) {
            throw new ValidationException("Job Type and Status are required.");
        }
    }
}
