package dev.yowsef.zonedialogs.menu.queue;

import dev.nandi0813.api.Interface.BotRegistry;
import dev.nandi0813.api.ZonePracticeApi;
import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.bridge.ZonePracticeBridge;
import dev.yowsef.zonedialogs.config.QueueMenuConfig;
import dev.yowsef.zonedialogs.division.DivisionProgressCalculator;
import dev.yowsef.zonedialogs.division.ProgressBarRenderer;
import dev.yowsef.zonedialogs.division.WinsProgressCalculator;
import dev.yowsef.zonedialogs.session.PlayerSession;
import dev.yowsef.zonedialogs.util.ComponentUtils;
import dev.yowsef.zonedialogs.util.ItemIcon;
import dev.yowsef.zonedialogs.util.LadderNames;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class QueueDialogBuilder {

    private final ZoneDialogsPlugin plugin;
    private final QueueMenuController controller;
    private final ProgressBarRenderer progressBarRenderer;

    public QueueDialogBuilder(@NotNull ZoneDialogsPlugin plugin, @NotNull QueueMenuController controller) {
        this.plugin = plugin;
        this.controller = controller;
        this.progressBarRenderer = new ProgressBarRenderer();
    }

    @NotNull
    public Dialog build(@NotNull Player player) {
        QueueMenuConfig config = plugin.getConfigManager().getQueueMenuConfig();
        PlayerSession session = plugin.getSessionManager().getSession(player);

        List<String> allLadders = ZonePracticeBridge.getLadderNames();
        Map<String, String> canonical = canonicalMap(allLadders);
        Set<String> queuedLower = lowerSet(ZonePracticeBridge.getQueuedLadderNames(player));
        boolean ranked = session.isRanked();

        DivisionProgressCalculator divisionCalculator =
                new DivisionProgressCalculator(plugin.getConfigManager().getDivisionThresholds());
        WinsProgressCalculator winsCalculator = new WinsProgressCalculator();

        List<DialogBody> bodies = new ArrayList<>();
        int width = config.getBodyWidth();

        if (config.getHeaderSubtitle() != null && !config.getHeaderSubtitle().isBlank()) {
            bodies.add(DialogBody.plainMessage(ComponentUtils.parse(player, config.getHeaderSubtitle()), width));
        }

        bodies.add(DialogBody.plainMessage(buildCategoryTabsBar(player, session, config), width));
        bodies.add(DialogBody.plainMessage(buildControlsBar(player, session, config, queuedLower), width));
        bodies.add(DialogBody.plainMessage(
                ComponentUtils.parse(player, config.getHeaderSpacer() != null ? config.getHeaderSpacer() : " "),
                width
        ));

        if (!queuedLower.isEmpty()) {
            bodies.add(DialogBody.plainMessage(buildQueuedLine(player, config, queuedLower, canonical), width));
        }

        List<String> laddersToDisplay = resolveLaddersToDisplay(session, allLadders, canonical);
        if (laddersToDisplay.isEmpty()) {
            String emptyMsg = "favorites".equalsIgnoreCase(session.getSelectedCategory())
                    ? "<gray>No favorite ladders saved yet. Click the ☆ next to a ladder to add one.</gray>"
                    : "<gray>No ladders available for this category.</gray>";
            bodies.add(DialogBody.plainMessage(ComponentUtils.parse(player, emptyMsg), width));
        } else {
            for (String ladderName : laddersToDisplay) {
                bodies.add(buildLadderItemBody(
                        player, session, config, ladderName, queuedLower, ranked, divisionCalculator, winsCalculator
                ));
            }
        }

        Component titleComp = ComponentUtils.parse(player, config.getDialogTitle());
        Component extTitleComp = ComponentUtils.parse(player, config.getDialogExternalTitle());

        DialogBase.DialogAfterAction afterAction;
        try {
            afterAction = DialogBase.DialogAfterAction.valueOf(config.getAfterAction().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            afterAction = DialogBase.DialogAfterAction.NONE;
        }

        // There is no dialog-close event in Paper. If a timer can re-show the
        // menu, Escape must stay disabled or the menu would immediately reopen.
        boolean canCloseWithEscape = config.isCanCloseEscape()
                && plugin.getConfigManager().getAutoRefreshIntervalTicks() <= 0;

        DialogBase base = DialogBase.builder(titleComp)
                .externalTitle(extTitleComp)
                .pause(config.isDialogPause())
                .canCloseWithEscape(canCloseWithEscape)
                .afterAction(afterAction)
                .body(bodies)
                .build();

        ActionButton closeButton = ActionButton.create(
                ComponentUtils.parse("<red>Close</red>"),
                ComponentUtils.parse("<gray>Close menu</gray>"),
                80,
                DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        Bukkit.getScheduler().runTask(plugin, () -> controller.handleClose(p));
                    }
                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build())
        );

        return Dialog.create(b -> b.empty().base(base)
                .type(io.papermc.paper.registry.data.dialog.type.DialogType.notice(closeButton)));
    }

    @NotNull
    public List<String> resolveLaddersToDisplay(@NotNull PlayerSession session) {
        List<String> allLadders = ZonePracticeBridge.getLadderNames();
        return resolveLaddersToDisplay(session, allLadders, canonicalMap(allLadders));
    }

    @NotNull
    private List<String> resolveLaddersToDisplay(
            @NotNull PlayerSession session,
            @NotNull List<String> allLadders,
            @NotNull Map<String, String> canonical
    ) {
        String activeCat = session.getSelectedCategory().toLowerCase(Locale.ROOT);

        if ("favorites".equals(activeCat)) {
            LinkedHashSet<String> result = new LinkedHashSet<>();
            for (String favorite : session.getFavorites()) {
                String actual = canonical.get(favorite.toLowerCase(Locale.ROOT));
                if (actual != null) result.add(actual);
            }
            return new ArrayList<>(result);
        }

        if ("all".equals(activeCat)) {
            return new ArrayList<>(allLadders);
        }

        QueueMenuConfig config = plugin.getConfigManager().getQueueMenuConfig();
        String fallback = plugin.getConfigManager().getFallbackCategory().toLowerCase(Locale.ROOT);

        QueueMenuConfig.CategoryDef def = config.getCategories().get(activeCat);
        boolean usingFallback = false;
        if (def == null) {
            def = config.getCategories().get(fallback);
            usingFallback = def != null;
        }

        if (def == null) {
            // A broken/missing category list should not make the whole menu empty.
            return new ArrayList<>(allLadders);
        }

        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String configured : def.ladders()) {
            String actual = canonical.get(configured.toLowerCase(Locale.ROOT));
            if (actual != null) result.add(actual);
        }

        if (plugin.getConfigManager().isAutoDetectLadders()
                && ("all".equals(activeCat) || activeCat.equals(fallback) || usingFallback)) {
            Set<String> assigned = new HashSet<>();
            for (QueueMenuConfig.CategoryDef category : config.getCategories().values()) {
                for (String configured : category.ladders()) {
                    assigned.add(configured.toLowerCase(Locale.ROOT));
                }
            }
            for (String actual : allLadders) {
                if (!assigned.contains(actual.toLowerCase(Locale.ROOT))) {
                    result.add(actual);
                }
            }
        }

        return new ArrayList<>(result);
    }

    @NotNull
    private Component buildCategoryTabsBar(Player player, PlayerSession session, QueueMenuConfig config) {
        Component bar = Component.empty();
        boolean first = true;

        for (Map.Entry<String, QueueMenuConfig.CategoryDef> entry : config.getCategories().entrySet()) {
            String catId = entry.getKey();
            QueueMenuConfig.CategoryDef def = entry.getValue();

            if (!first) {
                bar = bar.append(ComponentUtils.parse("<dark_gray> - </dark_gray>"));
            }
            first = false;

            boolean isSelected = catId.equalsIgnoreCase(session.getSelectedCategory());
            String display = def.display();
            if (display == null || display.isBlank()
                    || (!display.contains("<item:") && !display.contains("<block:") && !display.contains("<sprite:"))) {
                display = ItemIcon.tag(def.icon()) + " " + (display == null ? catId : display);
            }

            Component tabComp = ComponentUtils.parse(player, display);
            if (isSelected) {
                tabComp = tabComp.decorate(TextDecoration.UNDERLINED);
            }

            tabComp = tabComp
                    .hoverEvent(HoverEvent.showText(ComponentUtils.parse(
                            "<gray>Switch category to </gray>" + display
                                    + "<newline><dark_gray>Click to view kits</dark_gray>")))
                    .clickEvent(ClickEvent.callback(audience -> {
                        if (audience instanceof Player p) {
                            controller.handleCategorySwitch(p, catId);
                        }
                    }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build()));

            bar = bar.append(tabComp);
        }

        return bar;
    }

    @NotNull
    private Component buildControlsBar(
            Player player,
            PlayerSession session,
            QueueMenuConfig config,
            Set<String> queuedLower
    ) {
        Component bar = Component.empty();

        bar = bar.append(ComponentUtils.parse("<dark_gray>Mode: </dark_gray>"));
        bar = bar.append(modeButton(player, session, config, false));
        bar = bar.append(ComponentUtils.parse("<dark_gray> / </dark_gray>"));
        bar = bar.append(modeButton(player, session, config, true));
        bar = bar.append(Component.text("    "));

        String queueAllRaw = session.isQueueAll()
                ? config.getToggleQueueAllChecked()
                : config.getToggleQueueAllUnchecked();
        Component queueAllComp = ComponentUtils.parse(player, queueAllRaw)
                .hoverEvent(HoverEvent.showText(ComponentUtils.parse(
                        "<aqua><b>Queue All</b></aqua><newline>"
                                + "<gray>Status: " + (session.isQueueAll() ? "<green>Active</green>" : "<red>Inactive</red>") + "</gray><newline>"
                                + "<dark_gray>Queues every available kit in this category</dark_gray>"
                )))
                .clickEvent(callback(p -> controller.handleQueueAllToggle(p)));
        bar = bar.append(queueAllComp);
        bar = bar.append(Component.text("    "));

        String keepQueuingRaw = session.isKeepQueuing()
                ? config.getToggleKeepQueuingChecked()
                : config.getToggleKeepQueuingUnchecked();
        Component keepQueuingComp = ComponentUtils.parse(player, keepQueuingRaw)
                .hoverEvent(HoverEvent.showText(ComponentUtils.parse(
                        "<aqua><b>Keep Queuing</b></aqua><newline>"
                                + "<gray>Status: " + (session.isKeepQueuing() ? "<green>Active</green>" : "<red>Inactive</red>") + "</gray><newline>"
                                + "<dark_gray>Automatically re-enters the queue after a match</dark_gray>"
                )))
                .clickEvent(callback(p -> controller.handleKeepQueuingToggle(p)));
        bar = bar.append(keepQueuingComp);
        bar = bar.append(Component.newline());

        Component refresh = ComponentUtils.parse(player, config.getButtonRefresh())
                .hoverEvent(HoverEvent.showText(ComponentUtils.parse("<gray>Refresh queue counts</gray>")))
                .clickEvent(callback(p -> controller.handleRefresh(p)));
        bar = bar.append(refresh);

        if (!queuedLower.isEmpty()) {
            Component leaveAll = ComponentUtils.parse(player, config.getButtonLeaveAll())
                    .hoverEvent(HoverEvent.showText(ComponentUtils.parse("<gray>Leave every queue</gray>")))
                    .clickEvent(callback(p -> controller.handleLeaveQueue(p)));
            bar = bar.append(Component.text("    ")).append(leaveAll);
        }

        if (config.isShowBotPractice() && isBotPracticeAvailable()) {
            Component bot = ComponentUtils.parse(player, config.getButtonBotPractice())
                    .hoverEvent(HoverEvent.showText(ComponentUtils.parse("<gray>Open the bot practice setup</gray>")))
                    .clickEvent(callback(p -> controller.handleBotPractice(p)));
            bar = bar.append(Component.text("    ")).append(bot);
        }

        return bar;
    }

    @NotNull
    private Component modeButton(Player player, PlayerSession session, QueueMenuConfig config, boolean rankedButton) {
        String raw = rankedButton ? config.getButtonModeRanked() : config.getButtonModeUnranked();
        Component component = ComponentUtils.parse(player, raw);
        boolean selected = session.isRanked() == rankedButton;

        if (selected) {
            component = component.decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED);
        }

        component = component
                .hoverEvent(HoverEvent.showText(ComponentUtils.parse(
                        "<gray>Switch to " + (rankedButton ? "ranked" : "unranked") + " queue</gray>")))
                .clickEvent(callback(p -> {
                    if (session.isRanked() != rankedButton) {
                        controller.handleModeToggle(p);
                    }
                }));
        return component;
    }

    private boolean isBotPracticeAvailable() {
        if (!ZonePracticeApi.isAvailable()) return false;
        try {
            BotRegistry registry = ZonePracticeApi.getInstance().getBotRegistry();
            return registry != null && registry.isAvailable() && registry.getProvider() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @NotNull
    private Component buildQueuedLine(
            Player player,
            QueueMenuConfig config,
            Set<String> queuedLower,
            Map<String, String> canonical
    ) {
        Component line = ComponentUtils.parse("<gray>Queued: </gray>");
        int shown = 0;
        int total = queuedLower.size();

        for (String lower : queuedLower) {
            if (shown >= 8) break;
            if (shown > 0) line = line.append(ComponentUtils.parse("<dark_gray>, </dark_gray>"));
            String actual = canonical.getOrDefault(lower, lower);
            line = line.append(LadderNames.display(player, actual, config.getLadderColor(actual)));
            shown++;
        }

        if (total > shown) {
            line = line.append(ComponentUtils.parse("<dark_gray> +" + (total - shown) + " more</dark_gray>"));
        }
        return line;
    }

    @NotNull
    private DialogBody buildLadderItemBody(
            Player player,
            PlayerSession session,
            QueueMenuConfig config,
            String ladderName,
            Set<String> queuedLower,
            boolean ranked,
            @NotNull DivisionProgressCalculator divisionCalculator,
            @NotNull WinsProgressCalculator winsCalculator
    ) {
        boolean isQueued = queuedLower.contains(ladderName.toLowerCase(Locale.ROOT));
        ItemStack item = resolveLadderItem(ladderName, config);
        int playerCount = controller.getLadderPlayerCount(ladderName, ranked);

        Component favorite = ComponentUtils.parse(
                        session.isFavorite(ladderName) ? "<gold>★</gold>" : "<dark_gray>☆</dark_gray>")
                .hoverEvent(HoverEvent.showText(ComponentUtils.parse(
                        session.isFavorite(ladderName) ? "<gray>Remove from favorites</gray>" : "<gray>Add to favorites</gray>")))
                .clickEvent(callback(p -> controller.handleFavoriteToggle(p, ladderName)));

        String mode = ranked ? "ranked" : "unranked";
        Component status = isQueued
                ? ComponentUtils.parse("<green>✔ Queued</green>")
                : ComponentUtils.parse("<gray>" + playerCount + " " + mode + "</gray>");

        Component rowLine = Component.empty()
                .append(favorite)
                .append(Component.space())
                .append(LadderNames.display(player, ladderName, config.getLadderColor(ladderName)))
                .append(ComponentUtils.parse("<dark_gray> - </dark_gray>"))
                .append(status);

        Component ladderHover = buildLadderHover(player, ladderName, isQueued, playerCount, mode);
        rowLine = rowLine.hoverEvent(HoverEvent.showText(ladderHover))
                .clickEvent(callback(p -> controller.handleLadderQueue(p, ladderName)));

        boolean showPrimaryBar = (isQueued || "all".equalsIgnoreCase(config.getProgressBarVisibility()))
                && !"none".equalsIgnoreCase(config.getProgressBarVisibility());

        boolean showWinsBar = false;
        if (config.isWinsBarEnabled() && !"none".equalsIgnoreCase(config.getWinsBarVisibility())) {
            if ("inherit".equalsIgnoreCase(config.getWinsBarVisibility())) {
                showWinsBar = showPrimaryBar;
            } else if ("all".equalsIgnoreCase(config.getWinsBarVisibility())) {
                showWinsBar = true;
            } else if ("queued-only".equalsIgnoreCase(config.getWinsBarVisibility())) {
                showWinsBar = isQueued;
            }
        }

        Component descComp = rowLine;
        boolean hasBar = false;

        if (showPrimaryBar) {
            DivisionProgressCalculator.DivisionProgress divProgress = divisionCalculator.calculate(player, ladderName);
            if (divProgress.isRanked()) {
                descComp = descComp.append(Component.newline()).append(Component.newline());
                Component progressBarComp = progressBarRenderer.render(divProgress.progressPercent(), config)
                        .hoverEvent(HoverEvent.showText(buildProgressBarHover(divProgress)));
                descComp = descComp.append(progressBarComp);
                hasBar = true;

                if (config.isShowDivisionLabel()) {
                    if (divProgress.isMaxTier()) {
                        descComp = descComp.append(Component.space())
                                .append(ComponentUtils.parse("<gold><b>MAX</b></gold>"));
                    } else {
                        String rawRank = config.getDivisionLabelFormat()
                                .replace("{division_color}", "<green>")
                                .replace("{division_name}", divProgress.currentDivisionName())
                                .replace("{current_elo}", String.valueOf(divProgress.currentElo()))
                                .replace("{next_elo}", String.valueOf(divProgress.nextTierMinElo()));
                        descComp = descComp.append(Component.space()).append(ComponentUtils.parse(player, rawRank));
                    }
                }
            }
        }

        if (showWinsBar) {
            if (!hasBar) {
                descComp = descComp.append(Component.newline()).append(Component.newline());
            } else {
                descComp = descComp.append(Component.newline());
            }
            WinsProgressCalculator.WinsProgress winsProgress = winsCalculator.calculate(player, ladderName, config);
            Component winsBarComp = progressBarRenderer.render(
                            winsProgress.progressPercent(),
                            config.getWinsBarLength(),
                            config.getWinsBarFilledChar(),
                            config.getWinsBarEmptyChar(),
                            config.getWinsBarFilledColor(),
                            config.getWinsBarEmptyColor(),
                            "orange_concrete")
                    .hoverEvent(HoverEvent.showText(buildWinsBarHover(player, ladderName, config, winsProgress)));
            descComp = descComp.append(winsBarComp);

            if (config.isWinsBarShowLabel()) {
                String rawWins = config.getWinsBarLabelFormat()
                        .replace("{current_wins}", String.valueOf(winsProgress.currentWins()))
                        .replace("{target_wins}", String.valueOf(winsProgress.nextMilestone()))
                        .replace("{progress_percent}", String.valueOf((int) (winsProgress.progressPercent() * 100)));
                descComp = descComp.append(Component.space()).append(ComponentUtils.parse(player, rawWins));
            }
        }

        int descriptionWidth = Math.max(120, config.getBodyWidth() - 50);
        PlainMessageDialogBody description = DialogBody.plainMessage(descComp, descriptionWidth);
        return DialogBody.item(item, description, true, true, 16, 16);
    }

    @NotNull
    private Component buildLadderHover(
            Player player,
            String ladderName,
            boolean isQueued,
            int playerCount,
            String mode
    ) {
        QueueMenuConfig config = plugin.getConfigManager().getQueueMenuConfig();
        Component hover = Component.empty()
                .append(LadderNames.display(player, ladderName, config.getLadderColor(ladderName)).decorate(TextDecoration.BOLD))
                .append(Component.newline())
                .append(ComponentUtils.parse("<gray>Status: "))
                .append(isQueued
                        ? ComponentUtils.parse("<green>Queued ✔</green>")
                        : ComponentUtils.parse("<dark_gray>Not queued</dark_gray>"))
                .append(Component.newline())
                .append(ComponentUtils.parse("<gray>In queue: <yellow>" + playerCount + "</yellow> " + mode + " players</gray>"))
                .append(Component.newline());

        if (ZonePracticeApi.isAvailable()) {
            int elo = ZonePracticeBridge.getElo(player, ladderName);
            if (elo >= 0) {
                hover = hover.append(ComponentUtils.parse("<gray>Your Elo: <gold>" + elo + "</gold></gray>"))
                        .append(Component.newline());
            }
            int wins = ZonePracticeBridge.getPlayerLadderWins(player, ladderName, "LADDER_TOTAL");
            hover = hover.append(ComponentUtils.parse("<gray>Wins: <gold>" + wins + "</gold></gray>"))
                    .append(Component.newline());

            int bestStreak = ZonePracticeBridge.getBestKillStreak(player, ladderName);
            if (bestStreak > 0) {
                hover = hover.append(ComponentUtils.parse("<gray>Best Streak: <aqua>" + bestStreak + "</aqua></gray>"))
                        .append(Component.newline());
            }
        }

        hover = hover.append(Component.newline())
                .append(isQueued
                        ? ComponentUtils.parse("<red>Click to leave this queue</red>")
                        : ComponentUtils.parse("<green>Click to join this queue</green>"));
        return hover;
    }

    @NotNull
    private Component buildProgressBarHover(DivisionProgressCalculator.DivisionProgress divProgress) {
        if (!divProgress.isRanked()) {
            return ComponentUtils.parse("<gray>This ladder has no ranked Elo.</gray>");
        }

        int percent = (int) Math.round(divProgress.progressPercent() * 100.0);
        StringBuilder sb = new StringBuilder();
        sb.append("<green><b>Division Progress</b></green><newline>");
        sb.append("<gray>Current Rank: <white><b>").append(divProgress.currentDivisionName()).append("</b></white></gray><newline>");
        if (divProgress.nextDivisionName() != null) {
            sb.append("<gray>Next Rank: <gold><b>").append(divProgress.nextDivisionName()).append("</b></gold></gray><newline>");
            sb.append("<gray>Elo: <yellow>").append(divProgress.currentElo()).append("</yellow> / <gold>")
                    .append(divProgress.nextTierMinElo()).append("</gold></gray><newline>");
            sb.append("<gray>Progress: <aqua>").append(percent).append("%</aqua></gray>");
        } else {
            sb.append("<gold><b>Maximum Division Reached!</b></gold>");
        }

        return ComponentUtils.parse(sb.toString());
    }

    @NotNull
    private Component buildWinsBarHover(
            Player player,
            String ladderName,
            QueueMenuConfig config,
            WinsProgressCalculator.WinsProgress winsProgress
    ) {
        int percent = (int) Math.round(winsProgress.progressPercent() * 100.0);
        Component hover = Component.empty()
                .append(ComponentUtils.parse("<gold><b>Win Progression</b></gold>"))
                .append(Component.newline())
                .append(ComponentUtils.parse("<gray>Ladder: </gray>"))
                .append(LadderNames.display(player, ladderName, config.getLadderColor(ladderName)))
                .append(Component.newline())
                .append(ComponentUtils.parse("<gray>Current Wins: <yellow><b>" + winsProgress.currentWins()
                        + "</b></yellow></gray>"))
                .append(Component.newline());

        if (!winsProgress.isMaxMilestone()) {
            int needed = Math.max(0, winsProgress.nextMilestone() - winsProgress.currentWins());
            hover = hover.append(ComponentUtils.parse("<gray>Next Milestone: <gold><b>"
                            + winsProgress.nextMilestone() + " wins</b></gold></gray>"))
                    .append(Component.newline())
                    .append(ComponentUtils.parse("<gray>Progress: <aqua>" + percent + "%</aqua></gray>"))
                    .append(Component.newline())
                    .append(ComponentUtils.parse("<gray>Remaining: <green>" + needed + " wins needed</green></gray>"));
        } else {
            hover = hover.append(ComponentUtils.parse("<gold><b>Maximum Win Milestone Reached!</b></gold>"));
        }
        return hover;
    }

    @NotNull
    private ItemStack resolveLadderItem(String ladderName, QueueMenuConfig config) {
        Material custom = config.getCustomIcon(ladderName);
        if (custom != null) {
            return new ItemStack(custom);
        }

        ItemStack item = ZonePracticeBridge.getLadderItem(ladderName);
        return item != null ? item : new ItemStack(Material.IRON_SWORD);
    }

    private ClickEvent callback(java.util.function.Consumer<Player> action) {
        return ClickEvent.callback(audience -> {
            if (audience instanceof Player player) {
                // Run after the client finishes the current dialog interaction.
                Bukkit.getScheduler().runTask(plugin, () -> action.accept(player));
            }
        }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build());
    }

    @NotNull
    private Map<String, String> canonicalMap(@NotNull List<String> allLadders) {
        Map<String, String> result = new java.util.LinkedHashMap<>();
        for (String ladder : allLadders) {
            result.put(ladder.toLowerCase(Locale.ROOT), ladder);
        }
        return result;
    }

    @NotNull
    private Set<String> lowerSet(@NotNull List<String> values) {
        Set<String> result = new LinkedHashSet<>();
        for (String value : values) {
            result.add(value.toLowerCase(Locale.ROOT));
        }
        return result;
    }
}
