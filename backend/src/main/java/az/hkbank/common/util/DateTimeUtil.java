package az.hkbank.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations.
 * Provides common date/time functionality used across the application.
 */
public final class DateTimeUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns the current date and time.
     *
     * @return current LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Formats a LocalDateTime to a string using the default format (yyyy-MM-dd HH:mm:ss).
     *
     * @param dateTime the LocalDateTime to format
     * @return formatted date-time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * Checks if the given date-time has expired (is before the current time).
     *
     * @param dateTime the LocalDateTime to check
     * @return true if the date-time is in the past, false otherwise
     */
    public static boolean isExpired(LocalDateTime dateTime) {
        if (dateTime == null) {
            return true;
        }
        return dateTime.isBefore(LocalDateTime.now());
    }
}
