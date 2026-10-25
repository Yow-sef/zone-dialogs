package dev.yowsef.zonedialogs.menu.queue;

import dev.nandi0813.api.Interface.BotRegistry;
import dev.nandi0813.api.ZonePracticeApi;
import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.bridge.ZonePracticeBridge;
import dev.yowsef.zonedialogs.config.QueueMenuConfig;
import dev.yowsef.zonedialogs.session.MenuType;
import dev.yowsef.zonedialogs.session.PlayerSession;
import dev.yowsef.zonedialogs.util.ComponentUtils;
import io.papermc.paper.dialog.Dialog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnstableApiUsage")
public class QueueMenuController {

    private final ZoneDialogsPlugin plugin;
    private final QueueDialogBuilder dialogBuilder;
    private final Set<UUID> openDialogPlayers = ConcurrentHashMap.newKeySet();

    public QueueMenuController(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
        this.dialogBuilder = new QueueDialogBuilder(plugin, this);
    }

    public void open(@NotNull Player player) {
        openMenu(player);
    }

    public void openMenu(@NotNull Player player) {
        if (!player.isOnline()) return;

        PlayerSession session = plugin.getSessionManager().getSession(player);
        session.setActiveMenu(MenuType.QUEUE);
        openDialogPlayers.add(player.getUniqueId());

        try {
            Dialog dialog = dialogBuilder.build(player);
            player.showDialog(dialog);
        } catch (Throwable t) {
            session.setActiveMenu(MenuType.NONE);
            openDialogPlayers.remove(player.getUniqueId());
            plugin.getLogger().warning("Failed to open queue menu for " + player.getName() + ": " + t);
            send(player, plugin.getConfigManager().getMessageMenuOpenFailed());
        }
    }

    public void refresh(@NotNull Player player) {
        if (!player.isOnline()) {
            openDialogPlayers.remove(player.getUniqueId());
            return;
        }
        PlayerSession session = plugin.getSessionManager().getSession(player);
        if (!session.isMenuOpen(MenuType.QUEUE)) {
            openDialogPlayers.remove(player.getUniqueId());
            return;
        }

        try {
            Dialog dialog = dialogBuilder.build(player);
            player.showDialog(dialog);
        } catch (Throwable t) {
            session.setActiveMenu(MenuType.NONE);
            openDialogPlayers.remove(player.getUniqueId());
            plugin.getLogger().warning("Failed to refresh queue menu for " + player.getName() + ": " + t);
        }
    }

    public void handleClose(@NotNull Player player) {
        PlayerSession session = plugin.getSessionManager().getSession(player);
        session.setActiveMenu(MenuType.NONE);
        openDialogPlayers.remove(player.getUniqueId());
        try {
            player.closeDialog();
        } catch (Throwable ignored) {
        }
    }

    /**
     * Stops periodic queue-menu refreshes without touching the player's open
     * dialog. Used when another ZoneDialogs menu replaces this one.
     */
    public void forget(@NotNull Player player) {
        openDialogPlayers.remove(player.getUniqueId());
    }

    public void handleCategorySwitch(@NotNull Player player, @NotNull String categoryId) {
        PlayerSession session = plugin.getSessionManager().getSession(player);
        session.setSelectedCategory(categoryId);
        QueueMenuConfig config = plugin.getConfigManager().getQueueMenuConfig();
        ComponentUtils.playSound(player, config.getSoundTabSwitch());

        if (session.isQueueAll()) {
            queueCategory(player, session);
        }
        refresh(player);
    }

    public void handleModeToggle(@NotNull Player player) {
        PlayerSession session = plugin.getSessionManager().getSession(player);
        session.toggleRanked();

        // Rebuild auto-queued categories in the new mode so the displayed mode
        // matches the queues the player is actually in.
        if (session.isQueueAll()) {
            leaveCategory(player, session);
            queueCategory(player, session);
        }

        QueueMenuConfig config = plugin.getConfigManager().getQueueMenuConfig();
        ComponentUtils.playSound(player, config.getSoundModeToggle());
        refresh(player);
    }

    public void handleQueueAllToggle(@NotNull Player player) {
        PlayerSession session = plugin.getSessionManager().getSession(player);
        QueueMenuConfig config = plugin.getConfigManager().getQueueMenuConfig();

        if (!session.isQueueAll()) {
            if (!ZonePracticeBridge.isMultiQueueAllowed(player)) {
                ComponentUtils.playSound(player, config.getSoundQueueLeave());
                send(player, plugin.getConfigManager().getMessageMultiQueueUnavailable());
                return;
            }
            session.setQueueAll(true);
            queueCategory(player, session);
        } else {
            session.setQueueAll(false);
            ZonePracticeBridge.leaveQueue(player);
            send(player, plugin.getConfigManager().getMessageLeftQueue());
        }

        ComponentUtils.playSound(player, config.getSoundToggleOption());
        refresh(player);
    }

    public void handleKeepQueuingToggle(@NotNull Player player) {
        PlayerSession session = plugin.getSessionManager().getSession(player);
        session.toggleKeepQueuing();
        QueueMenuConfig config = plugin.getConfigManager().getQueueMenuConfig();
        ComponentUtils.playSound(player, config.getSoundToggleOption());
        refresh(player);
    }

    public void handleFavoriteToggle(@NotNull Player player, @NotNull String ladder) {
        PlayerSession session = plugin.getSessionManager().getSession(player);
        session.toggleFavorite(ladder);
        QueueMenuConfig config = plugin.getConfigManager().getQueueMenuConfig();
        ComponentUtils.playSound(player, config.getSoundFavoriteToggle());
        refresh(player);
    }

    public void handleLadderQueue(@NotNull Player player, @NotNull String ladder) {
        PlayerSession session = plugin.getSessionManager().getSession(player);
        QueueMenuConfig config = plugin.getConfigManager().getQueueMenuConfig();

        if (ZonePracticeBridge.isLadderQueued(player, ladder)) {
            ZonePracticeBridge.leaveLadderQueue(player, ladder);
            ComponentUtils.playSound(player, config.getSoundQueueLeave());
        } else {
            boolean hadOtherQueues = !ZonePracticeBridge.getQueuedLadderNames(player).isEmpty();
            boolean success = ZonePracticeBridge.queueLadder(player, ladder, session.isRanked());
            if (success) {
                ComponentUtils.playSound(player, config.getSoundQueueJoin());
            } else if (hadOtherQueues && !ZonePracticeBridge.isMultiQueueAllowed(player)) {
                ComponentUtils.playSound(player, config.getSoundQueueLeave());
                send(player, plugin.getConfigManager().getMessageMultiQueueUnavailable());
            } else {
                ComponentUtils.playSound(player, config.getSoundQueueLeave());
                send(player, plugin.getConfigManager().getMessageQueueFailed());
            }
        }

        // ZonePractice updates its state during the same tick.
        Bukkit.getScheduler().runTaskLater(plugin, () -> refresh(player), 1L);
    }

    public void handleLeaveQueue(@NotNull Player player) {
        ZonePracticeBridge.leaveQueue(player);
        QueueMenuConfig config = plugin.getConfigManager().getQueueMenuConfig();
        ComponentUtils.playSound(player, config.getSoundQueueLeave());
        send(player, plugin.getConfigManager().getMessageLeftQueue());
        Bukkit.getScheduler().runTaskLater(plugin, () -> refresh(player), 1L);
    }

    public void handleRefresh(@NotNull Player player) {
        ComponentUtils.playSound(player, plugin.getConfigManager().getQueueMenuConfig().getSoundTabSwitch());
        refresh(player);
    }

    public void handleBotPractice(@NotNull Player player) {
        if (!ZonePracticeApi.isAvailable()) return;

        BotRegistry registry = ZonePracticeApi.getInstance().getBotRegistry();
        if (registry == null || !registry.isAvailable() || registry.getProvider() == null) {
            return;
        }

        handleClose(player);
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                registry.openSetup(player, registry.getProvider().getDefaultOptions());
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to open bot practice for " + player.getName() + ": " + t);
                send(player, plugin.getConfigManager().getMessageMenuOpenFailed());
            }
        });
    }

    /**
     * Queues every ladder in the current category that is not already queued.
     *
     * @return the number of queue attempts that succeeded
     */
    public int queueCategory(@NotNull Player player, @NotNull PlayerSession session) {
        if (!ZonePracticeBridge.isMultiQueueAllowed(player)) {
            return 0;
        }

        Set<String> alreadyQueued = lowerSet(ZonePracticeBridge.getQueuedLadderNames(player));
        int queued = 0;
        for (String ladder : dialogBuilder.resolveLaddersToDisplay(session)) {
            if (alreadyQueued.contains(ladder.toLowerCase(Locale.ROOT))) continue;
            if (ZonePracticeBridge.queueLadder(player, ladder, session.isRanked())) {
                queued++;
            }
        }

        if (queued > 0) {
            ComponentUtils.playSound(player, plugin.getConfigManager().getQueueMenuConfig().getSoundQueueJoin());
        }
        return queued;
    }

    /**
     * Leaves the queues for ladders in the current category.
     */
    public void leaveCategory(@NotNull Player player, @NotNull PlayerSession session) {
        Set<String> queued = lowerSet(ZonePracticeBridge.getQueuedLadderNames(player));
        for (String ladder : dialogBuilder.resolveLaddersToDisplay(session)) {
            if (queued.contains(ladder.toLowerCase(Locale.ROOT))) {
                ZonePracticeBridge.leaveLadderQueue(player, ladder);
            }
        }
        ComponentUtils.playSound(player, plugin.getConfigManager().getQueueMenuConfig().getSoundQueueLeave());
    }

    public int getLadderPlayerCount(@NotNull String ladderName, boolean ranked) {
        return ZonePracticeBridge.getLadderQueuedCount(ladderName, ranked);
    }

    public boolean hasOpenDialog(@NotNull Player player) {
        PlayerSession session = plugin.getSessionManager().getSession(player);
        return session.isMenuOpen(MenuType.QUEUE);
    }

    public void refreshAllOpen() {
        for (UUID uuid : List.copyOf(openDialogPlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                openDialogPlayers.remove(uuid);
                continue;
            }
            refresh(player);
        }
    }

    private Set<String> lowerSet(List<String> values) {
        Set<String> result = new HashSet<>();
        for (String value : values) {
            result.add(value.toLowerCase(Locale.ROOT));
        }
        return result;
    }

    private void send(@NotNull Player player, @NotNull String message) {
        player.sendMessage(ComponentUtils.parse(player, plugin.getConfigManager().prefix(message)));
    }
}
