package dev.yowsef.zonedialogs.bridge;

import dev.nandi0813.api.Enum.LeaderboardType;
import dev.nandi0813.api.Enum.PlayerState;
import dev.nandi0813.api.Enum.WeightClass;
import dev.nandi0813.api.Interface.LeaderboardEntry;
import dev.nandi0813.api.ZonePracticeApi;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ZonePracticeBridge {

    private ZonePracticeBridge() {}

    @NotNull
    public static List<String> getLadderNames() {
        if (!ZonePracticeApi.isAvailable()) return Collections.emptyList();
        List<String> names = ZonePracticeApi.getInstance().getLadderNames();
        return names != null ? names : Collections.emptyList();
    }

    public static boolean isLadderRegistered(@NotNull String ladderName) {
        return canonicalLadderName(ladderName) != null;
    }

    /**
     * Resolves a configured ladder id against the case-sensitive names returned
     * by the core. This prevents config entries such as "Sword" from silently
     * failing inside ZonePractice.
     */
    @Nullable
    public static String canonicalLadderName(@NotNull String ladderName) {
        for (String name : getLadderNames()) {
            if (name.equalsIgnoreCase(ladderName)) return name;
        }
        return null;
    }

    /**
     * Builds a lowercase-id to canonical-name map for one menu build.
     */
    @NotNull
    public static Map<String, String> canonicalLadderMap() {
        Map<String, String> result = new LinkedHashMap<>();
        for (String name : getLadderNames()) {
            result.put(name.toLowerCase(Locale.ROOT), name);
        }
        return result;
    }

    @NotNull
    public static List<String> getValidRegisteredLadderNames() {
        return getLadderNames();
    }

    @Nullable
    public static String getLadderDisplayName(@NotNull String ladderName) {
        if (!ZonePracticeApi.isAvailable()) return null;
        try {
            return ZonePracticeApi.getInstance().getLadderDisplayName(ladderName);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    public static ItemStack getLadderItem(@NotNull String ladderName) {
        if (!ZonePracticeApi.isAvailable()) return null;
        try {
            ItemStack icon = ZonePracticeApi.getInstance().getLadderIcon(ladderName);
            return icon != null ? icon.clone() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static int getLadderQueuedCount(@NotNull String ladderName, boolean ranked) {
        if (!ZonePracticeApi.isAvailable()) return 0;
        try {
            int count = ZonePracticeApi.getInstance().getQueuePlayerCount(ladderName, ranked);
            return Math.max(0, count);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static boolean isLadderQueued(@NotNull Player player, @NotNull String ladderName) {
        if (!ZonePracticeApi.isAvailable()) return false;
        try {
            return ZonePracticeApi.getInstance().isQueued(player, ladderName);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @NotNull
    public static List<String> getQueuedLadderNames(@NotNull Player player) {
        if (!ZonePracticeApi.isAvailable()) return Collections.emptyList();
        try {
            ZonePracticeApi api = ZonePracticeApi.getInstance();
            List<String> names = api.getQueuedLadderNames(player);
            if (names != null && !names.isEmpty()) return names;

            // Fallback for a single-queue core response.
            dev.nandi0813.api.Interface.Queue queue = api.getQueue(player);
            if (queue != null && queue.getLadderName() != null) {
                return Collections.singletonList(queue.getLadderName());
            }
            return Collections.emptyList();
        } catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }

    public static boolean isMultiQueueAllowed(@NotNull Player player) {
        if (!ZonePracticeApi.isAvailable()) return false;
        try {
            return ZonePracticeApi.getInstance().isMultiQueueAllowed(player);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @NotNull
    public static PlayerState getPlayerState(@NotNull Player player) {
        if (!ZonePracticeApi.isAvailable()) return PlayerState.UNKNOWN;
        try {
            PlayerState state = ZonePracticeApi.getInstance().getPlayerState(player);
            return state != null ? state : PlayerState.UNKNOWN;
        } catch (Throwable ignored) {
            return PlayerState.UNKNOWN;
        }
    }

    public static boolean queueLadder(@NotNull Player player, @NotNull String ladderName, boolean ranked) {
        if (!ZonePracticeApi.isAvailable()) return false;
        try {
            return ZonePracticeApi.getInstance().queuePlayer(player, ladderName, ranked, true);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void leaveLadderQueue(@NotNull Player player, @NotNull String ladderName) {
        if (!ZonePracticeApi.isAvailable()) return;
        try {
            ZonePracticeApi.getInstance().leaveQueue(player, ladderName);
        } catch (Throwable ignored) {
        }
    }

    public static void leaveQueue(@NotNull Player player) {
        if (!ZonePracticeApi.isAvailable()) return;
        try {
            ZonePracticeApi.getInstance().leaveAllQueues(player);
        } catch (Throwable ignored) {
        }
    }

    public static int getElo(@NotNull Player player, @NotNull String ladderName) {
        if (!ZonePracticeApi.isAvailable()) return -1;
        try {
            return ZonePracticeApi.getInstance().getElo(player, ladderName);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    public static int getBestKillStreak(@NotNull Player player, @NotNull String ladderName) {
        if (!ZonePracticeApi.isAvailable()) return -1;
        try {
            return ZonePracticeApi.getInstance().getLadderBestKillStreak(player, ladderName);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    public static int getPlayerLadderWins(@NotNull Player player, @NotNull String ladderName, @NotNull String countType) {
        if (!ZonePracticeApi.isAvailable()) return 0;
        ZonePracticeApi api = ZonePracticeApi.getInstance();
        try {
            switch (countType.toUpperCase(Locale.ROOT)) {
                case "GLOBAL":
                    return Math.max(0, api.getGlobalWins(player));
                case "LADDER_RANKED":
                    return Math.max(0, api.getLadderWins(player, ladderName, WeightClass.RANKED));
                case "LADDER_UNRANKED":
                    return Math.max(0, api.getLadderWins(player, ladderName, WeightClass.UNRANKED));
                case "LADDER_TOTAL":
                default:
                    int ranked = api.getLadderWins(player, ladderName, WeightClass.RANKED);
                    int unranked = api.getLadderWins(player, ladderName, WeightClass.UNRANKED);
                    return Math.max(0, ranked) + Math.max(0, unranked);
            }
        } catch (Throwable ignored) {
            return 0;
        }
    }

    @NotNull
    public static List<LeaderboardEntry> getLeaderboardEntries(
            @Nullable String ladderName,
            @NotNull String metricName,
            int limit
    ) {
        if (!ZonePracticeApi.isAvailable()) return Collections.emptyList();
        try {
            List<LeaderboardEntry> entries = ZonePracticeApi.getInstance()
                    .getLeaderboardEntries(toLeaderboardType(metricName), normalizeLadder(ladderName), limit);
            return entries != null ? entries : Collections.emptyList();
        } catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }

    @Nullable
    public static LeaderboardEntry getPlayerLeaderboardEntry(
            @NotNull Player player,
            @Nullable String ladderName,
            @NotNull String metricName
    ) {
        if (!ZonePracticeApi.isAvailable()) return null;
        try {
            return ZonePracticeApi.getInstance()
                    .getLeaderboardEntry(player, toLeaderboardType(metricName), normalizeLadder(ladderName));
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Maps a menu metric id such as {@code best_win_streak} to the core enum.
     */
    @NotNull
    public static LeaderboardType toLeaderboardType(@NotNull String metricName) {
        String clean = metricName.toUpperCase(Locale.ROOT).replace("_", "").replace("-", "");
        if (clean.contains("BEST")) {
            if (clean.contains("LOSE")) return LeaderboardType.BEST_LOSE_STREAK;
            if (clean.contains("WIN") || clean.contains("STREAK")) return LeaderboardType.BEST_WIN_STREAK;
        }
        if (clean.contains("LOSE")) return LeaderboardType.LOSE_STREAK;
        if (clean.contains("STREAK")) return LeaderboardType.WIN_STREAK;
        if (clean.contains("KILL")) return LeaderboardType.KILLS;
        if (clean.contains("DEATH")) return LeaderboardType.DEATHS;
        if (clean.contains("WIN")) return LeaderboardType.WINS;
        return LeaderboardType.ELO;
    }

    @Nullable
    private static String normalizeLadder(@Nullable String ladderName) {
        if (ladderName == null || ladderName.isBlank() || ladderName.equalsIgnoreCase("global")) {
            return null;
        }
        return ladderName;
    }

    public static int getPlayerLeaderboardRank(@NotNull Player player, @Nullable String ladderName, @NotNull String metricName) {
        LeaderboardEntry entry = getPlayerLeaderboardEntry(player, ladderName, metricName);
        if (entry != null) return entry.rank();
        if (!ZonePracticeApi.isAvailable()) return -1;
        try {
            return ZonePracticeApi.getInstance().getLeaderboardPosition(
                    player, toLeaderboardType(metricName), normalizeLadder(ladderName));
        } catch (Throwable ignored) {
            return -1;
        }
    }

    /**
     * Personal score for the selected leaderboard metric.
     * Returns {@link Integer#MIN_VALUE} when the public API cannot supply it.
     */
    public static int getPersonalScore(@NotNull Player player, @Nullable String ladderName, @NotNull String metricName) {
        LeaderboardEntry entry = getPlayerLeaderboardEntry(player, ladderName, metricName);
        if (entry != null) return entry.score();

        if (!ZonePracticeApi.isAvailable()) return Integer.MIN_VALUE;
        ZonePracticeApi api = ZonePracticeApi.getInstance();
        boolean global = ladderName == null || ladderName.equalsIgnoreCase("global") || ladderName.isBlank();
        String clean = metricName.toUpperCase(Locale.ROOT).replace("_", "");

        try {
            if (clean.startsWith("ELO")) {
                return global ? Integer.MIN_VALUE : api.getElo(player, ladderName);
            }
            if (clean.startsWith("WIN") && clean.contains("BEST")) {
                return global ? api.getBestWinStreak(player) : api.getLadderBestWinStreak(player, ladderName, WeightClass.RANKED);
            }
            if (clean.startsWith("WIN") && clean.contains("STREAK")) {
                return global ? api.getWinStreak(player) : Integer.MIN_VALUE;
            }
            if (clean.startsWith("WIN")) {
                if (global) return api.getGlobalWins(player);
                int ranked = api.getLadderWins(player, ladderName, WeightClass.RANKED);
                int unranked = api.getLadderWins(player, ladderName, WeightClass.UNRANKED);
                return Math.max(0, ranked) + Math.max(0, unranked);
            }
            if (clean.startsWith("KILL")) {
                return global ? api.getGlobalKills(player) : api.getLadderKills(player, ladderName);
            }
            if (clean.startsWith("DEATH")) {
                if (!global) return api.getLadderDeaths(player, ladderName);
                int deaths = 0;
                for (String name : api.getLadderNames()) {
                    deaths += Math.max(0, api.getLadderDeaths(player, name));
                }
                return deaths;
            }
        } catch (Throwable ignored) {
            // Fall through to unavailable.
        }
        return Integer.MIN_VALUE;
    }
}
