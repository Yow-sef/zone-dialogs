package dev.yowsef.zonedialogs.menu.leaderboard;

import dev.nandi0813.api.Interface.LeaderboardEntry;
import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import dev.yowsef.zonedialogs.bridge.ZonePracticeBridge;
import dev.yowsef.zonedialogs.config.LeaderboardMenuConfig;
import dev.yowsef.zonedialogs.util.ComponentUtils;
import dev.yowsef.zonedialogs.util.ItemIcon;
import dev.yowsef.zonedialogs.util.LadderNames;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class LeaderboardDialogBuilder {

    private final ZoneDialogsPlugin plugin;
    private final LeaderboardMenuController controller;

    public LeaderboardDialogBuilder(@NotNull ZoneDialogsPlugin plugin, @NotNull LeaderboardMenuController controller) {
        this.plugin = plugin;
        this.controller = controller;
    }

    @NotNull
    public Dialog build(@NotNull Player player) {
        LeaderboardMenuConfig config = plugin.getConfigManager().getLeaderboardMenuConfig();
        LeaderboardSession session = controller.getSession(player);

        List<DialogBody> bodies = new ArrayList<>();
        int width = config.getBodyWidth();

        if (config.getHeaderSubtitle() != null && !config.getHeaderSubtitle().isBlank()) {
            bodies.add(DialogBody.plainMessage(ComponentUtils.parse(player, config.getHeaderSubtitle()), width));
        }

        bodies.add(DialogBody.plainMessage(buildKitCarouselRow(player, session, config), width));
        bodies.add(DialogBody.plainMessage(buildMetricTabsRow(player, session, config), width));

        String ladder = session.getSelectedLadder();
        String metric = session.getSelectedMetric();
        LeaderboardMenuConfig.MetricDef metricDef = config.getMetrics().get(metric);
        String metricLabel = metricDef != null ? metricDef.name() : metric;

        Component sectionTitle = ComponentUtils.parse("<newline><aqua><b>Top " + config.getMaxEntries() + " </b></aqua>");
        if ("global".equalsIgnoreCase(ladder)) {
            sectionTitle = sectionTitle.append(ComponentUtils.parse("<white><b>Overall</b></white>"));
        } else {
            sectionTitle = sectionTitle.append(LadderNames.display(player, ladder, "<aqua>"));
        }
        sectionTitle = sectionTitle.append(ComponentUtils.parse("<gray> by </gray><gold>" + metricLabel + "</gold>"));
        bodies.add(DialogBody.plainMessage(sectionTitle, width));

        String search = session.getSearchQuery();
        // The API clamps the limit to 1-100, so a search fetches the whole cap
        // and filters locally instead of asking for zero entries.
        int fetchLimit = search.isBlank() ? config.getMaxEntries() : 100;
        List<LeaderboardEntry> records = ZonePracticeBridge.getLeaderboardEntries(
                "global".equalsIgnoreCase(ladder) ? null : ladder,
                metric,
                fetchLimit
        );

        if (!search.isBlank()) {
            String needle = search.toLowerCase(Locale.ROOT);
            records = records.stream()
                    .filter(record -> record.playerName().toLowerCase(Locale.ROOT).contains(needle))
                    .limit(config.getMaxEntries())
                    .toList();
        }

        if (records.isEmpty()) {
            bodies.add(DialogBody.plainMessage(
                    ComponentUtils.parse("<gray><i>No leaderboard data found for this selection.</i></gray>"),
                    width
            ));
        } else {
            for (LeaderboardEntry record : records) {
                bodies.add(buildRecordBody(player, record, metricDef, config));
            }
        }

        if (config.isShowPersonalRankFooter()) {
            bodies.add(buildPersonalRecordBody(player, session, config, metricDef));
        }

        Component titleComp = ComponentUtils.parse(player, config.getDialogTitle());
        Component extTitleComp = ComponentUtils.parse(player, config.getDialogExternalTitle());

        DialogBase.DialogAfterAction afterAction;
        try {
            afterAction = DialogBase.DialogAfterAction.valueOf(config.getAfterAction().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            afterAction = DialogBase.DialogAfterAction.NONE;
        }

        DialogInput searchInput = DialogInput.text("search", ComponentUtils.parse("<aqua>Search</aqua>"))
                .initial(session.getSearchQuery())
                .width(220)
                .build();

        DialogBase base = DialogBase.builder(titleComp)
                .externalTitle(extTitleComp)
                .pause(config.isDialogPause())
                .canCloseWithEscape(config.isCanCloseEscape())
                .afterAction(afterAction)
                .body(bodies)
                .inputs(List.of(searchInput))
                .build();

        ActionButton searchButton = ActionButton.create(
                ComponentUtils.parse("<green>Search</green>"),
                ComponentUtils.parse("<gray>Filter player list</gray>"),
                110,
                DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        String query = view.getText("search");
                        Bukkit.getScheduler().runTask(plugin, () -> controller.handleSearch(p, query != null ? query : ""));
                    }
                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build())
        );

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
                ComponentUtils.parse("<gray>Close leaderboard</gray>"),
                90,
                DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        Bukkit.getScheduler().runTask(plugin, () -> controller.close(p));
                    }
                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build())
        );

        return Dialog.create(b -> b.empty().base(base).type(DialogType.multiAction(
                List.of(searchButton, mainMenuButton),
                closeButton,
                2
        )));
    }

    @NotNull
    private Component buildKitCarouselRow(
            Player player,
            LeaderboardSession session,
            LeaderboardMenuConfig config
    ) {
        List<String> items = new ArrayList<>();
        items.add("global");
        items.addAll(ZonePracticeBridge.getValidRegisteredLadderNames());

        int perPage = Math.max(1, config.getKitsPerPage());
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / perPage));
        int page = Math.min(session.getLadderPage(), totalPages - 1);
        session.setLadderPage(page);

        Component row = Component.empty();

        boolean canPrev = page > 0;
        Component prevBtn = Component.text("<", canPrev ? NamedTextColor.AQUA : NamedTextColor.DARK_GRAY)
                .hoverEvent(HoverEvent.showText(ComponentUtils.parse("<yellow>Previous kit page</yellow>")))
                .clickEvent(callback(p -> {
                    if (canPrev) controller.handlePageChange(p, -1);
                }));
        row = row.append(prevBtn).append(Component.space()).append(Component.space());

        int start = page * perPage;
        int end = Math.min(items.size(), start + perPage);

        for (int i = start; i < end; i++) {
            String item = items.get(i);
            boolean isGlobal = item.equalsIgnoreCase("global");
            boolean active = item.equalsIgnoreCase(session.getSelectedLadder());

            Component label;
            if (isGlobal) {
                label = Component.text("Overall", active ? NamedTextColor.GOLD : NamedTextColor.AQUA);
            } else {
                label = LadderNames.display(player, item, "<aqua>");
            }
            if (active) label = label.decorate(TextDecoration.BOLD);

            label = label
                    .hoverEvent(HoverEvent.showText(ComponentUtils.parse("<yellow>View this leaderboard</yellow>")))
                    .clickEvent(callback(p -> controller.handleLadderSelect(p, item)));

            row = row.append(label).append(Component.space()).append(Component.space());
        }

        boolean canNext = page < totalPages - 1;
        Component nextBtn = Component.text(">", canNext ? NamedTextColor.AQUA : NamedTextColor.DARK_GRAY)
                .hoverEvent(HoverEvent.showText(ComponentUtils.parse("<yellow>Next kit page</yellow>")))
                .clickEvent(callback(p -> {
                    if (canNext) controller.handlePageChange(p, 1);
                }));

        row = row.append(nextBtn);
        return row;
    }

    @NotNull
    private Component buildMetricTabsRow(
            Player player,
            LeaderboardSession session,
            LeaderboardMenuConfig config
    ) {
        Component row = ComponentUtils.parse("<dark_gray>Metric: </dark_gray>");
        boolean first = true;

        for (Map.Entry<String, LeaderboardMenuConfig.MetricDef> entry : config.getMetrics().entrySet()) {
            if (!first) row = row.append(ComponentUtils.parse("<dark_gray> / </dark_gray>"));
            first = false;

            LeaderboardMenuConfig.MetricDef def = entry.getValue();
            String display = def.display();
            if (display == null || display.isBlank()
                    || (!display.contains("<item:") && !display.contains("<block:") && !display.contains("<sprite:"))) {
                display = ItemIcon.tag(def.icon()) + " " + (display == null ? entry.getKey() : display);
            }

            boolean active = entry.getKey().equalsIgnoreCase(session.getSelectedMetric());
            Component tab = ComponentUtils.parse(player, display);
            if (active) tab = tab.decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED);

            tab = tab
                    .hoverEvent(HoverEvent.showText(ComponentUtils.parse("<gray>Switch leaderboard metric</gray>")))
                    .clickEvent(callback(p -> controller.handleMetricSelect(p, entry.getKey())));

            row = row.append(tab);
        }

        return row;
    }

    @NotNull
    private DialogBody buildRecordBody(
            Player player,
            LeaderboardEntry record,
            LeaderboardMenuConfig.MetricDef metricDef,
            LeaderboardMenuConfig config
    ) {
        String rankColor = switch (record.rank()) {
            case 1 -> "<gold><b>#" + record.rank() + "</b></gold>";
            case 2 -> "<white><b>#" + record.rank() + "</b></white>";
            case 3 -> "<color:#cd7f32><b>#" + record.rank() + "</b></color>";
            default -> "<gray>#" + record.rank() + "</gray>";
        };

        String metricName = metricDef != null ? metricDef.name() : "Score";
        Component line = Component.empty();
        if (config.isShowPlayerHeads()) {
            String head = headTag(record.playerName());
            if (!head.isEmpty()) {
                line = line.append(ComponentUtils.parse(player, head)).append(Component.space());
            }
        }
        line = line.append(ComponentUtils.parse(player, rankColor + " "))
                .append(Component.text(record.playerName(), NamedTextColor.WHITE).decorate(TextDecoration.BOLD))
                .append(ComponentUtils.parse("<dark_gray> - </dark_gray><white>" + record.score()
                        + "</white> <gray>" + metricName + "</gray>"));

        return DialogBody.plainMessage(line, config.getBodyWidth());
    }

    @NotNull
    private DialogBody buildPersonalRecordBody(
            Player player,
            LeaderboardSession session,
            LeaderboardMenuConfig config,
            LeaderboardMenuConfig.MetricDef metricDef
    ) {
        String ladder = session.getSelectedLadder();
        String ladderParam = "global".equalsIgnoreCase(ladder) ? null : ladder;
        int rank = ZonePracticeBridge.getPlayerLeaderboardRank(player, ladderParam, session.getSelectedMetric());
        int score = ZonePracticeBridge.getPersonalScore(player, ladderParam, session.getSelectedMetric());

        String rankText = rank > 0 ? String.valueOf(rank) : "—";
        String scoreText = score == Integer.MIN_VALUE || score < 0 ? "—" : String.valueOf(score);

        String format = config.getFooterFormat();
        if (format == null || format.isBlank()) {
            format = "<dark_gray>------------------------</dark_gray><newline><gray>Your Rank: </gray><gold><b>#{rank}</b></gold> <dark_gray>(</dark_gray><yellow>{score}</yellow><dark_gray>)</dark_gray>";
        }
        String text = format
                .replace("{rank}", rankText)
                .replace("{score}", scoreText)
                .replace("{metric}", metricDef != null ? metricDef.name() : session.getSelectedMetric());

        Component line = Component.empty();
        if (config.isShowPlayerHeads()) {
            String head = headTag(player.getName());
            if (!head.isEmpty()) {
                line = line.append(ComponentUtils.parse(player, head)).append(Component.space());
            }
        }
        line = line.append(ComponentUtils.parse(player, text));
        return DialogBody.plainMessage(line, config.getBodyWidth());
    }

    /**
     * Flat player-head sprite from 1.21.9 object components. Only valid player
     * names are turned into tags so a strange board name cannot break the parse.
     */
    @NotNull
    private String headTag(String playerName) {
        if (playerName == null || !playerName.matches("[A-Za-z0-9_]{1,16}")) {
            return "";
        }
        return "<head:" + playerName + ">";
    }

    private ClickEvent callback(java.util.function.Consumer<Player> action) {
        return ClickEvent.callback(audience -> {
            if (audience instanceof Player player) {
                Bukkit.getScheduler().runTask(plugin, () -> action.accept(player));
            }
        }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build());
    }
}
