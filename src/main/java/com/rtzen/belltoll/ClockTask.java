package com.rtzen.belltoll;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.ZonedDateTime;

public final class ClockTask implements Runnable {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private volatile ClockConfig config;
    private int lastMinute = -1;

    public ClockTask(ClockConfig config) {
        this.config = config;
    }

    public void setConfig(ClockConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        ClockConfig cfg = config;
        ZonedDateTime now = ZonedDateTime.now(cfg.getTimezone());
        int minute = now.getMinute();

        if (minute == lastMinute) return;
        lastMinute = minute;

        if (minute % cfg.getIntervalMinutes() != 0) return;

        sendTimeMessage(null);
    }

    /**
     * Sends a time announcement to all online players.
     *
     * @param overrideMode display mode to use for this send, or {@code null} to use the configured default
     */
    public void sendTimeMessage(DisplayMode overrideMode) {
        ClockConfig cfg = config;
        ZonedDateTime now = ZonedDateTime.now(cfg.getTimezone());
        String formattedTime = now.format(cfg.getTimeFormatter());
        String raw = cfg.getMessageTemplate().replace("%time%", formattedTime);
        Component message = MINI.deserialize(raw);

        DisplayMode mode = overrideMode != null ? overrideMode : cfg.getDisplayMode();

        for (Player player : Bukkit.getOnlinePlayers()) {
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
    }
}
