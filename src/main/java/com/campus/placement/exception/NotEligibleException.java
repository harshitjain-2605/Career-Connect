package com.campus.placement.exception;

/**
 * Thrown when a student does not satisfy the eligibility criteria or placement
 * policy required to apply for a placement drive.
 */
// [Unit 2: Custom exception hierarchy]
public class NotEligibleException extends PlacementException {

    /**
     * Constructs a NotEligibleException explaining why the student is ineligible.
     *
     * @param message eligibility failure reason
     */
    public NotEligibleException(String message) {
        super(message);
    }
}
