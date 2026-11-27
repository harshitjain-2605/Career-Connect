package com.campus.placement;

import com.campus.placement.model.*;
import com.campus.placement.service.EligibilityService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EligibilityService} covering all academic, deadline, and placement policy rules.
 */
class EligibilityServiceTest {

    private final EligibilityService service = new EligibilityService();
    private final LocalDate today = LocalDate.of(2026, 12, 1);

    private Student baseStudent() {
        Student s = new Student();
        s.setId(1);
        s.setBranch("CSE");
        s.setBatchYear(2026);
        s.setCgpa(8.5);
        s.setActiveBacklogs(0);
        s.setBlocked(false);
        return s;
    }

    private Drive baseDrive() {
        Drive d = new Drive();
        d.setId(10);
        d.setStatus(DriveStatus.OPEN);
        d.setLastDate(today.plusDays(5));
        d.setDriveDate(today.plusDays(10));
        d.setBatchYear(2026);
        d.setMinCgpa(7.5);
        d.setMaxBacklogs(0);
        d.setPackageLpa(10.0);
        d.setAllowedBranches(Set.of("CSE", "ECE"));
        return d;
    }

    @Test
    void eligibleStudent_returnsOk() {
        EligibilityResult res = service.evaluate(baseStudent(), baseDrive(), today, null, "UPGRADE_1_5X");
        assertTrue(res.isEligible());
    }

    @Test
    void blockedStudent_isIneligible() {
        Student s = baseStudent();
        s.setBlocked(true);
        EligibilityResult res = service.evaluate(s, baseDrive(), today, null, "UPGRADE_1_5X");
        assertFalse(res.isEligible());
        assertTrue(res.getReason().contains("blocked"));
    }

    @Test
    void lowCgpa_isIneligible() {
        Student s = baseStudent();
        s.setCgpa(7.2);
        EligibilityResult res = service.evaluate(s, baseDrive(), today, null, "UPGRADE_1_5X");
        assertFalse(res.isEligible());
        assertTrue(res.getReason().contains("CGPA"));
    }

    @Test
    void excessiveBacklogs_isIneligible() {
        Student s = baseStudent();
        s.setActiveBacklogs(1);
        EligibilityResult res = service.evaluate(s, baseDrive(), today, null, "UPGRADE_1_5X");
        assertFalse(res.isEligible());
        assertTrue(res.getReason().contains("Backlogs"));
    }

    @Test
    void disallowedBranch_isIneligible() {
        Student s = baseStudent();
        s.setBranch("ME");
        EligibilityResult res = service.evaluate(s, baseDrive(), today, null, "UPGRADE_1_5X");
        assertFalse(res.isEligible());
        assertTrue(res.getReason().contains("Branch"));
    }

    @Test
    void pastDeadline_isIneligible() {
        Drive d = baseDrive();
        d.setLastDate(today.minusDays(1));
        EligibilityResult res = service.evaluate(baseStudent(), d, today, null, "UPGRADE_1_5X");
        assertFalse(res.isEligible());
        assertTrue(res.getReason().contains("Deadline"));
    }

    @Test
    void closedDrive_isIneligible() {
        Drive d = baseDrive();
        d.setStatus(DriveStatus.CLOSED);
        EligibilityResult res = service.evaluate(baseStudent(), d, today, null, "UPGRADE_1_5X");
        assertFalse(res.isEligible());
    }

    @Test
    void singleOfferPolicy_blocksSecondDrive() {
        Offer accepted = new Offer();
        accepted.setPackageLpa(10.0);
        EligibilityResult res = service.evaluate(baseStudent(), baseDrive(), today, accepted, "SINGLE_OFFER");
        assertFalse(res.isEligible());
        assertTrue(res.getReason().contains("Single-Offer"));
    }

    @Test
    void upgrade15xPolicy_requiresAtLeast150Percent() {
        Offer accepted = new Offer();
        accepted.setPackageLpa(10.0);

        Drive belowUpgrade = baseDrive();
        belowUpgrade.setPackageLpa(14.0);
        assertFalse(service.evaluate(baseStudent(), belowUpgrade, today, accepted, "UPGRADE_1_5X").isEligible());

        Drive meetsUpgrade = baseDrive();
        meetsUpgrade.setPackageLpa(15.0);
        assertTrue(service.evaluate(baseStudent(), meetsUpgrade, today, accepted, "UPGRADE_1_5X").isEligible());
    }
}
