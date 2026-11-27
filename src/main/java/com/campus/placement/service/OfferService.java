package com.campus.placement.service;

import com.campus.placement.dao.*;
import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.exception.ValidationException;
import com.campus.placement.model.*;

import java.util.List;

/**
 * Service managing job {@link Offer} retrieval and transactional acceptance/rejection.
 */
// [Unit 4: Transaction management (commit/rollback)]
public class OfferService {

    private final OfferDao offerDao;
    private final StudentDao studentDao;
    private final NotificationDao notificationDao;
    private final AuditService auditService;

    public OfferService() {
        this(new JdbcOfferDao(), new JdbcStudentDao(), new JdbcNotificationDao(), new AuditService());
    }

    public OfferService(OfferDao offerDao, StudentDao studentDao,
                        NotificationDao notificationDao, AuditService auditService) {
        this.offerDao = offerDao;
        this.studentDao = studentDao;
        this.notificationDao = notificationDao;
        this.auditService = auditService;
    }

    public List<Offer> getOffersForStudent(int studentId) throws DataAccessException {
        return offerDao.findByStudentId(studentId);
    }

    public List<Offer> getAllOffers() throws DataAccessException {
        return offerDao.findAll();
    }

    /**
     * Accepts or declines an offer inside ONE JDBC transaction:
     * updates offer status, inserts a notification, and writes an audit log.
     * Rolls back all changes if any step fails.
     */
    public void respondToOffer(int offerId, int studentUserId, boolean accept)
            throws ValidationException, DataAccessException {
        Offer offer = offerDao.findById(offerId)
                .orElseThrow(() -> new ValidationException("Offer not found: id=" + offerId));
        Student student = studentDao.findById(offer.getStudentId())
                .orElseThrow(() -> new ValidationException("Student not found for offer."));

        if (student.getUserId() != studentUserId) {
            throw new ValidationException("You are not authorized to respond to this offer.");
        }
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new ValidationException("Offer has already been responded to (" + offer.getStatus() + ").");
        }

        OfferStatus newStatus = accept ? OfferStatus.ACCEPTED : OfferStatus.DECLINED;

        ConnectionManager.executeInTransaction(conn -> {
            offerDao.updateStatusInTransaction(conn, offerId, newStatus);

            Notification notif = new Notification(
                    studentUserId,
                    "Offer " + newStatus.name() + ": " + offer.getCompanyName(),
                    String.format("You have %s the offer for %s (%.2f LPA).",
                            newStatus.name().toLowerCase(), offer.getJobRole(), offer.getPackageLpa()));
            notificationDao.saveInTransaction(conn, notif);

            auditService.logInTransaction(conn, studentUserId, "OFFER_" + newStatus.name(),
                    "OFFER", offerId, "Package=" + offer.getPackageLpa());
            return null;
        });
    }
}
