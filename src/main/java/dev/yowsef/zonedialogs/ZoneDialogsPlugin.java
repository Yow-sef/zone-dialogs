package dev.yowsef.zonedialogs;

import dev.nandi0813.api.ZonePracticeApi;
import dev.yowsef.zonedialogs.bridge.ZonePracticeBridge;
import dev.yowsef.zonedialogs.command.ZoneDialogsCommand;
import dev.yowsef.zonedialogs.config.ConfigManager;
import dev.yowsef.zonedialogs.listener.PlayerConnectionListener;
import dev.yowsef.zonedialogs.listener.QueueCommandListener;
import dev.yowsef.zonedialogs.listener.ZonePracticeListener;
import dev.yowsef.zonedialogs.menu.queue.QueueMenuController;
import dev.yowsef.zonedialogs.session.PlayerSession;
import dev.yowsef.zonedialogs.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class ZoneDialogsPlugin extends JavaPlugin {

    private static ZoneDialogsPlugin instance;

    private ConfigManager configManager;
    private SessionManager sessionManager;
    private QueueMenuController queueMenuController;
    private dev.yowsef.zonedialogs.menu.leaderboard.LeaderboardMenuController leaderboardMenuController;
    private dev.yowsef.zonedialogs.menu.stats.StatsMenuController statsMenuController;
    private BukkitTask refreshTask;

    public static ZoneDialogsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        int apiVersion = readApiVersion();
        if (apiVersion < 8) {
            getLogger().warning("ZonePractice Pro with API 2.9.0+ required (found API value "
                    + (apiVersion < 0 ? "unknown" : apiVersion) + "). Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.configManager = new ConfigManager(this);
        this.configManager.reload();

        this.sessionManager = new SessionManager(this);
        this.queueMenuController = new QueueMenuController(this);
        this.leaderboardMenuController = new dev.yowsef.zonedialogs.menu.leaderboard.LeaderboardMenuController(this);
        this.statsMenuController = new dev.yowsef.zonedialogs.menu.stats.StatsMenuController(this);

        getServer().getPluginManager().registerEvents(new ZonePracticeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new QueueCommandListener(this), this);

        PluginCommand zdCmd = getCommand("zonedialogs");
        if (zdCmd != null) {
            ZoneDialogsCommand cmd = new ZoneDialogsCommand(this);
            zdCmd.setExecutor(cmd);
            zdCmd.setTabCompleter(cmd);
        }

        PluginCommand dqCmd = getCommand("dqueue");
        if (dqCmd != null) {
            dqCmd.setExecutor(new dev.yowsef.zonedialogs.command.DQueueCommand(this));
        }

        PluginCommand dstatsCmd = getCommand("dstats");
        if (dstatsCmd != null) {
            dev.yowsef.zonedialogs.command.DStatsCommand cmd = new dev.yowsef.zonedialogs.command.DStatsCommand(this);
            dstatsCmd.setExecutor(cmd);
            dstatsCmd.setTabCompleter(cmd);
        }

        PluginCommand dtopCmd = getCommand("dtop");
        if (dtopCmd != null) {
            dev.yowsef.zonedialogs.command.DTopCommand cmd = new dev.yowsef.zonedialogs.command.DTopCommand(this);
            dtopCmd.setExecutor(cmd);
            dtopCmd.setTabCompleter(cmd);
        }

        startRefreshTicker();
        validateConfiguredLadders();
        getLogger().info("ZoneDialogs v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }

        if (sessionManager != null) {
            sessionManager.saveAll();
        }

        instance = null;
        getLogger().info("ZoneDialogs disabled.");
    }

    /**
     * Reloads configuration and everything that was captured at startup
     * (refresh interval, division thresholds, ladder validation).
     */
    public void reloadRuntime() {
        configManager.reload();
        startRefreshTicker();
        validateConfiguredLadders();
    }

    private void startRefreshTicker() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }

        int interval = configManager.getAutoRefreshIntervalTicks();
        if (interval > 0) {
            // Re-showing a dialog resets its scroll position. Keep an accidental
            // 1-second timer from making the menu unusable.
            interval = Math.max(100, interval);
            refreshTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
                if (queueMenuController != null) {
                    queueMenuController.refreshAllOpen();
                }
            }, interval, interval);
        }
    }

    private int readApiVersion() {
        if (!ZonePracticeApi.isAvailable()) return -1;
        try {
            // API_VERSION is a compile-time constant, so reading the field
            // directly gets inlined into this jar. Reflection reads the value
            // of the API class that the server is actually running.
            return (int) ZonePracticeApi.class.getField("API_VERSION").getInt(null);
        } catch (ReflectiveOperationException e) {
            return -1;
        }
    }

    public void validateConfiguredLadders() {
        Set<String> known = new HashSet<>();
        for (String name : ZonePracticeBridge.getLadderNames()) {
            known.add(name.toLowerCase(Locale.ROOT));
        }
        if (known.isEmpty()) return;

        for (var category : configManager.getQueueMenuConfig().getCategories().values()) {
            for (String ladder : category.ladders()) {
                if (!known.contains(ladder.toLowerCase(Locale.ROOT))) {
                    getLogger().warning("Queue category '" + category.id()
                            + "' references unknown ladder '" + ladder + "'.");
                }
            }
        }

        if (configManager.isAutoDetectLadders()) {
            String fallback = configManager.getFallbackCategory().toLowerCase(Locale.ROOT);
            if (!configManager.getQueueMenuConfig().getCategories().containsKey(fallback)
                    && !configManager.getQueueMenuConfig().getCategories().isEmpty()) {
                getLogger().warning("Fallback queue category '" + configManager.getFallbackCategory()
                        + "' does not exist; auto-detected ladders will not be shown.");
            }

            Set<String> configured = new HashSet<>();
            for (var category : configManager.getQueueMenuConfig().getCategories().values()) {
                for (String ladder : category.ladders()) {
                    configured.add(ladder.toLowerCase(Locale.ROOT));
                }
            }
            for (String name : ZonePracticeBridge.getLadderNames()) {
                if (!configured.contains(name.toLowerCase(Locale.ROOT))) {
                    getLogger().info("Auto-detected ladder '" + name
                            + "'; it will appear in the fallback category '"
                            + configManager.getFallbackCategory() + "'.");
                }
            }
        }
    }

    /**
     * Closes whichever ZoneDialogs menu is open for a player.
     */
    public void closeMenus(@NotNull Player player) {
        PlayerSession session = sessionManager.getLoadedSession(player.getUniqueId());
        if (session == null || !session.hasMenuOpen()) return;

        switch (session.getActiveMenu()) {
            case QUEUE -> queueMenuController.handleClose(player);
            case LEADERBOARD -> leaderboardMenuController.closeIfOpen(player);
            case STATS -> statsMenuController.closeIfOpen(player);
            default -> { }
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public QueueMenuController getQueueMenuController() {
        return queueMenuController;
    }

    public dev.yowsef.zonedialogs.menu.leaderboard.LeaderboardMenuController getLeaderboardMenuController() {
        return leaderboardMenuController;
    }

    public dev.yowsef.zonedialogs.menu.stats.StatsMenuController getStatsMenuController() {
        return statsMenuController;
    }
}
