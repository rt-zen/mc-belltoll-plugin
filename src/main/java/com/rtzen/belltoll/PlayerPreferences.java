package com.rtzen.belltoll;

import java.time.ZoneId;
import java.util.List;

/**
 * Holds per-player notification preference overrides.
 * A {@code null} field means "use the global server default".
 */
public final class PlayerPreferences {

    private NotificationMode mode;
    private Integer intervalMinutes;
    private ZoneId timezone;
    private List<Integer> audioMinutes;

    public PlayerPreferences() {}

    public NotificationMode getMode() { return mode; }
    public void setMode(NotificationMode mode) { this.mode = mode; }

    public Integer getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }

    public ZoneId getTimezone() { return timezone; }
    public void setTimezone(ZoneId timezone) { this.timezone = timezone; }

    /** Returns the list of minute-of-hour values at which audio should play, or {@code null} to use the server default. */
    public List<Integer> getAudioMinutes() { return audioMinutes; }
    public void setAudioMinutes(List<Integer> audioMinutes) { this.audioMinutes = audioMinutes; }
}
