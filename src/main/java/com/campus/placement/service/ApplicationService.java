package com.campus.placement.service;

import com.campus.placement.dao.*;
import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.exception.DuplicateEntryException;
import com.campus.placement.exception.NotEligibleException;
import com.campus.placement.exception.ValidationException;
import com.campus.placement.model.*;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service managing student drive applications and validated recruitment stage transitions.
 */
public class ApplicationService {

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(ApplicationStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ApplicationStatus.APPLIED,
                Set.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.SHORTLISTED,
                Set.of(ApplicationStatus.TEST, ApplicationStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.TEST,
                Set.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.INTERVIEW,
                Set.of(ApplicationStatus.SELECTED, ApplicationStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.SELECTED, Set.of());
        ALLOWED_TRANSITIONS.put(ApplicationStatus.REJECTED, Set.of());
    }

    private final ApplicationDao applicationDao;
    private final DriveDao driveDao;
    private final StudentDao studentDao;
    private final JdbcOfferDao offerDao;
    private final NotificationDao notificationDao;
    private final EligibilityService eligibilityService;
    private final AuditService auditService;

    public ApplicationService() {
        this(new JdbcApplicationDao(), new JdbcDriveDao(), new JdbcStudentDao(),
                new JdbcOfferDao(), new JdbcNotificationDao(),
                new EligibilityService(), new AuditService());
    }

    public ApplicationService(ApplicationDao applicationDao, DriveDao driveDao, StudentDao studentDao,
                              JdbcOfferDao offerDao, NotificationDao notificationDao,
                              EligibilityService eligibilityService, AuditService auditService) {
        this.applicationDao = applicationDao;
        this.driveDao = driveDao;
        this.studentDao = studentDao;
        this.offerDao = offerDao;
        this.notificationDao = notificationDao;
        this.eligibilityService = eligibilityService;
        this.auditService = auditService;
    }

    /**
     * Checks if transitioning from {@code from} to {@code to} is allowed by the pipeline rules.
     */
    public boolean isValidTransition(ApplicationStatus from, ApplicationStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    /**
     * Applies a student to a drive after enforcing eligibility and duplicate prevention.
     */
    public Application apply(Student student, int driveId)
            throws NotEligibleException, DuplicateEntryException, DataAccessException, ValidationException {
        Drive drive = driveDao.findById(driveId)
                .orElseThrow(() -> new ValidationException("Drive not found: id=" + driveId));

        if (applicationDao.findByStudentAndDrive(student.getId(), driveId).isPresent()) {
            throw new DuplicateEntryException("You have already applied to this drive.");
        }

        EligibilityResult eligibility = eligibilityService.checkEligibility(student, drive);
        if (!eligibility.isEligible()) {
            throw new NotEligibleException(eligibility.getReason());
        }

        Application app = new Application();
        app.setStudentId(student.getId());
        app.setDriveId(driveId);
        app.setStatus(ApplicationStatus.APPLIED);

        Application saved = applicationDao.save(app);
        auditService.log(student.getUserId(), "APPLY", "APPLICATION", saved.getId(),
                "Applied to driveId=" + driveId);
        return saved;
    }

    /**
     * Transitions an application to a new stage inside a transaction.
     * If {@code newStatus == SELECTED}, an {@link Offer} and {@link Notification} are also created.
     */
    public void updateStage(int applicationId, ApplicationStatus newStatus, Integer actorUserId,
                            String remarks, LocalDate joiningDate)
            throws ValidationException, DataAccessException {
        Application app = applicationDao.findById(applicationId)
                .orElseThrow(() -> new ValidationException("Application not found: id=" + applicationId));

        if (!isValidTransition(app.getStatus(), newStatus)) {
            throw new ValidationException("Invalid status transition: "
                    + app.getStatus() + " -> " + newStatus);
        }

        Drive drive = driveDao.findById(app.getDriveId())
                .orElseThrow(() -> new ValidationException("Drive not found: id=" + app.getDriveId()));
        Student student = studentDao.findById(app.getStudentId())
                .orElseThrow(() -> new ValidationException("Student not found: id=" + app.getStudentId()));

        ConnectionManager.executeInTransaction(conn -> {
            applicationDao.updateStatusInTransaction(
                    conn, applicationId, app.getStatus(), newStatus, actorUserId, remarks);

            if (newStatus == ApplicationStatus.SELECTED) {
                Offer offer = new Offer();
                offer.setApplicationId(applicationId);
                offer.setStudentId(student.getId());
                offer.setDriveId(drive.getId());
                offer.setPackageLpa(drive.getPackageLpa());
                offer.setJoiningDate(joiningDate != null ? joiningDate : drive.getDriveDate().plusMonths(6));
                offer.setStatus(OfferStatus.PENDING);
                offerDao.saveInTransaction(conn, offer);

                Notification notif = new Notification(
                        student.getUserId(),
                        "Offer Issued: " + drive.getCompanyName(),
                        String.format("Congratulations! You have been selected for %s (%.2f LPA).",
                                drive.getJobRole(), drive.getPackageLpa()));
                notificationDao.saveInTransaction(conn, notif);
            } else {
                Notification notif = new Notification(
                        student.getUserId(),
                        "Application Update: " + drive.getCompanyName(),
                        "Your application for " + drive.getJobRole() + " moved to stage: " + newStatus);
                notificationDao.saveInTransaction(conn, notif);
            }

            auditService.logInTransaction(conn, actorUserId, "STATUS_CHANGE", "APPLICATION",
                    applicationId, app.getStatus() + " -> " + newStatus);
            return null;
        });
    }

    public List<Application> getApplicationsByDrive(int driveId) throws DataAccessException {
        return applicationDao.findByDriveId(driveId);
    }

    public List<Application> getApplicationsByStudent(int studentId) throws DataAccessException {
        return applicationDao.findByStudentId(studentId);
    }

    public List<ApplicationStatusHistory> getStatusHistory(int applicationId) throws DataAccessException {
        return applicationDao.findHistoryByApplicationId(applicationId);
    }
}
