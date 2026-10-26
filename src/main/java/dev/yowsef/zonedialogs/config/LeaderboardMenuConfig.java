package dev.yowsef.zonedialogs.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class LeaderboardMenuConfig {

    public record MetricDef(
            String id,
            String display,
            String name,
            Material icon
    ) {}

    private String dialogTitle = "<gold><b>Leaderboards</b></gold>";
    private String dialogExternalTitle = "<dark_gray>[ZonePractice Top]</dark_gray>";
    private boolean dialogPause = false;
    private boolean canCloseEscape = true;
    private String afterAction = "NONE";
    private int bodyWidth = 310;
    private String headerSubtitle = "<gold><b>Leaderboards</b></gold> <gray>• Select ladder and metric</gray>";

    private final Map<String, MetricDef> metrics = new LinkedHashMap<>();
    private int maxEntries = 10;
    private int kitsPerPage = 4;
    private int metricsPerRow = 4;
    private boolean showPersonalRankFooter = true;
    private boolean showPlayerHeads = true;
    private String rowFormat = "{head}{rank_color}<b>{rank}</b> {player}{you} <dark_gray>-</dark_gray> <white>{score}</white> <gray>{metric}</gray>";
    private String footerFormat = "<dark_gray>------------------------</dark_gray><newline><gray>Your Rank: </gray><gold><b>#{rank}</b></gold> <dark_gray>(</dark_gray><yellow>{score}</yellow><dark_gray>)</dark_gray>";

    private String soundTabSwitch = "UI_BUTTON_CLICK";
    private String soundMetricSwitch = "UI_BUTTON_CLICK";

    public void load(@NotNull File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection dialogSec = yaml.getConfigurationSection("dialog");
        if (dialogSec != null) {
            dialogTitle = dialogSec.getString("title", dialogTitle);
            dialogExternalTitle = dialogSec.getString("external-title", dialogExternalTitle);
            dialogPause = dialogSec.getBoolean("pause", dialogPause);
            canCloseEscape = dialogSec.getBoolean("can-close-with-escape", canCloseEscape);
            afterAction = dialogSec.getString("after-action", afterAction);
            bodyWidth = dialogSec.getInt("body-width", bodyWidth);
        }

        headerSubtitle = yaml.getString("header.subtitle", headerSubtitle);

        ConfigurationSection metricSec = yaml.getConfigurationSection("metrics");
        metrics.clear();
        if (metricSec != null) {
            for (String key : metricSec.getKeys(false)) {
                ConfigurationSection m = metricSec.getConfigurationSection(key);
                if (m == null) continue;
                String display = m.getString("display", key.toUpperCase());
                String name = m.getString("name", display);
                String matStr = m.getString("icon", "EXPERIENCE_BOTTLE");
                Material mat = Material.matchMaterial(matStr);
                if (mat == null) mat = Material.EXPERIENCE_BOTTLE;
                metrics.put(key.toLowerCase(), new MetricDef(key.toLowerCase(), display, name, mat));
            }
        }
        if (metrics.isEmpty()) {
            metrics.put("elo", new MetricDef("elo", "ELO", "Elo Rating", Material.EXPERIENCE_BOTTLE));
            metrics.put("wins", new MetricDef("wins", "WINS", "Total Wins", Material.NETHER_STAR));
            metrics.put("win_streak", new MetricDef("win_streak", "STREAK", "Win Streak", Material.BLAZE_POWDER));
        }

        ConfigurationSection dispSec = yaml.getConfigurationSection("display");
        if (dispSec != null) {
            maxEntries = Math.max(1, dispSec.getInt("max-entries", maxEntries));
            kitsPerPage = Math.max(1, dispSec.getInt("kits-per-page", kitsPerPage));
            metricsPerRow = Math.max(1, dispSec.getInt("metrics-per-row", metricsPerRow));
            showPersonalRankFooter = dispSec.getBoolean("show-personal-rank-footer", showPersonalRankFooter);
            showPlayerHeads = dispSec.getBoolean("show-player-heads", showPlayerHeads);
            rowFormat = dispSec.getString("row-format", rowFormat);
            footerFormat = dispSec.getString("footer-format", footerFormat);
        }

        ConfigurationSection sndSec = yaml.getConfigurationSection("sounds");
        if (sndSec != null) {
            soundTabSwitch = sndSec.getString("tab-switch", soundTabSwitch);
            soundMetricSwitch = sndSec.getString("metric-switch", soundMetricSwitch);
        }
    }

    public String getDialogTitle() { return dialogTitle; }
    public String getDialogExternalTitle() { return dialogExternalTitle; }
    public boolean isDialogPause() { return dialogPause; }
    public boolean isCanCloseEscape() { return canCloseEscape; }
    public String getAfterAction() { return afterAction; }
    public int getBodyWidth() { return bodyWidth; }
    public String getHeaderSubtitle() { return headerSubtitle; }
    public Map<String, MetricDef> getMetrics() { return Collections.unmodifiableMap(metrics); }
    public int getMaxEntries() { return maxEntries; }
    public int getKitsPerPage() { return kitsPerPage; }
    public int getMetricsPerRow() { return metricsPerRow; }
    public boolean isShowPersonalRankFooter() { return showPersonalRankFooter; }
    public boolean isShowPlayerHeads() { return showPlayerHeads; }
    public String getRowFormat() { return rowFormat; }
    public String getFooterFormat() { return footerFormat; }
    public String getSoundTabSwitch() { return soundTabSwitch; }
    public String getSoundMetricSwitch() { return soundMetricSwitch; }
}
