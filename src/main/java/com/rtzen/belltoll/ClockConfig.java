package com.rtzen.belltoll;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class ClockConfig {

    private final ZoneId timezone;
    private final int intervalMinutes;
    private final DateTimeFormatter timeFormatter;
    private final DisplayMode displayMode;
    private final String messageTemplate;
    private final int titleFadeIn;
    private final int titleStay;
    private final int titleFadeOut;
    private final Sound sound;
    private final float soundVolume;
    private final float soundPitch;
    private final List<Integer> audioMinutes;
    private final NotificationMode defaultNotificationMode;

    public ClockConfig(JavaPlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();
        this.timezone        = parseTimezone(cfg.getString("timezone", "UTC"), plugin);
        this.intervalMinutes = parseInterval(cfg.getInt("interval-minutes", 15), plugin);
        this.timeFormatter   = parseFormatter(cfg.getString("time-format", "HH:mm"), plugin);
        this.displayMode     = DisplayMode.fromString(cfg.getString("display-mode", "CHAT"));
        this.messageTemplate = cfg.getString("message",
                "<gold>\uD83D\uDD14 The church bell rings! <yellow>%time%</yellow></gold>");
        this.titleFadeIn  = cfg.getInt("title.fade-in",  10);
        this.titleStay    = cfg.getInt("title.stay",     60);
        this.titleFadeOut = cfg.getInt("title.fade-out", 10);
        this.sound        = parseSound(cfg.getString("sound.name", "BLOCK_BELL_USE"), plugin);
        this.soundVolume  = (float) cfg.getDouble("sound.volume", 1.0);
        this.soundPitch   = (float) cfg.getDouble("sound.pitch",  1.0);
        this.audioMinutes = cfg.getIntegerList("audio-minutes");
        this.defaultNotificationMode = NotificationMode.fromString(
                cfg.getString("default-notification-mode", "FULL"));
    }

    private static ZoneId parseTimezone(String value, JavaPlugin plugin) {
        try {
            return ZoneId.of(value);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid timezone '" + value + "', falling back to UTC.");
            return ZoneId.of("UTC");
        }
    }

    private static int parseInterval(int value, JavaPlugin plugin) {
        if (value >= 1 && value <= 60 && 60 % value == 0) {
            return value;
        }
        plugin.getLogger().warning(
                "Invalid interval-minutes '" + value + "'. Must be a divisor of 60 (1-60). Falling back to 15.");
        return 15;
    }

    private static DateTimeFormatter parseFormatter(String pattern, JavaPlugin plugin) {
        try {
            return DateTimeFormatter.ofPattern(pattern);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid time-format '" + pattern + "', falling back to HH:mm.");
            return DateTimeFormatter.ofPattern("HH:mm");
        }
    }

    private static Sound parseSound(String name, JavaPlugin plugin) {
        try {
            return Sound.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound name '" + name + "', falling back to BLOCK_BELL_USE.");
            return Sound.BLOCK_BELL_USE;
        }
    }

    public ZoneId getTimezone()                          { return timezone; }
    public int getIntervalMinutes()                      { return intervalMinutes; }
    public DateTimeFormatter getTimeFormatter()          { return timeFormatter; }
    public DisplayMode getDisplayMode()                  { return displayMode; }
    public String getMessageTemplate()                   { return messageTemplate; }
    public int getTitleFadeIn()                          { return titleFadeIn; }
    public int getTitleStay()                            { return titleStay; }
    public int getTitleFadeOut()                         { return titleFadeOut; }
    public Sound getSound()                              { return sound; }
    public float getSoundVolume()                        { return soundVolume; }
    public float getSoundPitch()                         { return soundPitch; }
    /** Minutes-of-hour at which the audio fires. An empty list means every interval. */
    public List<Integer> getAudioMinutes()               { return audioMinutes; }
    public NotificationMode getDefaultNotificationMode() { return defaultNotificationMode; }
}
