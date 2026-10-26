package dev.yowsef.zonedialogs.command;

import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.util.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DStatsCommand implements CommandExecutor, TabCompleter {

    private final ZoneDialogsPlugin plugin;

    public DStatsCommand(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                    plugin.getConfigManager().getMessageMustBePlayer())));
            return true;
        }

        if (!viewer.hasPermission("zonedialogs.stats")) {
            viewer.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                    plugin.getConfigManager().getMessageNoPermission())));
            return true;
        }

        Player target = viewer;
        if (args.length > 0) {
            Player found = Bukkit.getPlayer(args[0]);
            if (found != null) {
                target = found;
            } else {
                viewer.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                        plugin.getConfigManager().getMessagePlayerOffline().replace("{player}", args[0]))));
                return true;
            }
        }

        plugin.getStatsMenuController().open(viewer, target);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    list.add(p.getName());
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
