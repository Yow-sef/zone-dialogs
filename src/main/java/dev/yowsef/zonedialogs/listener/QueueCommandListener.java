package dev.yowsef.zonedialogs.listener;

import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Intercepts only the bare /queue command. Arguments are deliberately forwarded
 * to ZonePractice Pro so /queue sword, /queue leave and /queue accept keep
 * working exactly as the core defines them.
 */
public class QueueCommandListener implements Listener {

    private final ZoneDialogsPlugin plugin;

    public QueueCommandListener(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfigManager().isOverrideDefaultQueueCommand()) return;

        String message = event.getMessage().trim();
        if (!message.equalsIgnoreCase("/queue")) return;
        if (!event.getPlayer().hasPermission("zonedialogs.queue")) return;

        event.setCancelled(true);
        plugin.getQueueMenuController().openMenu(event.getPlayer());
    }
}
