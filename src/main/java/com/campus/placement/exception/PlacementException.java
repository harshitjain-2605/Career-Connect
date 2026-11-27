package com.campus.placement.exception;

/**
 * Base checked exception for all domain and system errors in the
 * Campus Placement Management System.
 */
// [Unit 2: Custom exception hierarchy] Base checked exception
public class PlacementException extends Exception {

    /**
     * Constructs a new PlacementException with the specified detail message.
     *
     * @param message human-readable error description
     */
    public PlacementException(String message) {
        super(message);
    }

    /**
     * Constructs a new PlacementException with a detail message and underlying cause.
     *
     * @param message human-readable error description
     * @param cause   original cause of the failure
     */
    public PlacementException(String message, Throwable cause) {
        super(message, cause);
    }
}
