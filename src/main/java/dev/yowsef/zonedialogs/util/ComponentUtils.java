package dev.yowsef.zonedialogs.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ComponentUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");

    private ComponentUtils() {}

    /**
     * Resolves a string message to an Adventure Component, supporting MiniMessage,
     * legacy '&' color codes, hex codes, and PlaceholderAPI (if installed).
     */
    @NotNull
    public static Component parse(@Nullable Player player, @Nullable String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String resolved = text;
        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            resolved = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, resolved);
        }

        // Section-sign codes are still used by many plugins and PlaceholderAPI
        // expansions. Treat them exactly like ampersand codes.
        resolved = resolved.replace('§', '&');

        // Convert legacy &#RRGGBB hex to MiniMessage <color:#RRGGBB>
        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(resolved);
        StringBuilder hexBuffer = new StringBuilder();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(hexBuffer, "<color:#" + hexMatcher.group(1) + ">");
        }
        hexMatcher.appendTail(hexBuffer);
        resolved = hexBuffer.toString();

        // Convert legacy &c to MiniMessage tags
        resolved = convertLegacyToMiniMessage(resolved);

        // Resolve <item:material> to native sprite tags
        resolved = ItemIcon.resolveItemTags(resolved);

        try {
            return MINI_MESSAGE.deserialize(resolved);
        } catch (Exception e) {
            return Component.text(text);
        }
    }

    @NotNull
    public static Component parse(@Nullable String text) {
        return parse(null, text);
    }

    private static String convertLegacyToMiniMessage(String text) {
        Matcher matcher = LEGACY_COLOR_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            char code = Character.toLowerCase(matcher.group(1).charAt(0));
            String tag = switch (code) {
                case '0' -> "<black>";
                case '1' -> "<dark_blue>";
                case '2' -> "<dark_green>";
                case '3' -> "<dark_aqua>";
                case '4' -> "<dark_red>";
                case '5' -> "<dark_purple>";
                case '6' -> "<gold>";
                case '7' -> "<gray>";
                case '8' -> "<dark_gray>";
                case '9' -> "<blue>";
                case 'a' -> "<green>";
                case 'b' -> "<aqua>";
                case 'c' -> "<red>";
                case 'd' -> "<light_purple>";
                case 'e' -> "<yellow>";
                case 'f' -> "<white>";
                case 'k' -> "<obfuscated>";
                case 'l' -> "<b>";
                case 'm' -> "<strikethrough>";
                case 'n' -> "<u>";
                case 'o' -> "<i>";
                case 'r' -> "<reset>";
                default -> "";
            };
            matcher.appendReplacement(sb, tag);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Plays a sound for the player safely by name.
     */
    public static void playSound(@NotNull Player player, @Nullable String soundName) {
        if (soundName == null || soundName.isBlank() || soundName.equalsIgnoreCase("none")) {
            return;
        }
        try {
            String raw = soundName.trim().toLowerCase(java.util.Locale.ROOT);
            String keyString = raw.contains(":") ? raw : "minecraft:" + raw.replace('_', '.');
            Sound sound = Sound.sound(Key.key(keyString), Sound.Source.MASTER, 1.0f, 1.0f);
            player.playSound(sound, Sound.Emitter.self());
        } catch (RuntimeException ignored) {
            // Unknown or malformed sound name
        }
    }
}
