package com.rtzen.belltoll;

import java.util.logging.Logger;

public enum NotificationMode {

    /** No text or audio notifications. */
    DISABLED,

    /** Text notifications only; audio is suppressed. */
    STEALTH,

    /** Full text and audio notifications. */
    FULL;

    /**
     * Parses a notification mode from a string, case-insensitively.
     * Falls back to {@link #FULL} for null or unrecognised values.
     */
    public static NotificationMode fromString(String value) {
        if (value == null) return FULL;
        try {
            return NotificationMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            Logger.getLogger("ForWhenTheBellTolls").warning(
                    "Unknown notification-mode value '" + value + "', falling back to FULL.");
            return FULL;
        }
    }
}
