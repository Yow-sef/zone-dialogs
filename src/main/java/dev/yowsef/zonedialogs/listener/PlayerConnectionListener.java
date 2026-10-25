package dev.yowsef.zonedialogs.listener;

import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerConnectionListener implements Listener {

    private final ZoneDialogsPlugin plugin;

    public PlayerConnectionListener(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.closeMenus(event.getPlayer());
        plugin.getQueueMenuController().forget(event.getPlayer());
        plugin.getLeaderboardMenuController().cleanupPlayer(event.getPlayer().getUniqueId());
        plugin.getSessionManager().removeSession(event.getPlayer().getUniqueId());
    }
}
