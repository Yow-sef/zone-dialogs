package dev.yowsef.zonedialogs.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class StatsMenuConfig {

    private String dialogTitle = "<aqua><b>Player Profile & Statistics</b></aqua>";
    private String dialogExternalTitle = "<dark_gray>[ZonePractice Profile]</dark_gray>";
    private boolean dialogPause = false;
    private boolean canCloseEscape = true;
    private String afterAction = "NONE";
    private int bodyWidth = 310;
    private String headerSubtitle = "<aqua><b>Player Profile & Statistics</b></aqua>";

    private boolean showHeadCard = true;
    private String overviewFormat = "<yellow><b>{player_name}</b></yellow> <dark_gray>•</dark_gray> <green>{division}</green><newline><gray>Matches: <white>{matches}</white> • Wins: <gold>{wins}</gold> • WLR: <yellow>{wlr}</yellow></gray><newline><gray>Kills: <green>{kills}</green> • Deaths: <red>{deaths}</red> • KDR: <yellow>{kdr}</yellow></gray><newline><gray>Best Streak: <aqua>{best_streak}</aqua></gray>";

    private boolean showLadders = true;
    private boolean showEloBar = true;
    private boolean showWinsBar = true;
    private int barLength = 10;
    private int maxLadders = 0;
    private String soundOpen = "UI_BUTTON_CLICK";

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

        ConfigurationSection ovSec = yaml.getConfigurationSection("overview");
        if (ovSec != null) {
            showHeadCard = ovSec.getBoolean("show-head-card", showHeadCard);
            overviewFormat = ovSec.getString("format", overviewFormat);
        }

        ConfigurationSection ladSec = yaml.getConfigurationSection("ladders");
        if (ladSec != null) {
            showLadders = ladSec.getBoolean("show-ladders", showLadders);
            showEloBar = ladSec.getBoolean("show-elo-bar", showEloBar);
            showWinsBar = ladSec.getBoolean("show-wins-bar", showWinsBar);
            barLength = Math.max(5, ladSec.getInt("bar-length", barLength));
            maxLadders = Math.max(0, ladSec.getInt("max-ladders", maxLadders));
        }

        ConfigurationSection sndSec = yaml.getConfigurationSection("sounds");
        if (sndSec != null) {
            soundOpen = sndSec.getString("open", soundOpen);
        }
    }

    public String getDialogTitle() { return dialogTitle; }
    public String getDialogExternalTitle() { return dialogExternalTitle; }
    public boolean isDialogPause() { return dialogPause; }
    public boolean isCanCloseEscape() { return canCloseEscape; }
    public String getAfterAction() { return afterAction; }
    public int getBodyWidth() { return bodyWidth; }
    public String getHeaderSubtitle() { return headerSubtitle; }
    public boolean isShowHeadCard() { return showHeadCard; }
    public String getOverviewFormat() { return overviewFormat; }
    public boolean isShowLadders() { return showLadders; }
    public boolean isShowEloBar() { return showEloBar; }
    public boolean isShowWinsBar() { return showWinsBar; }
    public int getBarLength() { return barLength; }
    public int getMaxLadders() { return maxLadders; }
    public String getSoundOpen() { return soundOpen; }
}
