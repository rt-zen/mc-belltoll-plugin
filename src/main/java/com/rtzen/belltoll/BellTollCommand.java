package com.rtzen.belltoll;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class BellTollCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "ring");
    private static final List<String> MODES = List.of("chat", "actionbar", "title");

    private final BellTollPlugin plugin;

    public BellTollCommand(BellTollPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + label + " <reload|ring [chat|actionbar|title]>"));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                if (!sender.hasPermission("belltoll.reload")) {
                    sender.sendMessage(Component.text("You don't have permission to do that."));
                    return true;
                }
                plugin.reloadClockConfig();
                sender.sendMessage(Component.text("[" + plugin.getName() + "] Configuration reloaded."));
            }
            case "ring" -> {
                if (!sender.hasPermission("belltoll.ring")) {
                    sender.sendMessage(Component.text("You don't have permission to do that."));
                    return true;
                }
                DisplayMode override = args.length >= 2 ? DisplayMode.fromString(args[1]) : null;
                plugin.getClockTask().sendTimeMessage(override);
                String modeNote = override != null ? " (" + override.name().toLowerCase(Locale.ROOT) + ")" : "";
                sender.sendMessage(Component.text("[" + plugin.getName() + "] Bell tolled" + modeNote + "."));
            }
            default -> sender.sendMessage(
                    Component.text("Usage: /" + label + " <reload|ring [chat|actionbar|title]>"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && "ring".equalsIgnoreCase(args[0])) {
            String input = args[1].toLowerCase(Locale.ROOT);
            return MODES.stream()
                    .filter(m -> m.startsWith(input))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
