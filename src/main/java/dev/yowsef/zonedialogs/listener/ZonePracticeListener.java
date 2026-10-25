package dev.yowsef.zonedialogs.listener;

import dev.nandi0813.api.Enum.PlayerState;
import dev.nandi0813.api.Event.Match.MatchEndEvent;
import dev.nandi0813.api.Event.Match.MatchStartEvent;
import dev.nandi0813.api.Event.PlayerStateChangeEvent;
import dev.nandi0813.api.Event.Queue.QueueEndEvent;
import dev.nandi0813.api.Event.Queue.QueueStartEvent;
import dev.nandi0813.api.Event.ZonePracticeReadyEvent;
import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.bridge.ZonePracticeBridge;
import dev.yowsef.zonedialogs.session.PlayerSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ZonePracticeListener implements Listener {

    private static final long REQUEUE_DELAY_TICKS = 40L;
    private static final int MAX_REQUEUE_ATTEMPTS = 5;

    private final ZoneDialogsPlugin plugin;

    public ZonePracticeListener(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onZonePracticeReady(ZonePracticeReadyEvent event) {
        // Ladders, arenas and divisions are only guaranteed to exist now.
        plugin.getConfigManager().reloadDivisionThresholds();
        plugin.validateConfiguredLadders();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQueueStart(QueueStartEvent event) {
        if (event.isCancelled() || event.getQueue() == null) return;
        Player player = event.getQueue().getPlayer();
        if (player != null && player.isOnline()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getQueueMenuController().refresh(player), 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQueueEnd(QueueEndEvent event) {
        if (event.getQueue() == null) return;
        Player player = event.getQueue().getPlayer();
        if (player != null && player.isOnline()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getQueueMenuController().refresh(player), 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchStart(MatchStartEvent event) {
        if (event.isCancelled() || event.getMatch() == null) return;
        for (Player fighter : event.getMatch().getPlayers()) {
            if (fighter != null && fighter.isOnline()) {
                plugin.closeMenus(fighter);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerStateChange(PlayerStateChangeEvent event) {
        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) return;

        PlayerState to = event.getTo();
        if (to.isFighting() || to == PlayerState.SPECTATE
                || to == PlayerState.EDITOR || to == PlayerState.CUSTOM_EDITOR) {
            plugin.closeMenus(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchEnd(MatchEndEvent event) {
        if (event.getMatch() == null) return;

        String ladder = event.getMatch().getLadderName();
        boolean ranked = event.getMatch().isRanked();
        if (ladder == null || ladder.isBlank()) return;

        for (Player player : event.getMatch().getPlayers()) {
            if (player == null || !player.isOnline()) continue;

            PlayerSession session = plugin.getSessionManager().getSession(player);
            if (!session.isKeepQueuing()) continue;

            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> tryRequeue(player, ladder, ranked, 1), REQUEUE_DELAY_TICKS);
        }
    }

    private void tryRequeue(@NotNull Player player, @NotNull String ladder, boolean ranked, int attempt) {
        if (!player.isOnline() || attempt > MAX_REQUEUE_ATTEMPTS) return;

        PlayerSession session = plugin.getSessionManager().getSession(player);
        if (!session.isKeepQueuing()) return;

        PlayerState state = ZonePracticeBridge.getPlayerState(player);
        boolean canQueue = state.isInLobby() || state == PlayerState.QUEUE;

        if (!canQueue) {
            // The core usually teleports back to the lobby shortly after the
            // match-end event. Retry instead of silently dropping the requeue.
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> tryRequeue(player, ladder, ranked, attempt + 1), REQUEUE_DELAY_TICKS);
            return;
        }

        if (!ZonePracticeBridge.queueLadder(player, ladder, ranked) && attempt < MAX_REQUEUE_ATTEMPTS) {
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> tryRequeue(player, ladder, ranked, attempt + 1), REQUEUE_DELAY_TICKS);
        }
    }
}
