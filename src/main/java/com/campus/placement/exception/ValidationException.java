package com.campus.placement.exception;

/**
 * Thrown when user input or business state fails validation rules.
 */
// [Unit 2: Custom exception hierarchy]
public class ValidationException extends PlacementException {

    /**
     * Constructs a ValidationException with a descriptive message.
     *
     * @param message validation failure details
     */
    public ValidationException(String message) {
        super(message);
    }
}
