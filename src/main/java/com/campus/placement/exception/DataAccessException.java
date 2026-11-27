package com.campus.placement.exception;

/**
 * Thrown when a database or file-system persistence operation fails.
 */
// [Unit 2: Custom exception hierarchy]
public class DataAccessException extends PlacementException {

    /**
     * Constructs a DataAccessException with a descriptive message.
     *
     * @param message persistence error details
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a DataAccessException wrapping an underlying SQLException or IOException.
     *
     * @param message persistence error details
     * @param cause   underlying cause
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
