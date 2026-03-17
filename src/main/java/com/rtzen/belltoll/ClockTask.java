package com.rtzen.belltoll;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClockTask implements Runnable {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private volatile ClockConfig config;
    private final PlayerPreferencesManager preferencesManager;

    /** Tracks the last processed minute-of-hour per player to avoid duplicate notifications. */
    private final Map<UUID, Integer> lastProcessedMinute = new HashMap<>();

    public ClockTask(ClockConfig config, PlayerPreferencesManager preferencesManager) {
        this.config = config;
        this.preferencesManager = preferencesManager;
    }

    public void setConfig(ClockConfig config) {
        this.config = config;
    }

    public ClockConfig getConfig() {
        return config;
    }

    @Override
    public void run() {
        ClockConfig cfg = config;
        Instant instant = Instant.now();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerPreferences prefs = preferencesManager.getPreferences(uuid);

            ZoneId tz = prefs.getTimezone() != null ? prefs.getTimezone() : cfg.getTimezone();
            ZonedDateTime now = instant.atZone(tz);
            int minute = now.getMinute();

            Integer last = lastProcessedMinute.get(uuid);
            if (last != null && last == minute) continue;     // same minute, nothing to do
            lastProcessedMinute.put(uuid, minute);
            if (last == null) continue;                       // first tick for this player – skip

            int interval = prefs.getIntervalMinutes() != null ? prefs.getIntervalMinutes() : cfg.getIntervalMinutes();
            if (minute % interval != 0) continue;

            NotificationMode mode = prefs.getMode() != null ? prefs.getMode() : cfg.getDefaultNotificationMode();
            if (mode == NotificationMode.DISABLED) continue;

            sendTextToPlayer(player, now, cfg, null);

            if (mode == NotificationMode.FULL) {
                List<Integer> audioMins = prefs.getAudioMinutes() != null
                        ? prefs.getAudioMinutes() : cfg.getAudioMinutes();
                if (audioMins.isEmpty() || audioMins.contains(minute)) {
                    playBellSound(player, cfg);
                }
            }
        }
    }

    /**
     * Sends a time announcement to all online players, respecting each player's
     * notification preferences. Used by the {@code /belltoll ring} admin command.
     *
     * @param overrideMode display mode to use, or {@code null} to use the configured default
     */
    public void sendTimeMessage(DisplayMode overrideMode) {
        ClockConfig cfg = config;
        Instant instant = Instant.now();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerPreferences prefs = preferencesManager.getPreferences(uuid);

            NotificationMode mode = prefs.getMode() != null ? prefs.getMode() : cfg.getDefaultNotificationMode();
            if (mode == NotificationMode.DISABLED) continue;

            ZoneId tz = prefs.getTimezone() != null ? prefs.getTimezone() : cfg.getTimezone();
            ZonedDateTime now = instant.atZone(tz);

            sendTextToPlayer(player, now, cfg, overrideMode);

            if (mode == NotificationMode.FULL) {
                playBellSound(player, cfg);
            }
        }
    }

    private void sendTextToPlayer(Player player, ZonedDateTime now, ClockConfig cfg, DisplayMode overrideMode) {
        String formattedTime = now.format(cfg.getTimeFormatter());
        String raw = cfg.getMessageTemplate().replace("%time%", formattedTime);
        Component message = MINI.deserialize(raw);

        DisplayMode mode = overrideMode != null ? overrideMode : cfg.getDisplayMode();
        switch (mode) {
            case CHAT      -> player.sendMessage(message);
            case ACTIONBAR -> player.sendActionBar(message);
            case TITLE     -> {
                Title.Times times = Title.Times.times(
                        Duration.ofMillis(cfg.getTitleFadeIn()  * 50L),
                        Duration.ofMillis(cfg.getTitleStay()    * 50L),
                        Duration.ofMillis(cfg.getTitleFadeOut() * 50L)
                );
                player.showTitle(Title.title(message, Component.empty(), times));
            }
        }
    }

    private static void playBellSound(Player player, ClockConfig cfg) {
        player.playSound(player.getLocation(), cfg.getSound(), SoundCategory.BLOCKS,
                cfg.getSoundVolume(), cfg.getSoundPitch());
    }
}
