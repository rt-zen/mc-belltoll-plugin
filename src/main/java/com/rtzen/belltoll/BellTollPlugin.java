package com.rtzen.belltoll;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class BellTollPlugin extends JavaPlugin {

    private ClockConfig clockConfig;
    private ClockTask clockTask;
    private BukkitTask scheduledTask;
    private PlayerPreferencesManager preferencesManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        clockConfig = new ClockConfig(this);
        preferencesManager = new PlayerPreferencesManager(this);
        clockTask = new ClockTask(clockConfig, preferencesManager);
        scheduledTask = getServer().getScheduler().runTaskTimer(this, clockTask, 0L, 20L);

        BellTollCommand commandHandler = new BellTollCommand(this, preferencesManager);
        var cmd = getCommand("belltoll");
        if (cmd != null) {
            cmd.setExecutor(commandHandler);
            cmd.setTabCompleter(commandHandler);
        }

        getLogger().info("Enabled. Interval: " + clockConfig.getIntervalMinutes()
                + " min | Timezone: " + clockConfig.getTimezone()
                + " | Display: " + clockConfig.getDisplayMode()
                + " | Default mode: " + clockConfig.getDefaultNotificationMode());
    }

    @Override
    public void onDisable() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
        if (preferencesManager != null) {
            preferencesManager.save();
        }
        getLogger().info("Disabled.");
    }

    /** Reloads config.yml and applies the new settings to the running task. */
    public void reloadClockConfig() {
        reloadConfig();
        clockConfig = new ClockConfig(this);
        clockTask.setConfig(clockConfig);
    }

    public ClockTask getClockTask() {
        return clockTask;
    }

    public ClockConfig getClockConfig() {
        return clockConfig;
    }
}
