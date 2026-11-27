package com.campus.placement.exception;

/**
 * Thrown when login credentials are invalid or a user account is inactive/blocked.
 */
// [Unit 2: Custom exception hierarchy]
public class AuthenticationException extends PlacementException {

    /**
     * Constructs an AuthenticationException with a descriptive message.
     *
     * @param message authentication failure details
     */
    public AuthenticationException(String message) {
        super(message);
    }
}
