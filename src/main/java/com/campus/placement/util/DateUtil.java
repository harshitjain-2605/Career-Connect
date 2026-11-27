package com.campus.placement.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date and timestamp parsing/formatting helpers for SQLite/MySQL ISO strings.
 */
// [Unit 2: LocalDate / LocalDateTime]
public final class DateUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter SQL_DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATETIME_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private DateUtil() {
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return LocalDate.parse(text.trim(), DATE_FMT);
    }

    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FMT);
    }

    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim();
        try {
            return LocalDateTime.parse(trimmed, SQL_DATETIME_FMT);
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(trimmed);
        }
    }

    public static String formatSqlDateTime(LocalDateTime dt) {
        return dt == null ? null : dt.format(SQL_DATETIME_FMT);
    }

    public static String formatDisplayDateTime(LocalDateTime dt) {
        return dt == null ? "-" : dt.format(DISPLAY_DATETIME_FMT);
    }
}
