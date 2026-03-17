package com.rtzen.belltoll;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class BellTollCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS         = List.of("prefs", "reload", "ring");
    private static final List<String> MODES               = List.of("actionbar", "chat", "title");
    private static final List<String> PREFS_SUBCOMMANDS   = List.of("audio", "interval", "mode", "timezone");
    private static final List<String> NOTIFICATION_MODES  = List.of("disabled", "full", "stealth");
    private static final List<String> INTERVALS           = List.of(
            "1", "2", "3", "4", "5", "6", "10", "12", "15", "20", "30", "60", "reset");
    private static final List<String> TIMEZONE_SUGGESTIONS = List.of(
            "America/Chicago", "America/Los_Angeles", "America/New_York", "America/Sao_Paulo",
            "Asia/Shanghai", "Asia/Tokyo", "Australia/Sydney",
            "Europe/Berlin", "Europe/Lisbon", "Europe/London", "Europe/Paris",
            "UTC", "reset");
    private static final List<String> AUDIO_SUGGESTIONS   = List.of("0", "0,30", "0,15,30,45", "all", "reset");

    private final BellTollPlugin plugin;
    private final PlayerPreferencesManager preferencesManager;

    public BellTollCommand(BellTollPlugin plugin, PlayerPreferencesManager preferencesManager) {
        this.plugin = plugin;
        this.preferencesManager = preferencesManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(usage(label));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                if (!sender.hasPermission("belltoll.reload")) {
                    sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
                    return true;
                }
                plugin.reloadClockConfig();
                sender.sendMessage(Component.text("[" + plugin.getName() + "] Configuration reloaded.", NamedTextColor.GREEN));
            }
            case "ring" -> {
                if (!sender.hasPermission("belltoll.ring")) {
                    sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
                    return true;
                }
                DisplayMode override = args.length >= 2 ? DisplayMode.fromString(args[1]) : null;
                plugin.getClockTask().sendTimeMessage(override);
                String modeNote = override != null ? " (" + override.name().toLowerCase(Locale.ROOT) + ")" : "";
                sender.sendMessage(Component.text("[" + plugin.getName() + "] Bell tolled" + modeNote + ".", NamedTextColor.GREEN));
            }
            case "prefs" -> handlePrefs(sender, label, args);
            default -> sender.sendMessage(usage(label));
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // /belltoll prefs handler
    // -------------------------------------------------------------------------

    private void handlePrefs(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return;
        }
        if (!sender.hasPermission("belltoll.prefs")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return;
        }

        if (args.length == 1) {
            sendPrefsDisplay(player);
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "mode" -> handlePrefsMode(player, label, args);
            case "interval" -> handlePrefsInterval(player, label, args);
            case "timezone" -> handlePrefsTimezone(player, label, args);
            case "audio" -> handlePrefsAudio(player, label, args);
            default -> player.sendMessage(Component.text(
                    "Usage: /" + label + " prefs [mode|interval|timezone|audio]", NamedTextColor.YELLOW));
        }
    }

    private void handlePrefsMode(Player player, String label, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text(
                    "Usage: /" + label + " prefs mode <disabled|stealth|full>", NamedTextColor.YELLOW));
            return;
        }
        NotificationMode mode;
        try {
            mode = NotificationMode.valueOf(args[2].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("Invalid mode. Use: disabled, stealth, or full.", NamedTextColor.RED));
            return;
        }
        preferencesManager.getPreferences(player.getUniqueId()).setMode(mode);
        preferencesManager.save();
        player.sendMessage(Component.text("Notification mode set to: " + mode.name().toLowerCase(Locale.ROOT) + ".", NamedTextColor.GREEN));
    }

    private void handlePrefsInterval(Player player, String label, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text(
                    "Usage: /" + label + " prefs interval <minutes|reset>", NamedTextColor.YELLOW));
            return;
        }
        if ("reset".equalsIgnoreCase(args[2])) {
            preferencesManager.getPreferences(player.getUniqueId()).setIntervalMinutes(null);
            preferencesManager.save();
            player.sendMessage(Component.text("Notification interval reset to server default.", NamedTextColor.GREEN));
            return;
        }
        int minutes;
        try {
            minutes = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid number. Use a divisor of 60 (1–60), or 'reset'.", NamedTextColor.RED));
            return;
        }
        if (minutes < 1 || minutes > 60 || 60 % minutes != 0) {
            player.sendMessage(Component.text(
                    "Invalid interval. Must be a divisor of 60: 1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, or 60.", NamedTextColor.RED));
            return;
        }
        preferencesManager.getPreferences(player.getUniqueId()).setIntervalMinutes(minutes);
        preferencesManager.save();
        player.sendMessage(Component.text("Notification interval set to every " + minutes + " minute(s).", NamedTextColor.GREEN));
    }

    private void handlePrefsTimezone(Player player, String label, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text(
                    "Usage: /" + label + " prefs timezone <zone|reset>", NamedTextColor.YELLOW));
            return;
        }
        if ("reset".equalsIgnoreCase(args[2])) {
            preferencesManager.getPreferences(player.getUniqueId()).setTimezone(null);
            preferencesManager.save();
            player.sendMessage(Component.text("Timezone reset to server default.", NamedTextColor.GREEN));
            return;
        }
        try {
            ZoneId zone = ZoneId.of(args[2]);
            preferencesManager.getPreferences(player.getUniqueId()).setTimezone(zone);
            preferencesManager.save();
            player.sendMessage(Component.text("Timezone set to: " + zone.getId() + ".", NamedTextColor.GREEN));
        } catch (Exception e) {
            player.sendMessage(Component.text(
                    "Invalid timezone. Use a Java timezone ID, e.g. Europe/Lisbon, America/New_York, UTC.", NamedTextColor.RED));
        }
    }

    private void handlePrefsAudio(Player player, String label, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text(
                    "Usage: /" + label + " prefs audio <minutes|all|reset>", NamedTextColor.YELLOW));
            player.sendMessage(Component.text(
                    "  e.g. '0,30' for full/half hour, '0' for full hour only, 'all' for every interval, or 'reset'.",
                    NamedTextColor.GRAY));
            return;
        }
        if ("reset".equalsIgnoreCase(args[2])) {
            preferencesManager.getPreferences(player.getUniqueId()).setAudioMinutes(null);
            preferencesManager.save();
            player.sendMessage(Component.text("Audio minutes reset to server default.", NamedTextColor.GREEN));
            return;
        }
        if ("all".equalsIgnoreCase(args[2])) {
            preferencesManager.getPreferences(player.getUniqueId()).setAudioMinutes(Collections.emptyList());
            preferencesManager.save();
            player.sendMessage(Component.text("Audio will play at all scheduled intervals.", NamedTextColor.GREEN));
            return;
        }
        String[] parts = args[2].split(",");
        List<Integer> audioMins = new ArrayList<>();
        for (String part : parts) {
            int m;
            try {
                m = Integer.parseInt(part.trim());
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text(
                        "Invalid audio minutes. Use comma-separated values 0–59, e.g. '0,30', or 'all'/'reset'.", NamedTextColor.RED));
                return;
            }
            if (m < 0 || m > 59) {
                player.sendMessage(Component.text(
                        "Each minute value must be between 0 and 59.", NamedTextColor.RED));
                return;
            }
            audioMins.add(m);
        }
        preferencesManager.getPreferences(player.getUniqueId()).setAudioMinutes(audioMins);
        preferencesManager.save();
        String display = ":" + audioMins.stream()
                .map(m -> String.format("%02d", m)).collect(Collectors.joining(", :"));
        player.sendMessage(Component.text("Audio will play at: " + display + ".", NamedTextColor.GREEN));
    }

    private void sendPrefsDisplay(Player player) {
        ClockConfig cfg = plugin.getClockTask().getConfig();
        PlayerPreferences prefs = preferencesManager.getPreferences(player.getUniqueId());

        player.sendMessage(Component.text("╔══ Your Bell Toll Preferences ══╗", NamedTextColor.GOLD));

        String modeStr = prefs.getMode() != null
                ? prefs.getMode().name().toLowerCase(Locale.ROOT)
                : "default (" + cfg.getDefaultNotificationMode().name().toLowerCase(Locale.ROOT) + ")";
        player.sendMessage(Component.text("  Mode:     ", NamedTextColor.YELLOW)
                .append(Component.text(modeStr, NamedTextColor.WHITE)));

        String intervalStr = prefs.getIntervalMinutes() != null
                ? "every " + prefs.getIntervalMinutes() + " min"
                : "default (every " + cfg.getIntervalMinutes() + " min)";
        player.sendMessage(Component.text("  Interval: ", NamedTextColor.YELLOW)
                .append(Component.text(intervalStr, NamedTextColor.WHITE)));

        String tzStr = prefs.getTimezone() != null
                ? prefs.getTimezone().getId()
                : "default (" + cfg.getTimezone().getId() + ")";
        player.sendMessage(Component.text("  Timezone: ", NamedTextColor.YELLOW)
                .append(Component.text(tzStr, NamedTextColor.WHITE)));

        final String audioStr;
        if (prefs.getAudioMinutes() != null) {
            audioStr = prefs.getAudioMinutes().isEmpty() ? "all intervals"
                    : ":" + prefs.getAudioMinutes().stream()
                            .map(m -> String.format("%02d", m))
                            .collect(Collectors.joining(", :"));
        } else {
            List<Integer> globalAudio = cfg.getAudioMinutes();
            String globalStr = globalAudio.isEmpty() ? "all intervals"
                    : ":" + globalAudio.stream()
                            .map(m -> String.format("%02d", m))
                            .collect(Collectors.joining(", :"));
            audioStr = "default (" + globalStr + ")";
        }
        player.sendMessage(Component.text("  Audio at: ", NamedTextColor.YELLOW)
                .append(Component.text(audioStr, NamedTextColor.WHITE)));

        player.sendMessage(Component.text("╚════════════════════════════════╝", NamedTextColor.GOLD));
    }

    // -------------------------------------------------------------------------
    // Tab completion
    // -------------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(SUBCOMMANDS, args[0]);
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "ring":
                if (args.length == 2) return filterStartsWith(MODES, args[1]);
                break;
            case "prefs":
                if (!(sender instanceof Player)) return Collections.emptyList();
                if (args.length == 2) return filterStartsWith(PREFS_SUBCOMMANDS, args[1]);
                if (args.length == 3) {
                    return switch (args[1].toLowerCase(Locale.ROOT)) {
                        case "mode"     -> filterStartsWith(NOTIFICATION_MODES, args[2]);
                        case "interval" -> filterStartsWith(INTERVALS, args[2]);
                        case "timezone" -> filterStartsWith(TIMEZONE_SUGGESTIONS, args[2]);
                        case "audio"    -> filterStartsWith(AUDIO_SUGGESTIONS, args[2]);
                        default         -> Collections.emptyList();
                    };
                }
                break;
        }
        return Collections.emptyList();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static List<String> filterStartsWith(List<String> list, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return list.stream().filter(s -> s.startsWith(lower)).collect(Collectors.toList());
    }

    private static Component usage(String label) {
        return Component.text(
                "Usage: /" + label + " <reload | ring [chat|actionbar|title] | prefs [mode|interval|timezone|audio]>",
                NamedTextColor.YELLOW);
    }
}
