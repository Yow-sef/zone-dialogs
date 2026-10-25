package dev.yowsef.zonedialogs.session;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerSession {

    private final UUID playerUuid;
    private String selectedCategory = "weapons";
    private boolean ranked = false;
    private boolean queueAll = false;
    private boolean keepQueuing = false;
    private final Set<String> favorites = new HashSet<>();
    private MenuType activeMenu = MenuType.NONE;

    public PlayerSession(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public boolean isRanked() {
        return ranked;
    }

    public void setRanked(boolean ranked) {
        this.ranked = ranked;
    }

    public void toggleRanked() {
        this.ranked = !this.ranked;
    }

    public boolean isQueueAll() {
        return queueAll;
    }

    public void setQueueAll(boolean queueAll) {
        this.queueAll = queueAll;
    }

    public void toggleQueueAll() {
        this.queueAll = !this.queueAll;
    }

    public boolean isKeepQueuing() {
        return keepQueuing;
    }

    public void setKeepQueuing(boolean keepQueuing) {
        this.keepQueuing = keepQueuing;
    }

    public void toggleKeepQueuing() {
        this.keepQueuing = !this.keepQueuing;
    }

    public Set<String> getFavorites() {
        return Collections.unmodifiableSet(favorites);
    }

    public boolean isFavorite(String ladder) {
        return favorites.contains(ladder.toLowerCase());
    }

    public void toggleFavorite(String ladder) {
        String key = ladder.toLowerCase();
        if (favorites.contains(key)) {
            favorites.remove(key);
        } else {
            favorites.add(key);
        }
    }

    public void addFavorite(String ladder) {
        favorites.add(ladder.toLowerCase());
    }

    public void setFavorites(Set<String> newFavorites) {
        favorites.clear();
        for (String f : newFavorites) {
            favorites.add(f.toLowerCase());
        }
    }

    public MenuType getActiveMenu() {
        return activeMenu;
    }

    public void setActiveMenu(MenuType activeMenu) {
        this.activeMenu = activeMenu != null ? activeMenu : MenuType.NONE;
    }

    public boolean isMenuOpen(MenuType menu) {
        return activeMenu == menu;
    }

    public boolean hasMenuOpen() {
        return activeMenu != MenuType.NONE;
    }
}
