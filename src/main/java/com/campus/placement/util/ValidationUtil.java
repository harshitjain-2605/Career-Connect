package com.campus.placement.util;

import com.campus.placement.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Reusable input validation helpers for forms and services.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10,15}$");

    private ValidationUtil() {
    }

    /**
     * Ensures a required string is non-null and non-blank.
     *
     * @param value     input string
     * @param fieldName label for error message
     * @throws ValidationException if blank
     */
    public static void requireNonBlank(String value, String fieldName) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    /**
     * Validates an email address format.
     *
     * @param email email string
     * @throws ValidationException if invalid
     */
    public static void validateEmail(String email) throws ValidationException {
        requireNonBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    /**
     * Validates a 10-15 digit phone number.
     *
     * @param phone phone string
     * @throws ValidationException if invalid
     */
    public static void validatePhone(String phone) throws ValidationException {
        requireNonBlank(phone, "Phone");
        String digits = phone.replaceAll("[\\s\\-+()]", "");
        if (!PHONE_PATTERN.matcher(digits).matches()) {
            throw new ValidationException("Phone must contain 10 to 15 digits.");
        }
    }

    /**
     * Validates CGPA is in [0.0, 10.0].
     *
     * @param cgpa CGPA value
     * @throws ValidationException if out of range
     */
    public static void validateCgpa(double cgpa) throws ValidationException {
        if (Double.isNaN(cgpa) || cgpa < 0.0 || cgpa > 10.0) {
            throw new ValidationException("CGPA must be between 0.00 and 10.00.");
        }
    }

    /**
     * Validates active backlogs is non-negative.
     *
     * @param backlogs backlog count
     * @throws ValidationException if negative
     */
    public static void validateBacklogs(int backlogs) throws ValidationException {
        if (backlogs < 0) {
            throw new ValidationException("Active backlogs cannot be negative.");
        }
    }
}
