package dev.yowsef.zonedialogs.util;

import dev.yowsef.zonedialogs.bridge.ZonePracticeBridge;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LadderNames {

    private LadderNames() {}

    /**
     * Returns the ladder name as a MiniMessage component. ZonePractice display
     * names are already MiniMessage, so they must never be inserted into a
     * string and parsed a second time.
     */
    @NotNull
    public static Component display(@Nullable Player player, @NotNull String ladder, @NotNull String fallbackColor) {
        String zpDisplay = ZonePracticeBridge.getLadderDisplayName(ladder);
        if (zpDisplay != null && !zpDisplay.isBlank()) {
            return ComponentUtils.parse(player, zpDisplay);
        }
        return ComponentUtils.parse(player, fallbackColor + "<b>" + pretty(ladder) + "</b>");
    }

    @NotNull
    public static Component display(@NotNull String ladder, @NotNull String fallbackColor) {
        return display(null, ladder, fallbackColor);
    }

    /**
     * Human-readable fallback for ladder ids when the core has no display name.
     */
    @NotNull
    public static String pretty(@Nullable String ladder) {
        if (ladder == null || ladder.isBlank()) return "";

        String lower = ladder.toLowerCase();
        return switch (lower) {
            case "axe" -> "Axe & Shield";
            case "endgame" -> "End Game";
            case "earlygame" -> "Early Game";
            case "spearelytra" -> "Spear Elytra";
            case "spearmace" -> "Spear Mace";
            case "tntsumo" -> "TNT Sumo";
            case "builduhc" -> "Build UHC";
            case "mlgrush" -> "MLG Rush";
            case "bedwars" -> "Bed Wars";
            default -> Character.toUpperCase(ladder.charAt(0)) + ladder.substring(1).toLowerCase();
        };
    }

    @NotNull
    public static String plain(@Nullable Player player, @NotNull String ladder) {
        String zpDisplay = ZonePracticeBridge.getLadderDisplayName(ladder);
        if (zpDisplay == null || zpDisplay.isBlank()) return pretty(ladder);
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(ComponentUtils.parse(player, zpDisplay));
    }
}
