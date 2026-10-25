package dev.yowsef.zonedialogs.menu.stats;

import dev.nandi0813.api.Enum.DivisionName;
import dev.nandi0813.api.Enum.WeightClass;
import dev.nandi0813.api.ZonePracticeApi;
import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.bridge.ZonePracticeBridge;
import dev.yowsef.zonedialogs.config.QueueMenuConfig;
import dev.yowsef.zonedialogs.config.StatsMenuConfig;
import dev.yowsef.zonedialogs.division.DivisionProgressCalculator;
import dev.yowsef.zonedialogs.division.ProgressBarRenderer;
import dev.yowsef.zonedialogs.division.WinsProgressCalculator;
import dev.yowsef.zonedialogs.util.ComponentUtils;
import dev.yowsef.zonedialogs.util.LadderNames;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnstableApiUsage")
public class StatsDialogBuilder {

    private final ZoneDialogsPlugin plugin;
    private final StatsMenuController controller;
    private final ProgressBarRenderer progressBarRenderer;
    private final WinsProgressCalculator winsCalculator;

    public StatsDialogBuilder(@NotNull ZoneDialogsPlugin plugin, @NotNull StatsMenuController controller) {
        this.plugin = plugin;
        this.controller = controller;
        this.progressBarRenderer = new ProgressBarRenderer();
        this.winsCalculator = new WinsProgressCalculator();
    }

    @NotNull
    public Dialog build(@NotNull Player viewer, @NotNull Player target) {
        StatsMenuConfig config = plugin.getConfigManager().getStatsMenuConfig();
        QueueMenuConfig queueConfig = plugin.getConfigManager().getQueueMenuConfig();
        DivisionProgressCalculator divisionCalculator =
                new DivisionProgressCalculator(plugin.getConfigManager().getDivisionThresholds());

        List<DialogBody> bodies = new ArrayList<>();
        int width = config.getBodyWidth();

        if (config.getHeaderSubtitle() != null && !config.getHeaderSubtitle().isBlank()) {
            bodies.add(DialogBody.plainMessage(ComponentUtils.parse(viewer, config.getHeaderSubtitle()), width));
        }

        if (config.isShowHeadCard()) {
            bodies.add(buildOverviewCard(viewer, target, config, queueConfig));
        }

        if (config.isShowLadders()) {
            bodies.add(DialogBody.plainMessage(
                    ComponentUtils.parse("<dark_gray>------------------------</dark_gray><newline><gray><b>Ladder Progression:</b></gray>"),
                    width
            ));

            List<String> ladders = ZonePracticeBridge.getValidRegisteredLadderNames();
            int limit = config.getMaxLadders() > 0
                    ? Math.min(config.getMaxLadders(), ladders.size())
                    : ladders.size();

            for (int i = 0; i < limit; i++) {
                bodies.add(buildLadderCard(target, ladders.get(i), queueConfig, config, divisionCalculator));
            }

            if (limit < ladders.size()) {
                bodies.add(DialogBody.plainMessage(
                        ComponentUtils.parse("<gray>… and <yellow>" + (ladders.size() - limit)
                                + "</yellow> more ladders.</gray>"),
                        width
                ));
            }
        }

        Component titleComp = ComponentUtils.parse(viewer, config.getDialogTitle());
        Component extTitleComp = ComponentUtils.parse(viewer, config.getDialogExternalTitle());

        DialogBase.DialogAfterAction afterAction;
        try {
            afterAction = DialogBase.DialogAfterAction.valueOf(config.getAfterAction().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            afterAction = DialogBase.DialogAfterAction.NONE;
        }

        DialogBase base = DialogBase.builder(titleComp)
                .externalTitle(extTitleComp)
                .pause(config.isDialogPause())
                .canCloseWithEscape(config.isCanCloseEscape())
                .afterAction(afterAction)
                .body(bodies)
                .build();

        ActionButton mainMenuButton = ActionButton.create(
                ComponentUtils.parse("<yellow>> Main Menu</yellow>"),
                ComponentUtils.parse("<gray>Return to queue menu</gray>"),
                110,
                DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        Bukkit.getScheduler().runTask(plugin, () -> controller.handleMainMenu(p));
                    }
                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build())
        );

        ActionButton closeButton = ActionButton.create(
                ComponentUtils.parse("<red>Close</red>"),
                ComponentUtils.parse("<gray>Close profile</gray>"),
                80,
                DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        Bukkit.getScheduler().runTask(plugin, () -> controller.close(p));
                    }
                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build())
        );

        return Dialog.create(b -> b.empty().base(base)
                .type(DialogType.multiAction(List.of(mainMenuButton), closeButton, 1)));
    }

    @NotNull
    private DialogBody buildOverviewCard(
            Player viewer,
            Player target,
            StatsMenuConfig config,
            QueueMenuConfig queueConfig
    ) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(target);
            item.setItemMeta(meta);
        }

        String division = "Unranked";
        int matches = 0;
        int wins = 0;
        int losses = 0;
        int kills = 0;
        int deaths = 0;
        int bestStreak = 0;

        if (ZonePracticeApi.isAvailable()) {
            ZonePracticeApi api = ZonePracticeApi.getInstance();
            String zpDiv = api.getPlayerDivision(target, DivisionName.FULL);
            if (zpDiv != null && !zpDiv.isBlank()) division = zpDiv;

            matches = Math.max(0, api.getMatchesPlayed(target));
            wins = Math.max(0, api.getGlobalWins(target));
            kills = Math.max(0, api.getGlobalKills(target));
            bestStreak = Math.max(0, api.getBestWinStreak(target));

            for (String ladder : api.getLadderNames()) {
                losses += Math.max(0, api.getLadderLosses(target, ladder, WeightClass.RANKED));
                losses += Math.max(0, api.getLadderLosses(target, ladder, WeightClass.UNRANKED));
                deaths += Math.max(0, api.getLadderDeaths(target, ladder));
            }
        }

        String wlrStr = ratio(wins, losses);
        String kdrStr = ratio(kills, deaths);

        String text = config.getOverviewFormat()
                .replace("{player_name}", target.getName())
                .replace("{division}", division)
                .replace("{matches}", String.valueOf(matches))
                .replace("{wins}", String.valueOf(wins))
                .replace("{losses}", String.valueOf(losses))
                .replace("{wlr}", wlrStr)
                .replace("{kills}", String.valueOf(kills))
                .replace("{deaths}", String.valueOf(deaths))
                .replace("{kdr}", kdrStr)
                .replace("{best_streak}", String.valueOf(bestStreak));

        Component description = ComponentUtils.parse(viewer, text);

        if (config.isShowEloBar() && ZonePracticeApi.isAvailable()) {
            double progress = ZonePracticeApi.getInstance().getPlayerDivisionProgress(target);
            if (progress >= 0) {
                int percent = (int) Math.round(progress);
                Component bar = progressBarRenderer.render(
                        Math.max(0.0, Math.min(1.0, progress / 100.0)),
                        config.getBarLength(),
                        queueConfig.getProgressBarFilledChar(),
                        queueConfig.getProgressBarEmptyChar(),
                        queueConfig.getProgressBarFilledColor(),
                        queueConfig.getProgressBarEmptyColor(),
                        "lime_concrete"
                ).hoverEvent(HoverEvent.showText(ComponentUtils.parse(
                        "<green><b>Overall Division Progress</b></green><newline>"
                                + "<gray>Current: <white>" + division + "</white></gray><newline>"
                                + "<gray>Progress: <aqua>" + percent + "%</aqua></gray>"
                )));
                description = description
                        .append(Component.newline())
                        .append(bar)
                        .append(ComponentUtils.parse("<gray> <aqua>" + percent + "%</aqua></gray>"));
            }
        }

        PlainMessageDialogBody body = DialogBody.plainMessage(description, 260);
        return DialogBody.item(item, body, true, true, 20, 20);
    }

    @NotNull
    private DialogBody buildLadderCard(
            Player target,
            String ladder,
            QueueMenuConfig queueConfig,
            StatsMenuConfig statsConfig,
            @NotNull DivisionProgressCalculator divisionCalculator
    ) {
        ItemStack item = ZonePracticeBridge.getLadderItem(ladder);
        if (item == null) item = new ItemStack(Material.IRON_SWORD);

        int elo = ZonePracticeBridge.getElo(target, ladder);
        int ladderWins = ZonePracticeBridge.getPlayerLadderWins(target, ladder, "LADDER_TOTAL");

        Component title = Component.empty()
                .append(LadderNames.display(target, ladder, "<aqua>").decorate(TextDecoration.BOLD))
                .append(ComponentUtils.parse("<gray> • Elo: </gray>"))
                .append(elo >= 0
                        ? ComponentUtils.parse("<gold>" + elo + "</gold>")
                        : ComponentUtils.parse("<dark_gray>Unranked</dark_gray>"))
                .append(ComponentUtils.parse("<gray> • Wins: </gray><yellow>" + ladderWins + "</yellow>"));

        Component card = title;

        if (statsConfig.isShowEloBar() && elo >= 0) {
            DivisionProgressCalculator.DivisionProgress divProgress = divisionCalculator.calculate(target, ladder);
            if (divProgress.isRanked()) {
                Component eloBar = progressBarRenderer.render(
                        divProgress.progressPercent(),
                        statsConfig.getBarLength(),
                        queueConfig.getProgressBarFilledChar(),
                        queueConfig.getProgressBarEmptyChar(),
                        queueConfig.getProgressBarFilledColor(),
                        queueConfig.getProgressBarEmptyColor(),
                        "lime_concrete"
                ).hoverEvent(HoverEvent.showText(ComponentUtils.parse(
                        "<green><b>Division Progress</b></green><newline>"
                                + "<gray>Rank: <white>" + divProgress.currentDivisionName() + "</white></gray><newline>"
                                + (divProgress.isMaxTier()
                                ? "<gold>Maximum Division Reached!</gold>"
                                : "<gray>Next: <gold>" + divProgress.nextDivisionName() + "</gold></gray><newline>"
                                + "<gray>Elo: <yellow>" + divProgress.currentElo() + "</yellow> / <gold>"
                                + divProgress.nextTierMinElo() + "</gold></gray>")
                )));
                card = card.append(Component.newline()).append(eloBar);
            }
        }

        if (statsConfig.isShowWinsBar()) {
            WinsProgressCalculator.WinsProgress winsProgress = winsCalculator.calculate(target, ladder, queueConfig);
            Component winsBar = progressBarRenderer.render(
                    winsProgress.progressPercent(),
                    statsConfig.getBarLength(),
                    queueConfig.getWinsBarFilledChar(),
                    queueConfig.getWinsBarEmptyChar(),
                    queueConfig.getWinsBarFilledColor(),
                    queueConfig.getWinsBarEmptyColor(),
                    "orange_concrete"
            ).hoverEvent(HoverEvent.showText(ComponentUtils.parse(
                    "<gold><b>Win Progression</b></gold><newline>"
                            + "<gray>Wins: <yellow>" + winsProgress.currentWins() + "</yellow></gray><newline>"
                            + (winsProgress.isMaxMilestone()
                            ? "<gold>Maximum Win Milestone Reached!</gold>"
                            : "<gray>Next: <gold>" + winsProgress.nextMilestone() + " wins</gold></gray>")
            )));
            card = card.append(Component.newline()).append(winsBar);
        }

        PlainMessageDialogBody description = DialogBody.plainMessage(card, 260);
        return DialogBody.item(item, description, true, true, 16, 16);
    }

    @NotNull
    private String ratio(int numerator, int denominator) {
        if (denominator <= 0) {
            return numerator > 0 ? "∞" : "0.00";
        }
        return String.format(Locale.US, "%.2f", (double) numerator / denominator);
    }
}
