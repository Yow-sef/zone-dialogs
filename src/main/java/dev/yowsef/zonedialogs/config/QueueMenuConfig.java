package dev.yowsef.zonedialogs.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class QueueMenuConfig {

    public record CategoryDef(
            String id,
            String display,
            Material icon,
            List<String> ladders
    ) {}

    private String dialogTitle = "<aqua><b>ZonePractice Queue</b></aqua>";
    private String dialogExternalTitle = "<dark_gray>[ZonePractice]</dark_gray>";
    private boolean dialogPause = false;
    private boolean canCloseEscape = true;
    private String afterAction = "NONE";
    private int bodyWidth = 310;

    private String headerSubtitle = "<aqua><b>Click a kit to join or leave its queue.</b></aqua>";
    private String headerSpacer = " ";

    private final Map<String, CategoryDef> categories = new LinkedHashMap<>();

    private String toggleQueueAllChecked = "<green>☑ Queue All</green>";
    private String toggleQueueAllUnchecked = "<gray>☐ Queue All</gray>";
    private String toggleKeepQueuingChecked = "<green>☑ Keep Queuing</green>";
    private String toggleKeepQueuingUnchecked = "<gray>☐ Keep Queuing</gray>";

    private boolean showBotPractice = true;
    private String buttonModeUnranked = "<gray>Unranked</gray>";
    private String buttonModeRanked = "<gold>Ranked</gold>";
    private String buttonRefresh = "<aqua>⟳ Refresh</aqua>";
    private String buttonLeaveAll = "<red>Leave All</red>";
    private String buttonBotPractice = "<green>Practice vs Bot</green>";

    private final Map<String, String> ladderColors = new HashMap<>();
    private final Map<String, Material> customIcons = new HashMap<>();

    private String progressBarVisibility = "queued-only";
    private int progressBarLength = 10;
    private String progressBarFilledChar = "<block:lime_concrete>";
    private String progressBarEmptyChar = "<block:black_concrete>";
    private String progressBarFilledColor = "";
    private String progressBarEmptyColor = "";
    private boolean showDivisionLabel = false;
    private String divisionLabelFormat = "<gray>Rank: </gray>{division_color}<b>{division_name}</b> <dark_gray>(</dark_gray><yellow>{current_elo}</yellow><gray>/</gray><gold>{next_elo} Elo</gold><dark_gray>)</dark_gray>";

    private boolean winsBarEnabled = true;
    private String winsBarVisibility = "inherit";
    private int winsBarLength = 10;
    private String winsBarFilledChar = "<block:orange_concrete>";
    private String winsBarEmptyChar = "<block:black_concrete>";
    private String winsBarFilledColor = "";
    private String winsBarEmptyColor = "";
    private List<Integer> winsBarMilestones = new ArrayList<>(List.of(5, 10, 25, 50, 100, 250, 500));
    private String winsBarCountType = "LADDER_TOTAL";
    private boolean winsBarShowLabel = false;
    private String winsBarLabelFormat = "<gray>Wins: </gray><gold><b>{current_wins}</b></gold><gray>/</gray><yellow>{target_wins}</yellow>";

    private String soundTabSwitch = "UI_BUTTON_CLICK";
    private String soundModeToggle = "UI_BUTTON_CLICK";
    private String soundToggleOption = "BLOCK_LEVER_CLICK";
    private String soundFavoriteToggle = "BLOCK_NOTE_BLOCK_CHIME";
    private String soundQueueJoin = "BLOCK_NOTE_BLOCK_PLING";
    private String soundQueueLeave = "BLOCK_NOTE_BLOCK_BASS";

    public void load(@NotNull File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection dialogSec = yaml.getConfigurationSection("dialog");
        if (dialogSec != null) {
            dialogTitle = dialogSec.getString("title", dialogTitle);
            dialogExternalTitle = dialogSec.getString("external-title", dialogExternalTitle);
            dialogPause = dialogSec.getBoolean("pause", false);
            canCloseEscape = dialogSec.getBoolean("can-close-with-escape", true);
            afterAction = dialogSec.getString("after-action", afterAction);
            bodyWidth = dialogSec.getInt("body-width", 310);
        }

        ConfigurationSection headerSec = yaml.getConfigurationSection("header");
        if (headerSec != null) {
            headerSubtitle = headerSec.getString("subtitle", headerSubtitle);
            headerSpacer = headerSec.getString("spacer", " ");
        }

        categories.clear();
        ConfigurationSection catSec = yaml.getConfigurationSection("categories");
        if (catSec != null) {
            for (String key : catSec.getKeys(false)) {
                ConfigurationSection c = catSec.getConfigurationSection(key);
                if (c == null) continue;
                String display = c.getString("display", key);
                String matStr = c.getString("icon", "BOOK");
                Material mat = Material.matchMaterial(matStr);
                if (mat == null) mat = Material.BOOK;
                List<String> ladders = c.getStringList("ladders");
                categories.put(key.toLowerCase(), new CategoryDef(key.toLowerCase(), display, mat, ladders));
            }
        }

        ConfigurationSection ctrlSec = yaml.getConfigurationSection("controls");
        if (ctrlSec != null) {
            showBotPractice = ctrlSec.getBoolean("show-bot-practice", showBotPractice);
            toggleQueueAllChecked = ctrlSec.getString("toggles.queue-all.checked", toggleQueueAllChecked);
            toggleQueueAllUnchecked = ctrlSec.getString("toggles.queue-all.unchecked", toggleQueueAllUnchecked);
            toggleKeepQueuingChecked = ctrlSec.getString("toggles.keep-queuing.checked", toggleKeepQueuingChecked);
            toggleKeepQueuingUnchecked = ctrlSec.getString("toggles.keep-queuing.unchecked", toggleKeepQueuingUnchecked);
            buttonModeUnranked = ctrlSec.getString("buttons.mode-unranked", buttonModeUnranked);
            buttonModeRanked = ctrlSec.getString("buttons.mode-ranked", buttonModeRanked);
            buttonRefresh = ctrlSec.getString("buttons.refresh", buttonRefresh);
            buttonLeaveAll = ctrlSec.getString("buttons.leave-all", buttonLeaveAll);
            buttonBotPractice = ctrlSec.getString("buttons.bot-practice", buttonBotPractice);
        }

        ladderColors.clear();
        customIcons.clear();
        ConfigurationSection ldSec = yaml.getConfigurationSection("ladder-display");
        if (ldSec != null) {
            ConfigurationSection colorSec = ldSec.getConfigurationSection("name-colors");
            if (colorSec != null) {
                for (String lName : colorSec.getKeys(false)) {
                    ladderColors.put(lName.toLowerCase(), colorSec.getString(lName, "<aqua>"));
                }
            }

            ConfigurationSection iconSec = ldSec.getConfigurationSection("custom-icons");
            if (iconSec != null) {
                for (String lName : iconSec.getKeys(false)) {
                    Material m = Material.matchMaterial(iconSec.getString(lName, ""));
                    if (m != null) customIcons.put(lName.toLowerCase(), m);
                }
            }
        }

        ConfigurationSection pbSec = yaml.getConfigurationSection("progress-bar");
        if (pbSec != null) {
            progressBarVisibility = pbSec.getString("visibility", "queued-only");
            progressBarLength = pbSec.getInt("length", 10);
            progressBarFilledChar = pbSec.getString("filled-char", "<block:lime_concrete>");
            progressBarEmptyChar = pbSec.getString("empty-char", "<block:black_concrete>");
            progressBarFilledColor = pbSec.getString("filled-color", "");
            progressBarEmptyColor = pbSec.getString("empty-color", "");
            showDivisionLabel = pbSec.getBoolean("show-division-label", false);
            divisionLabelFormat = pbSec.getString("label-format", divisionLabelFormat);

            ConfigurationSection winsSec = pbSec.getConfigurationSection("wins-bar");
            if (winsSec != null) {
                winsBarEnabled = winsSec.getBoolean("enabled", true);
                winsBarVisibility = winsSec.getString("visibility", "inherit");
                winsBarLength = winsSec.getInt("length", 10);
                winsBarFilledChar = winsSec.getString("filled-char", "<block:orange_concrete>");
                winsBarEmptyChar = winsSec.getString("empty-char", "<block:black_concrete>");
                winsBarFilledColor = winsSec.getString("filled-color", "");
                winsBarEmptyColor = winsSec.getString("empty-color", "");
                List<Integer> list = winsSec.getIntegerList("milestones");
                if (!list.isEmpty()) {
                    winsBarMilestones = new ArrayList<>(list);
                    Collections.sort(winsBarMilestones);
                } else if (winsSec.contains("target-wins")) {
                    winsBarMilestones = List.of(winsSec.getInt("target-wins", 50));
                }
                winsBarCountType = winsSec.getString("count-type", "LADDER_TOTAL");
                winsBarShowLabel = winsSec.getBoolean("show-label", false);
                winsBarLabelFormat = winsSec.getString("label-format", winsBarLabelFormat);
            }
        }

        ConfigurationSection sndSec = yaml.getConfigurationSection("sounds");
        if (sndSec != null) {
            soundTabSwitch = sndSec.getString("tab-switch", soundTabSwitch);
            soundModeToggle = sndSec.getString("mode-toggle", soundModeToggle);
            soundToggleOption = sndSec.getString("toggle-option", soundToggleOption);
            soundFavoriteToggle = sndSec.getString("favorite-toggle", soundFavoriteToggle);
            soundQueueJoin = sndSec.getString("queue-join", soundQueueJoin);
            soundQueueLeave = sndSec.getString("queue-leave", soundQueueLeave);
        }
    }

    public String getDialogTitle() { return dialogTitle; }
    public String getDialogExternalTitle() { return dialogExternalTitle; }
    public boolean isDialogPause() { return dialogPause; }
    public boolean isCanCloseEscape() { return canCloseEscape; }
    public String getAfterAction() { return afterAction; }
    public int getBodyWidth() { return bodyWidth; }
    public String getHeaderSubtitle() { return headerSubtitle; }
    public String getHeaderSpacer() { return headerSpacer; }
    public Map<String, CategoryDef> getCategories() { return Collections.unmodifiableMap(categories); }
    public String getToggleQueueAllChecked() { return toggleQueueAllChecked; }
    public String getToggleQueueAllUnchecked() { return toggleQueueAllUnchecked; }
    public String getToggleKeepQueuingChecked() { return toggleKeepQueuingChecked; }
    public String getToggleKeepQueuingUnchecked() { return toggleKeepQueuingUnchecked; }
    public boolean isShowBotPractice() { return showBotPractice; }
    public String getButtonModeUnranked() { return buttonModeUnranked; }
    public String getButtonModeRanked() { return buttonModeRanked; }
    public String getButtonRefresh() { return buttonRefresh; }
    public String getButtonLeaveAll() { return buttonLeaveAll; }
    public String getButtonBotPractice() { return buttonBotPractice; }
    public String getLadderColor(String ladder) { return ladderColors.getOrDefault(ladder.toLowerCase(), ladderColors.getOrDefault("default", "<aqua>")); }
    @org.jetbrains.annotations.Nullable
    public Material getCustomIcon(String ladder) { return customIcons.get(ladder.toLowerCase()); }
    public String getProgressBarVisibility() { return progressBarVisibility; }
    public int getProgressBarLength() { return progressBarLength; }
    public String getProgressBarFilledChar() { return progressBarFilledChar; }
    public String getProgressBarEmptyChar() { return progressBarEmptyChar; }
    public String getProgressBarFilledColor() { return progressBarFilledColor; }
    public String getProgressBarEmptyColor() { return progressBarEmptyColor; }
    public boolean isShowDivisionLabel() { return showDivisionLabel; }
    public String getDivisionLabelFormat() { return divisionLabelFormat; }
    public String getSoundTabSwitch() { return soundTabSwitch; }
    public String getSoundModeToggle() { return soundModeToggle; }
    public String getSoundToggleOption() { return soundToggleOption; }
    public String getSoundQueueJoin() { return soundQueueJoin; }
    public String getSoundQueueLeave() { return soundQueueLeave; }
    public String getSoundFavoriteToggle() { return soundFavoriteToggle; }

    public boolean isWinsBarEnabled() { return winsBarEnabled; }
    public String getWinsBarVisibility() { return winsBarVisibility; }
    public int getWinsBarLength() { return winsBarLength; }
    public String getWinsBarFilledChar() { return winsBarFilledChar; }
    public String getWinsBarEmptyChar() { return winsBarEmptyChar; }
    public String getWinsBarFilledColor() { return winsBarFilledColor; }
    public String getWinsBarEmptyColor() { return winsBarEmptyColor; }
    public List<Integer> getWinsBarMilestones() { return Collections.unmodifiableList(winsBarMilestones); }
    public String getWinsBarCountType() { return winsBarCountType; }
    public boolean isWinsBarShowLabel() { return winsBarShowLabel; }
    public String getWinsBarLabelFormat() { return winsBarLabelFormat; }
}
