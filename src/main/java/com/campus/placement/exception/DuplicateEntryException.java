package com.campus.placement.exception;

/**
 * Thrown when attempting to insert a duplicate record (e.g., duplicate email,
 * roll number, or duplicate application for the same drive).
 */
// [Unit 2: Custom exception hierarchy]
public class DuplicateEntryException extends PlacementException {

    /**
     * Constructs a DuplicateEntryException with a descriptive message.
     *
     * @param message duplicate conflict details
     */
    public DuplicateEntryException(String message) {
        super(message);
    }

    /**
     * Constructs a DuplicateEntryException with a message and SQL cause.
     *
     * @param message duplicate conflict details
     * @param cause   underlying exception
     */
    public DuplicateEntryException(String message, Throwable cause) {
        super(message, cause);
    }
}
