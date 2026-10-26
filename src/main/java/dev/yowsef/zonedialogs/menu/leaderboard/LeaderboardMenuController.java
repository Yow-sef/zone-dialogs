package dev.yowsef.zonedialogs.menu.leaderboard;

import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.session.MenuType;
import dev.yowsef.zonedialogs.session.PlayerSession;
import dev.yowsef.zonedialogs.util.ComponentUtils;
import io.papermc.paper.dialog.Dialog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardMenuController {

    private final ZoneDialogsPlugin plugin;
    private final LeaderboardDialogBuilder dialogBuilder;
    private final Map<UUID, LeaderboardSession> sessions = new ConcurrentHashMap<>();

    public LeaderboardMenuController(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
        this.dialogBuilder = new LeaderboardDialogBuilder(plugin, this);
    }

    public void open(@NotNull Player player) {
        if (!player.isOnline()) return;

        PlayerSession session = plugin.getSessionManager().getSession(player);
        plugin.getQueueMenuController().forget(player);
        session.setActiveMenu(MenuType.LEADERBOARD);

        try {
            Dialog dialog = dialogBuilder.build(player);
            player.showDialog(dialog);
        } catch (Throwable t) {
            session.setActiveMenu(MenuType.NONE);
            plugin.getLogger().warning("Failed to open leaderboard for " + player.getName() + ": " + t);
            player.sendMessage(ComponentUtils.parse(player,
                    plugin.getConfigManager().prefix(plugin.getConfigManager().getMessageMenuOpenFailed())));
        }
    }

    public void close(@NotNull Player player) {
        PlayerSession session = plugin.getSessionManager().getSession(player);
        session.setActiveMenu(MenuType.NONE);
        try {
            player.closeDialog();
        } catch (Throwable ignored) {
        }
    }

    public void closeIfOpen(@NotNull Player player) {
        PlayerSession session = plugin.getSessionManager().getSession(player);
        if (!session.isMenuOpen(MenuType.LEADERBOARD)) return;
        close(player);
    }

    public void handleMetricSelect(@NotNull Player player, @NotNull String metric) {
        LeaderboardSession session = getSession(player);
        session.setSelectedMetric(metric);
        ComponentUtils.playSound(player, plugin.getConfigManager().getLeaderboardMenuConfig().getSoundMetricSwitch());
        open(player);
    }

    public void handleLadderSelect(@NotNull Player player, @NotNull String ladder) {
        LeaderboardSession session = getSession(player);
        session.setSelectedLadder(ladder);
        ComponentUtils.playSound(player, plugin.getConfigManager().getLeaderboardMenuConfig().getSoundTabSwitch());
        open(player);
    }

    public void handlePageChange(@NotNull Player player, int delta) {
        LeaderboardSession session = getSession(player);
        session.setLadderPage(session.getLadderPage() + delta);
        ComponentUtils.playSound(player, plugin.getConfigManager().getLeaderboardMenuConfig().getSoundTabSwitch());
        open(player);
    }

    public void handleSearch(@NotNull Player player, String query) {
        LeaderboardSession session = getSession(player);
        session.setSearchQuery(query);
        ComponentUtils.playSound(player, plugin.getConfigManager().getLeaderboardMenuConfig().getSoundMetricSwitch());
        open(player);
    }

    public void handleMainMenu(@NotNull Player player) {
        close(player);
        plugin.getQueueMenuController().openMenu(player);
    }

    public void handlePlayerStats(@NotNull Player viewer, @NotNull UUID targetId, @NotNull String targetName) {
        if (!viewer.hasPermission("zonedialogs.stats")) {
            viewer.sendMessage(ComponentUtils.parse(viewer, plugin.getConfigManager().prefix(
                    plugin.getConfigManager().getMessageNoPermission())));
            return;
        }
        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()) {
            viewer.sendMessage(ComponentUtils.parse(viewer, plugin.getConfigManager().prefix(
                    plugin.getConfigManager().getMessagePlayerOffline().replace("{player}", targetName))));
            return;
        }
        plugin.getStatsMenuController().open(viewer, target);
    }

    public LeaderboardSession getSession(@NotNull Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), k -> new LeaderboardSession());
    }

    public void cleanupPlayer(@NotNull UUID uuid) {
        sessions.remove(uuid);
    }
}
