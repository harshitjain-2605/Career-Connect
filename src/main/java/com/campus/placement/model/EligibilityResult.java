package com.campus.placement.model;

/**
 * Result of evaluating a student's eligibility for a placement drive.
 */
public class EligibilityResult {

    private final boolean eligible;
    private final String reason;

    /**
     * Creates an EligibilityResult.
     *
     * @param eligible true if student meets all criteria
     * @param reason   "Eligible" or explanation of why ineligible
     */
    public EligibilityResult(boolean eligible, String reason) {
        this.eligible = eligible;
        this.reason = reason;
    }

    public static EligibilityResult ok() {
        return new EligibilityResult(true, "Eligible");
    }

    public static EligibilityResult fail(String reason) {
        return new EligibilityResult(false, reason);
    }

    public boolean isEligible() {
        return eligible;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return eligible ? "Eligible" : "Not eligible: " + reason;
    }
}
