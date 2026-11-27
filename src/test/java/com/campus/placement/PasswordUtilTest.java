package com.campus.placement;

import com.campus.placement.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PasswordUtil}.
 */
class PasswordUtilTest {

    @Test
    void hashAndVerify_roundTripSucceeds() {
        String raw = "Thapar@2026!";
        String hash1 = PasswordUtil.hashPassword(raw);
        String hash2 = PasswordUtil.hashPassword(raw);

        assertNotEquals(hash1, hash2, "Each hash should use a unique random salt");
        assertTrue(PasswordUtil.verifyPassword(raw, hash1));
        assertTrue(PasswordUtil.verifyPassword(raw, hash2));
        assertFalse(PasswordUtil.verifyPassword("WrongPass", hash1));
        assertFalse(PasswordUtil.verifyPassword(raw, "invalid-format"));
    }

    @Test
    void verifySeededAdminHash_succeeds() {
        String seededAdminHash = "65536:Y2FtcHVzX2FkbWluX3NhbHQ=:pQZzkBIZOCQhzKkqZKYOTWqNrHEowqjYWPeOOtjBHTU=";
        assertTrue(PasswordUtil.verifyPassword("Admin@123", seededAdminHash));
        assertFalse(PasswordUtil.verifyPassword("admin@123", seededAdminHash));
    }
}
