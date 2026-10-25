package dev.yowsef.zonedialogs.command;

import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.bridge.ZonePracticeBridge;
import dev.yowsef.zonedialogs.menu.leaderboard.LeaderboardSession;
import dev.yowsef.zonedialogs.util.ComponentUtils;
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

public class DTopCommand implements CommandExecutor, TabCompleter {

    private final ZoneDialogsPlugin plugin;

    public DTopCommand(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                    plugin.getConfigManager().getMessageMustBePlayer())));
            return true;
        }

        if (!player.hasPermission("zonedialogs.leaderboard")) {
            player.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                    plugin.getConfigManager().getMessageNoPermission())));
            return true;
        }

        LeaderboardSession session = plugin.getLeaderboardMenuController().getSession(player);
        if (args.length > 0) {
            String requested = args[0].toLowerCase();
            if (!requested.equals("global")) {
                String canonical = ZonePracticeBridge.canonicalLadderName(requested);
                if (canonical == null) {
                    player.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                            plugin.getConfigManager().getMessageLadderNotFound().replace("{ladder}", args[0]))));
                    return true;
                }
                requested = canonical;
            }
            session.setSelectedLadder(requested);
        }
        if (args.length > 1) {
            String metric = args[1].toLowerCase();
            if (!plugin.getConfigManager().getLeaderboardMenuConfig().getMetrics().containsKey(metric)) {
                player.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                        "<red>Unknown metric '" + args[1] + "'.</red>")));
                return true;
            }
            session.setSelectedMetric(metric);
        }

        plugin.getLeaderboardMenuController().open(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("global");
            list.addAll(ZonePracticeBridge.getValidRegisteredLadderNames());
            return list.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        } else if (args.length == 2) {
            return plugin.getConfigManager().getLeaderboardMenuConfig().getMetrics().keySet()
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
