package com.rtzen.belltoll;

import java.util.logging.Logger;

public enum DisplayMode {
    CHAT,
    ACTIONBAR,
    TITLE;

    /**
     * Parses a display mode from a string, case-insensitively.
     * Falls back to {@link #CHAT} for null or unrecognised values.
     */
    public static DisplayMode fromString(String value) {
        if (value == null) return CHAT;
        try {
            return DisplayMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            Logger.getLogger("ForWhenTheBellTolls").warning(
                    "Unknown display-mode value '" + value + "', falling back to CHAT.");
            return CHAT;
        }
    }
}
