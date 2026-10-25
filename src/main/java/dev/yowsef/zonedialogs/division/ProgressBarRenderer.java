package dev.yowsef.zonedialogs.division;

import dev.yowsef.zonedialogs.config.QueueMenuConfig;
import dev.yowsef.zonedialogs.util.ComponentUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ProgressBarRenderer {

    public ProgressBarRenderer() {}

    @NotNull
    public Component render(double progressPercent, @NotNull QueueMenuConfig config) {
        return render(
                progressPercent,
                config.getProgressBarLength(),
                config.getProgressBarFilledChar(),
                config.getProgressBarEmptyChar(),
                config.getProgressBarFilledColor(),
                config.getProgressBarEmptyColor(),
                "lime_concrete"
        );
    }

    @NotNull
    public Component render(
            double progressPercent,
            int length,
            String filledChar,
            String emptyChar,
            String filledColor,
            String emptyColor
    ) {
        return render(progressPercent, length, filledChar, emptyChar, filledColor, emptyColor, "orange_concrete");
    }

    @NotNull
    public Component render(
            double progressPercent,
            int length,
            String filledChar,
            String emptyChar,
            String filledColor,
            String emptyColor,
            String defaultFilledBlock
    ) {
        int barLength = Math.max(5, length);
        filledChar = normalizeProgressChar(filledChar, defaultFilledBlock);
        emptyChar = normalizeProgressChar(emptyChar, "black_concrete");

        double clamped = Math.max(0.0, Math.min(1.0, progressPercent));
        int filledCount = (int) Math.round(clamped * barLength);
        int emptyCount = barLength - filledCount;

        Component result = Component.empty();
        if (filledCount > 0) {
            String filledStr = filledChar.repeat(filledCount);
            String toParse = isSpriteOrBlock(filledChar) || filledColor == null || filledColor.isBlank()
                    ? filledStr
                    : filledColor + filledStr;
            result = result.append(ComponentUtils.parse(toParse));
        }
        if (emptyCount > 0) {
            String emptyStr = emptyChar.repeat(emptyCount);
            String toParse = isSpriteOrBlock(emptyChar) || emptyColor == null || emptyColor.isBlank()
                    ? emptyStr
                    : emptyColor + emptyStr;
            result = result.append(ComponentUtils.parse(toParse));
        }

        return result;
    }

    private static boolean isSpriteOrBlock(String charStr) {
        return charStr.contains("<sprite:") || charStr.contains("<block:") || charStr.contains("<item:");
    }

    private static String normalizeProgressChar(String raw, String defaultBlock) {
        if (raw == null || raw.isBlank()) {
            return "<block:" + defaultBlock + ">";
        }
        String trim = raw.trim();
        if (trim.startsWith("<") && trim.endsWith(">")) {
            return trim;
        }
        // If the user specified a block or item name like "lime_concrete" or "black_concrete"
        if (trim.matches("(?i)[a-z0-9_]+(concrete|wool|terracotta|block|glass|stone)[a-z0-9_]*")) {
            return "<block:" + trim.toLowerCase() + ">";
        }
        return trim;
    }
}
