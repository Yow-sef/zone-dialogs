package dev.yowsef.zonedialogs.division;

import dev.nandi0813.api.ZonePracticeApi;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DivisionProgressCalculator {

    private final Map<String, Integer> divisionThresholds = new LinkedHashMap<>();

    public DivisionProgressCalculator(@NotNull Map<String, Integer> configuredThresholds) {
        if (configuredThresholds.isEmpty()) {
            divisionThresholds.put("Bronze", 0);
            divisionThresholds.put("Silver", 1000);
            divisionThresholds.put("Gold", 1200);
            divisionThresholds.put("Platinum", 1400);
            divisionThresholds.put("Diamond", 1600);
            divisionThresholds.put("Master", 1800);
            divisionThresholds.put("Grandmaster", 2000);
        } else {
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(configuredThresholds.entrySet());
            entries.sort(Comparator.comparingInt(Map.Entry::getValue));
            for (Map.Entry<String, Integer> entry : entries) {
                divisionThresholds.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public record DivisionProgress(
            String currentDivisionName,
            String nextDivisionName,
            int currentElo,
            int currentTierMinElo,
            int nextTierMinElo,
            double progressPercent,
            boolean isMaxTier
    ) {
        public boolean isRanked() {
            return currentElo >= 0;
        }
    }

    @NotNull
    public DivisionProgress calculate(@NotNull Player player, @NotNull String ladder) {
        int elo = -1;
        if (ZonePracticeApi.isAvailable()) {
            elo = ZonePracticeApi.getInstance().getElo(player, ladder);
        }

        // Unranked-only ladders (and players without a loaded profile) have no elo.
        // Showing a fake 1000 elo here was misleading.
        if (elo < 0) {
            return new DivisionProgress("Unranked", null, -1, 0, 0, 0.0, false);
        }

        String currentTier = null;
        String nextTier = null;
        int currentTierMin = 0;
        int nextTierMin = 0;

        for (Map.Entry<String, Integer> entry : divisionThresholds.entrySet()) {
            if (elo >= entry.getValue()) {
                currentTier = entry.getKey();
                currentTierMin = entry.getValue();
            } else {
                nextTier = entry.getKey();
                nextTierMin = entry.getValue();
                break;
            }
        }

        if (currentTier == null) {
            Map.Entry<String, Integer> first = divisionThresholds.entrySet().iterator().next();
            currentTier = first.getKey();
            currentTierMin = first.getValue();
            if (nextTier == null && divisionThresholds.size() > 1) {
                var it = divisionThresholds.entrySet().iterator();
                it.next();
                Map.Entry<String, Integer> second = it.next();
                nextTier = second.getKey();
                nextTierMin = second.getValue();
            }
        }

        if (nextTier == null) {
            return new DivisionProgress(
                    prettyDivision(currentTier),
                    null,
                    elo,
                    currentTierMin,
                    currentTierMin,
                    1.0,
                    true
            );
        }

        int range = nextTierMin - currentTierMin;
        double progress = range > 0 ? (double) (elo - currentTierMin) / range : 0.0;
        progress = Math.max(0.0, Math.min(1.0, progress));

        return new DivisionProgress(
                prettyDivision(currentTier),
                prettyDivision(nextTier),
                elo,
                currentTierMin,
                nextTierMin,
                progress,
                false
        );
    }

    @NotNull
    private String prettyDivision(@NotNull String division) {
        if (division.isBlank()) return division;
        if (division.chars().anyMatch(Character::isLowerCase)) return division;
        String lower = division.toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
