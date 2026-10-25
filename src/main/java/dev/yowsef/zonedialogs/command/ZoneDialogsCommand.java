package dev.yowsef.zonedialogs.command;

import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.util.ComponentUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ZoneDialogsCommand implements CommandExecutor, TabCompleter {

    private final ZoneDialogsPlugin plugin;

    public ZoneDialogsCommand(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("open") || sub.equals("queue")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                        plugin.getConfigManager().getMessageMustBePlayer())));
                return true;
            }
            if (!player.hasPermission("zonedialogs.queue") && !player.hasPermission("zonedialogs.use")) {
                player.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                        plugin.getConfigManager().getMessageNoPermission())));
                return true;
            }
            plugin.getQueueMenuController().openMenu(player);
            return true;
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("zonedialogs.admin")) {
                sender.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                        plugin.getConfigManager().getMessageNoPermission())));
                return true;
            }
            plugin.reloadRuntime();
            sender.sendMessage(ComponentUtils.parse(
                    plugin.getConfigManager().prefix(plugin.getConfigManager().getMessageReloadSuccess())
            ));
            return true;
        }

        if (sub.equals("version") || sub.equals("info")) {
            sender.sendMessage(ComponentUtils.parse(
                    "<gradient:#38ef7d:#11998e><b>ZoneDialogs</b></gradient> <gray>v" + plugin.getDescription().getVersion() + " by Yowsef</gray>"
            ));
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ComponentUtils.parse(
                "<gradient:#38ef7d:#11998e><b>ZoneDialogs Commands:</b></gradient>\n" +
                "<gray>/zd open</gray> <dark_gray>-</dark_gray> <white>Open queue dialog</white>\n" +
                "<gray>/zd reload</gray> <dark_gray>-</dark_gray> <white>Reload configuration</white>\n" +
                "<gray>/zd info</gray> <dark_gray>-</dark_gray> <white>Plugin version</white>"
        ));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> options = List.of("open", "reload", "info", "help");
            for (String opt : options) {
                if (opt.startsWith(args[0].toLowerCase())) {
                    completions.add(opt);
                }
            }
        }
        return completions;
    }
}
