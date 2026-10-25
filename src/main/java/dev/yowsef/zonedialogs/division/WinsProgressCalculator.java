package dev.yowsef.zonedialogs.division;

import dev.yowsef.zonedialogs.bridge.ZonePracticeBridge;
import dev.yowsef.zonedialogs.config.QueueMenuConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WinsProgressCalculator {

    public record WinsProgress(
            int currentWins,
            int previousMilestone,
            int nextMilestone,
            double progressPercent,
            boolean isMaxMilestone
    ) {}

    @NotNull
    public WinsProgress calculate(@NotNull Player player, @NotNull String ladder, @NotNull QueueMenuConfig config) {
        int wins = ZonePracticeBridge.getPlayerLadderWins(player, ladder, config.getWinsBarCountType());
        List<Integer> milestones = config.getWinsBarMilestones();

        if (milestones == null || milestones.isEmpty()) {
            milestones = List.of(5, 10, 25, 50, 100, 250, 500);
        }

        int prev = 0;
        Integer next = null;

        for (int m : milestones) {
            if (wins >= m) {
                prev = m;
            } else {
                next = m;
                break;
            }
        }

        if (next == null) {
            // Already reached or exceeded maximum milestone
            return new WinsProgress(wins, prev, prev, 1.0, true);
        }

        int range = next - prev;
        double progress = range > 0 ? (double) (wins - prev) / range : 0.0;
        double clamped = Math.max(0.0, Math.min(1.0, progress));

        return new WinsProgress(wins, prev, next, clamped, false);
    }
}
