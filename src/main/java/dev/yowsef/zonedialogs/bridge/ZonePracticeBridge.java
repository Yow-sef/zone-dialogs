package dev.yowsef.zonedialogs.bridge;

import dev.nandi0813.api.Enum.LeaderboardType;
import dev.nandi0813.api.Enum.PlayerState;
import dev.nandi0813.api.Enum.WeightClass;
import dev.nandi0813.api.ZonePracticeApi;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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

    public record LeaderboardRecord(
            String playerName,
            UUID playerUuid,
            int score,
            int rank
    ) {}

    // The core does not expose top leaderboard entries, so this is the one place
    // that has to reflect into it. The lookups are cached and failures are logged
    // once instead of silently returning "no data" forever.
    private static boolean reflectionAttempted;
    private static boolean reflectionFailed;
    private static Class<?> leaderboardManagerClass;
    private static Method getManagerMethod;
    private static Method getBoardsMethod;
    private static Method boardGetScope;
    private static Method boardGetMetric;
    private static Method boardGetLadder;
    private static Method boardGetEntries;
    private static Method ladderGetName;
    private static boolean reflectionWarningLogged;

    private static synchronized void initReflection() {
        if (reflectionAttempted) return;
        reflectionAttempted = true;
        try {
            leaderboardManagerClass = Class.forName("dev.nandi0813.practice.manager.leaderboard.b");
            getManagerMethod = leaderboardManagerClass.getMethod("a");
            getBoardsMethod = leaderboardManagerClass.getMethod("b");
        } catch (Throwable t) {
            reflectionFailed = true;
            logReflectionWarning("leaderboard manager", t);
        }
    }

    private static void initBoardReflection(@NotNull Class<?> boardClass) {
        if (boardGetScope != null) return;
        try {
            boardGetScope = boardClass.getMethod("b");
            boardGetMetric = boardClass.getMethod("c");
            boardGetLadder = boardClass.getMethod("d");
            boardGetEntries = boardClass.getMethod("e");
            ladderGetName = boardGetLadder.getReturnType().getMethod("g");
        } catch (Throwable t) {
            reflectionFailed = true;
            logReflectionWarning("leaderboard board", t);
        }
    }

    private static void logReflectionWarning(String area, Throwable t) {
        if (reflectionWarningLogged) return;
        reflectionWarningLogged = true;
        Bukkit.getLogger().warning("[ZoneDialogs] Could not access the " + area
                + " API: " + t.getClass().getSimpleName() + ": " + t.getMessage()
                + " — leaderboard entries will be unavailable until this is updated.");
    }

    @NotNull
    public static List<LeaderboardRecord> getLeaderboardEntries(@Nullable String ladderName, @NotNull String metricName, int limit) {
        List<LeaderboardRecord> raw = new ArrayList<>();
        initReflection();
        if (reflectionFailed) return raw;

        try {
            Object manager = getManagerMethod.invoke(null);
            if (manager == null) return raw;

            List<?> boards = (List<?>) getBoardsMethod.invoke(manager);
            if (boards == null) return raw;

            boolean isGlobal = ladderName == null
                    || ladderName.equalsIgnoreCase("global")
                    || ladderName.isBlank();

            for (Object board : boards) {
                if (board == null) continue;
                initBoardReflection(board.getClass());
                if (reflectionFailed) return raw;

                String scopeName = String.valueOf(boardGetScope.invoke(board));
                boolean globalBoard = scopeName.equalsIgnoreCase("GLOBAL") || scopeName.equalsIgnoreCase("OVERALL");
                boolean ladderBoard = scopeName.equalsIgnoreCase("LADDER") || scopeName.equalsIgnoreCase("KIT");
                if (isGlobal && !globalBoard) continue;
                if (!isGlobal && !ladderBoard) continue;

                String metricVal = String.valueOf(boardGetMetric.invoke(board));
                if (!metricMatches(metricVal, metricName)) continue;

                if (!isGlobal) {
                    Object ladderObj = boardGetLadder.invoke(board);
                    if (ladderObj == null) continue;
                    String boardLadder = (String) ladderGetName.invoke(ladderObj);
                    if (boardLadder == null || !boardLadder.equalsIgnoreCase(ladderName)) continue;
                }

                Object mapObj = boardGetEntries.invoke(board);
                if (mapObj instanceof Map<?, ?> map) {
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        Object key = entry.getKey();
                        Object val = entry.getValue();

                        String playerName = "Unknown";
                        UUID playerUuid = null;
                        if (key instanceof OfflinePlayer op) {
                            playerName = op.getName() != null ? op.getName() : "Unknown";
                            playerUuid = op.getUniqueId();
                        } else if (key instanceof UUID id) {
                            OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                            playerName = op.getName() != null ? op.getName() : "Unknown";
                            playerUuid = id;
                        } else if (key instanceof String s) {
                            playerName = s;
                        }

                        int score = val instanceof Number n ? n.intValue() : 0;
                        raw.add(new LeaderboardRecord(playerName, playerUuid, score, 0));
                    }
                }
                break;
            }
        } catch (Throwable t) {
            reflectionFailed = true;
            logReflectionWarning("leaderboard entries", t);
            return raw;
        }

        // Never trust the internal map's iteration order.
        raw.sort(Comparator.comparingInt(LeaderboardRecord::score).reversed());

        List<LeaderboardRecord> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            if (limit > 0 && i >= limit) break;
            LeaderboardRecord record = raw.get(i);
            result.add(new LeaderboardRecord(record.playerName(), record.playerUuid(), record.score(), i + 1));
        }
        return result;
    }

    public static int getPlayerLeaderboardRank(@NotNull Player player, @Nullable String ladderName, @NotNull String metricName) {
        if (!ZonePracticeApi.isAvailable()) return -1;
        try {
            LeaderboardType lbType = LeaderboardType.ELO;
            String clean = metricName.toUpperCase(Locale.ROOT).replace("_", "");
            if (clean.contains("WIN") && clean.contains("BEST")) {
                lbType = LeaderboardType.BEST_WIN_STREAK;
            } else if (clean.contains("LOSE") && clean.contains("BEST")) {
                lbType = LeaderboardType.BEST_LOSE_STREAK;
            } else if (clean.contains("LOSE")) {
                lbType = LeaderboardType.LOSE_STREAK;
            } else if (clean.contains("STREAK")) {
                lbType = LeaderboardType.WIN_STREAK;
            } else if (clean.startsWith("WIN")) {
                lbType = LeaderboardType.WINS;
            } else if (clean.startsWith("KILL")) {
                lbType = LeaderboardType.KILLS;
            } else if (clean.startsWith("DEATH")) {
                lbType = LeaderboardType.DEATHS;
            }

            return ZonePracticeApi.getInstance().getLeaderboardPosition(player, lbType, ladderName);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    /**
     * Personal score for the selected leaderboard metric.
     * Returns {@link Integer#MIN_VALUE} when the public API cannot supply it.
     */
    public static int getPersonalScore(@NotNull Player player, @Nullable String ladderName, @NotNull String metricName) {
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

    private static boolean metricMatches(String enumName, String requested) {
        String e = enumName.toUpperCase(Locale.ROOT).replace("_", "");
        String r = requested.toUpperCase(Locale.ROOT).replace("_", "");
        if (e.equals(r)) return true;
        if (r.equals("WINS") && e.equals("WIN")) return true;
        if (r.equals("WIN") && e.equals("WINS")) return true;
        if (r.equals("STREAK") && e.equals("WINSTREAK")) return true;
        if (r.equals("BEST") && e.equals("BESTWINSTREAK")) return true;
        return false;
    }
}
