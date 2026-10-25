package dev.yowsef.zonedialogs.command;

import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.util.ComponentUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DQueueCommand implements CommandExecutor {

    private final ZoneDialogsPlugin plugin;

    public DQueueCommand(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                    plugin.getConfigManager().getMessageMustBePlayer())));
            return true;
        }

        if (!player.hasPermission("zonedialogs.queue")) {
            player.sendMessage(ComponentUtils.parse(plugin.getConfigManager().prefix(
                    plugin.getConfigManager().getMessageNoPermission())));
            return true;
        }

        plugin.getQueueMenuController().open(player);
        return true;
    }
}
