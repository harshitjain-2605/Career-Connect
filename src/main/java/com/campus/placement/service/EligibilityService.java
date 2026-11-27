package com.campus.placement.service;

import com.campus.placement.config.AppConfig;
import com.campus.placement.dao.JdbcOfferDao;
import com.campus.placement.dao.OfferDao;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Drive;
import com.campus.placement.model.DriveStatus;
import com.campus.placement.model.EligibilityResult;
import com.campus.placement.model.Offer;
import com.campus.placement.model.Student;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Evaluates whether a {@link Student} is eligible to apply for a {@link Drive}
 * based on academic criteria, drive status/deadline, and the configured placement policy.
 */
public class EligibilityService {

    private final OfferDao offerDao;

    public EligibilityService() {
        this(new JdbcOfferDao());
    }

    public EligibilityService(OfferDao offerDao) {
        this.offerDao = offerDao;
    }

    /**
     * Evaluates eligibility using today's date and the policy from {@link AppConfig}.
     */
    public EligibilityResult checkEligibility(Student student, Drive drive) throws DataAccessException {
        Optional<Offer> acceptedOffer = offerDao.findAcceptedOfferByStudentId(student.getId());
        return evaluate(student, drive, LocalDate.now(), acceptedOffer.orElse(null),
                AppConfig.getInstance().getPlacementPolicy());
    }

    /**
     * Pure, deterministic eligibility evaluation (easy to unit-test).
     *
     * @param student       student profile
     * @param drive         target placement drive
     * @param today         reference date
     * @param acceptedOffer student's highest accepted offer, or null if none
     * @param policy        {@code SINGLE_OFFER} or {@code UPGRADE_1_5X}
     * @return {@link EligibilityResult} with status and reason
     */
    public EligibilityResult evaluate(Student student, Drive drive, LocalDate today,
                                      Offer acceptedOffer, String policy) {
        if (student.isBlocked()) {
            return EligibilityResult.fail("Account is blocked by TPO");
        }
        if (drive.getStatus() != DriveStatus.OPEN) {
            return EligibilityResult.fail("Drive is " + drive.getStatus());
        }
        if (drive.getLastDate() != null && today.isAfter(drive.getLastDate())) {
            return EligibilityResult.fail("Deadline passed (" + drive.getLastDate() + ")");
        }
        if (student.getBatchYear() != drive.getBatchYear()) {
            return EligibilityResult.fail("Requires batch " + drive.getBatchYear());
        }
        if (drive.getAllowedBranches() == null || !drive.getAllowedBranches().contains(student.getBranch())) {
            return EligibilityResult.fail("Branch " + student.getBranch() + " not allowed");
        }
        if (student.getCgpa() < drive.getMinCgpa()) {
            return EligibilityResult.fail(String.format("CGPA %.2f < min %.2f", student.getCgpa(), drive.getMinCgpa()));
        }
        if (student.getActiveBacklogs() > drive.getMaxBacklogs()) {
            return EligibilityResult.fail("Backlogs (" + student.getActiveBacklogs()
                    + ") > max (" + drive.getMaxBacklogs() + ")");
        }

        if (acceptedOffer != null) {
            if ("SINGLE_OFFER".equalsIgnoreCase(policy)) {
                return EligibilityResult.fail("Already placed (Single-Offer policy)");
            }
            if ("UPGRADE_1_5X".equalsIgnoreCase(policy)) {
                double requiredPackage = acceptedOffer.getPackageLpa() * 1.5;
                if (drive.getPackageLpa() < requiredPackage) {
                    return EligibilityResult.fail(String.format(
                            "Upgrade policy requires >= %.2f LPA (1.5x of %.2f LPA)",
                            requiredPackage, acceptedOffer.getPackageLpa()));
                }
            }
        }

        return EligibilityResult.ok();
    }
}
