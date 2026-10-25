package dev.yowsef.zonedialogs.session;

import dev.yowsef.zonedialogs.ZoneDialogsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final ZoneDialogsPlugin plugin;
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final File dataFolder;

    public SessionManager(@NotNull ZoneDialogsPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "userdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    @NotNull
    public PlayerSession getSession(@NotNull Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), this::loadSession);
    }

    @NotNull
    public PlayerSession getSession(@NotNull UUID uuid) {
        return sessions.computeIfAbsent(uuid, this::loadSession);
    }

    @Nullable
    public PlayerSession getLoadedSession(@NotNull UUID uuid) {
        return sessions.get(uuid);
    }

    private PlayerSession loadSession(UUID uuid) {
        PlayerSession session = new PlayerSession(uuid);
        File file = new File(dataFolder, uuid + ".yml");
        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            session.setSelectedCategory(config.getString("selected-category", "weapons"));
            session.setRanked(config.getBoolean("ranked", false));
            session.setQueueAll(config.getBoolean("queue-all", false));
            session.setKeepQueuing(config.getBoolean("keep-queuing", false));

            List<String> favList = config.getStringList("favorites");
            session.setFavorites(new HashSet<>(favList));
        }
        return session;
    }

    public void saveSession(@NotNull PlayerSession session) {
        File file = new File(dataFolder, session.getPlayerUuid() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("selected-category", session.getSelectedCategory());
        config.set("ranked", session.isRanked());
        config.set("queue-all", session.isQueueAll());
        config.set("keep-queuing", session.isKeepQueuing());
        config.set("favorites", new ArrayList<>(session.getFavorites()));

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save userdata for " + session.getPlayerUuid() + ": " + e.getMessage());
        }
    }

    public void removeSession(@NotNull UUID uuid) {
        PlayerSession session = sessions.remove(uuid);
        if (session != null) {
            saveSession(session);
        }
    }

    public void saveAll() {
        for (PlayerSession session : sessions.values()) {
            saveSession(session);
        }
    }
}
