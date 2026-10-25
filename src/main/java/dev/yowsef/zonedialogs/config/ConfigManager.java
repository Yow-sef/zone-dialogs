package dev.yowsef.zonedialogs.config;

import dev.nandi0813.api.Enum.DivisionName;
import dev.nandi0813.api.ZonePracticeApi;
import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final ZoneDialogsPlugin plugin;
    private final QueueMenuConfig queueMenuConfig = new QueueMenuConfig();
    private final LeaderboardMenuConfig leaderboardMenuConfig = new LeaderboardMenuConfig();
    private final StatsMenuConfig statsMenuConfig = new StatsMenuConfig();

    private boolean overrideDefaultQueueCommand = true;
    private int autoRefreshIntervalTicks = 20;
    private boolean autoDetectLadders = true;
    private String fallbackCategory = "skills";

    private final Map<String, Integer> divisionThresholds = new LinkedHashMap<>();

    private String messagePrefix = "<gradient:#38ef7d:#11998e>[ZoneDialogs]</gradient> ";
    private String messageNoPermission = "<red>You do not have permission to execute this command.</red>";
    private String messageReloadSuccess = "<green>ZoneDialogs configuration reloaded successfully!</green>";
    private String messageMustBePlayer = "<red>Only in-game players can open dialog menus.</red>";
    private String messageLeftQueue = "<gray>You have left the queue.</gray>";
    private String messageLadderNotFound = "<red>Ladder '{ladder}' was not found.</red>";
    private String messageQueueFailed = "<red>Could not join that queue right now.</red>";
    private String messageMultiQueueUnavailable = "<red>Multi-queue is not enabled for you.</red>";
    private String messageMenuOpenFailed = "<red>Could not open that menu. Check the server console.</red>";

    public ConfigManager(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        overrideDefaultQueueCommand = config.getBoolean("settings.override-default-queue-command", true);
        autoRefreshIntervalTicks = Math.max(0, config.getInt("settings.auto-refresh-interval-ticks", 0));
        autoDetectLadders = config.getBoolean("settings.auto-detect-ladders", true);
        fallbackCategory = config.getString("settings.fallback-category", "skills");

        loadDivisionThresholds(config);

        ConfigurationSection msgSec = config.getConfigurationSection("messages");
        if (msgSec != null) {
            messagePrefix = msgSec.getString("prefix", messagePrefix);
            messageNoPermission = msgSec.getString("no-permission", messageNoPermission);
            messageReloadSuccess = msgSec.getString("reload-success", messageReloadSuccess);
            messageMustBePlayer = msgSec.getString("must-be-player", messageMustBePlayer);
            messageLeftQueue = msgSec.getString("left-queue", messageLeftQueue);
            messageLadderNotFound = msgSec.getString("ladder-not-found", messageLadderNotFound);
            messageQueueFailed = msgSec.getString("queue-failed", messageQueueFailed);
            messageMultiQueueUnavailable = msgSec.getString("multi-queue-unavailable", messageMultiQueueUnavailable);
            messageMenuOpenFailed = msgSec.getString("menu-open-failed", messageMenuOpenFailed);
        }

        // Load menus
        File menuDir = new File(plugin.getDataFolder(), "menus");
        if (!menuDir.exists()) {
            menuDir.mkdirs();
        }

        File queueFile = new File(menuDir, "queue.yml");
        if (!queueFile.exists()) {
            plugin.saveResource("menus/queue.yml", false);
        }
        queueMenuConfig.load(queueFile);

        File lbFile = new File(menuDir, "leaderboard.yml");
        if (!lbFile.exists()) {
            plugin.saveResource("menus/leaderboard.yml", false);
        }
        leaderboardMenuConfig.load(lbFile);

        File statsFile = new File(menuDir, "stats.yml");
        if (!statsFile.exists()) {
            plugin.saveResource("menus/stats.yml", false);
        }
        statsMenuConfig.load(statsFile);
    }

    /**
     * Re-reads only the division thresholds. Called again when the core fires
     * ZonePracticeReadyEvent, in case divisions were not loaded at onEnable.
     */
    public void reloadDivisionThresholds() {
        loadDivisionThresholds(plugin.getConfig());
    }

    private void loadDivisionThresholds(FileConfiguration config) {
        LinkedHashMap<String, Integer> configured = new LinkedHashMap<>();
        ConfigurationSection dtSec = config.getConfigurationSection("division-thresholds");
        if (dtSec != null) {
            for (String key : dtSec.getKeys(false)) {
                configured.put(key, dtSec.getInt(key));
            }
        }

        // Prefer the core's divisions so the menu does not drift from divisions.yml.
        LinkedHashMap<String, Integer> fromApi = new LinkedHashMap<>();
        if (ZonePracticeApi.isAvailable()) {
            try {
                ZonePracticeApi api = ZonePracticeApi.getInstance();
                List<String> names = api.getDivisionNames(DivisionName.FULL);
                if (names != null) {
                    for (String name : names) {
                        int elo = api.getDivisionRequiredElo(name);
                        if (elo >= 0) fromApi.put(name, elo);
                    }
                }
            } catch (Throwable ignored) {
                // Older cores or an unavailable division config: use the YAML fallback.
            }
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(
                (fromApi.size() >= 2 ? fromApi : configured).entrySet()
        );
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));

        divisionThresholds.clear();
        for (Map.Entry<String, Integer> entry : entries) {
            divisionThresholds.put(entry.getKey(), entry.getValue());
        }

        if (divisionThresholds.isEmpty()) {
            divisionThresholds.put("Bronze", 0);
            divisionThresholds.put("Silver", 1000);
            divisionThresholds.put("Gold", 1200);
            divisionThresholds.put("Platinum", 1400);
            divisionThresholds.put("Diamond", 1600);
            divisionThresholds.put("Master", 1800);
            divisionThresholds.put("Grandmaster", 2000);
        }
    }

    public QueueMenuConfig getQueueMenuConfig() { return queueMenuConfig; }
    public LeaderboardMenuConfig getLeaderboardMenuConfig() { return leaderboardMenuConfig; }
    public StatsMenuConfig getStatsMenuConfig() { return statsMenuConfig; }
    public boolean isOverrideDefaultQueueCommand() { return overrideDefaultQueueCommand; }
    public int getAutoRefreshIntervalTicks() { return autoRefreshIntervalTicks; }
    public boolean isAutoDetectLadders() { return autoDetectLadders; }
    public String getFallbackCategory() { return fallbackCategory; }
    public Map<String, Integer> getDivisionThresholds() { return divisionThresholds; }

    public String getMessagePrefix() { return messagePrefix; }
    public String getMessageNoPermission() { return messageNoPermission; }
    public String getMessageReloadSuccess() { return messageReloadSuccess; }
    public String getMessageMustBePlayer() { return messageMustBePlayer; }
    public String getMessageLeftQueue() { return messageLeftQueue; }
    public String getMessageLadderNotFound() { return messageLadderNotFound; }
    public String getMessageQueueFailed() { return messageQueueFailed; }
    public String getMessageMultiQueueUnavailable() { return messageMultiQueueUnavailable; }
    public String getMessageMenuOpenFailed() { return messageMenuOpenFailed; }

    @NotNull
    public String prefix(@NotNull String message) {
        return messagePrefix + message;
    }
}
