package dev.yowsef.zonedialogs.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ItemIcon {

    private static final String ITEMS_ATLAS = "minecraft:items";
    private static final String BLOCKS_ATLAS = "minecraft:blocks";
    private static final Pattern TAG_PATTERN = Pattern.compile("<(item|block):([a-zA-Z0-9_:#]+)>");

    private ItemIcon() {}

    @NotNull
    public static String tag(@NotNull String type, @NotNull String materialName) {
        String clean = materialName.toLowerCase().replace("minecraft:", "").replace(":", "/");

        boolean explicitBlock = "block".equalsIgnoreCase(type);
        boolean explicitItem = "item".equalsIgnoreCase(type);

        // Only guess the atlas when the caller did not specify one. Previously
        // <item:glass_bottle> and <item:stone_sword> were forced into the block
        // atlas and rendered as missing sprites.
        boolean block = explicitBlock || (!explicitItem && looksLikeBlock(clean));

        if (block) {
            if (!clean.startsWith("block/")) {
                clean = "block/" + clean;
            }
            return "<sprite:\"" + BLOCKS_ATLAS + "\":\"minecraft:" + clean + "\">";
        } else {
            if (!clean.startsWith("item/")) {
                clean = "item/" + clean;
            }
            return "<sprite:\"" + ITEMS_ATLAS + "\":\"minecraft:" + clean + "\">";
        }
    }

    private static boolean looksLikeBlock(String clean) {
        return clean.contains("concrete")
                || clean.contains("wool")
                || clean.contains("terracotta")
                || clean.contains("planks")
                || clean.contains("stone")
                || clean.contains("glass")
                || clean.contains("ore")
                || clean.contains("log");
    }

    /**
     * Auto-detects the atlas from the Bukkit material.
     */
    @NotNull
    public static String tag(@NotNull String materialName) {
        return tag("auto", materialName);
    }

    /**
     * Auto-detects the atlas from the Bukkit material type.
     */
    @NotNull
    public static String tag(@NotNull Material material) {
        return tag(material.isBlock() ? "block" : "item", material.name());
    }

    @NotNull
    public static Component of(@NotNull Material material) {
        return MiniMessage.miniMessage().deserialize(tag(material));
    }

    @Nullable
    public static String resolveItemTags(@Nullable String text) {
        if (text == null || (!text.contains("<item:") && !text.contains("<block:"))) {
            return text;
        }

        Matcher matcher = TAG_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String type = matcher.group(1);
            String matName = matcher.group(2);
            String spriteTag = tag(type, matName);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(spriteTag));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
