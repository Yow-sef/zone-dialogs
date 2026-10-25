package dev.yowsef.zonedialogs.menu.stats;

import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.session.MenuType;
import dev.yowsef.zonedialogs.session.PlayerSession;
import dev.yowsef.zonedialogs.util.ComponentUtils;
import io.papermc.paper.dialog.Dialog;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatsMenuController {

    private final ZoneDialogsPlugin plugin;
    private final StatsDialogBuilder dialogBuilder;

    public StatsMenuController(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
        this.dialogBuilder = new StatsDialogBuilder(plugin, this);
    }

    public void open(@NotNull Player viewer, @NotNull Player target) {
        if (!viewer.isOnline()) return;

        PlayerSession session = plugin.getSessionManager().getSession(viewer);
        plugin.getQueueMenuController().forget(viewer);
        session.setActiveMenu(MenuType.STATS);

        try {
            Dialog dialog = dialogBuilder.build(viewer, target);
            viewer.showDialog(dialog);
            ComponentUtils.playSound(viewer, plugin.getConfigManager().getStatsMenuConfig().getSoundOpen());
        } catch (Throwable t) {
            session.setActiveMenu(MenuType.NONE);
            plugin.getLogger().warning("Failed to open stats menu for " + viewer.getName() + ": " + t);
            viewer.sendMessage(ComponentUtils.parse(viewer,
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
        if (!session.isMenuOpen(MenuType.STATS)) return;
        close(player);
    }

    public void handleMainMenu(@NotNull Player player) {
        close(player);
        plugin.getQueueMenuController().openMenu(player);
    }
}
