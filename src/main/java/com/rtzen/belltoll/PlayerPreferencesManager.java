package com.rtzen.belltoll;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player notification preferences, persisted in {@code players.yml}
 * inside the plugin data folder.
 */
public final class PlayerPreferencesManager {

    private final JavaPlugin plugin;
    private final File dataFile;
    private final Map<UUID, PlayerPreferences> cache = new HashMap<>();

    public PlayerPreferencesManager(JavaPlugin plugin) {
        this.plugin   = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        load();
    }

    /**
     * Returns the preferences for the given UUID, creating an empty entry
     * (all fields null = use global defaults) if none exists.
     */
    public PlayerPreferences getPreferences(UUID uuid) {
        return cache.computeIfAbsent(uuid, k -> new PlayerPreferences());
    }

    /** Loads all player preferences from {@code players.yml}. */
    public void load() {
        if (!dataFile.exists()) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection players = yaml.getConfigurationSection("players");
        if (players == null) return;

        for (String key : players.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping invalid UUID key in players.yml: " + key);
                continue;
            }

            ConfigurationSection section = players.getConfigurationSection(key);
            if (section == null) continue;

            PlayerPreferences prefs = new PlayerPreferences();

            String modeStr = section.getString("mode");
            if (modeStr != null) {
                try {
                    prefs.setMode(NotificationMode.valueOf(modeStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning(
                            "Invalid notification mode '" + modeStr + "' for UUID " + key + ", ignoring.");
                }
            }

            if (section.contains("interval-minutes")) {
                prefs.setIntervalMinutes(section.getInt("interval-minutes"));
            }

            String tzStr = section.getString("timezone");
            if (tzStr != null) {
                try {
                    prefs.setTimezone(ZoneId.of(tzStr));
                } catch (Exception e) {
                    plugin.getLogger().warning(
                            "Invalid timezone '" + tzStr + "' for UUID " + key + ", ignoring.");
                }
            }

            if (section.contains("audio-minutes")) {
                List<Integer> audioMinutes = section.getIntegerList("audio-minutes");
                prefs.setAudioMinutes(audioMinutes);
            }

            cache.put(uuid, prefs);
        }
    }

    /** Saves all player preferences to {@code players.yml}. */
    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();

        for (Map.Entry<UUID, PlayerPreferences> entry : cache.entrySet()) {
            String prefix = "players." + entry.getKey();
            PlayerPreferences prefs = entry.getValue();

            if (prefs.getMode() != null) {
                yaml.set(prefix + ".mode", prefs.getMode().name());
            }
            if (prefs.getIntervalMinutes() != null) {
                yaml.set(prefix + ".interval-minutes", prefs.getIntervalMinutes());
            }
            if (prefs.getTimezone() != null) {
                yaml.set(prefix + ".timezone", prefs.getTimezone().getId());
            }
            if (prefs.getAudioMinutes() != null) {
                yaml.set(prefix + ".audio-minutes", prefs.getAudioMinutes());
            }
        }

        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save players.yml: " + e.getMessage());
        }
    }
}
