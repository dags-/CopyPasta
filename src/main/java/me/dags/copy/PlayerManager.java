package me.dags.copy;

import me.dags.copy.util.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
public class PlayerManager {

    private static final PlayerManager INSTANCE = new PlayerManager();

    private final Path userDir;
    private final PluginContainer container;
    private final Function<UUID, PlayerData> factory;
    private final Map<UUID, PlayerData> data = new HashMap<>();

    private PlayerManager() {
        PluginContainer plugin = Sponge.getPluginManager().getPlugin(CopyPasta.ID).orElseThrow(IllegalStateException::new);
        Object instance = plugin.getInstance().orElseThrow(IllegalStateException::new);
        ConfigRoot root = Sponge.getGame().getConfigManager().getPluginConfig(instance);
        container = plugin;
        userDir = Utils.ensure(root.getDirectory(), "users");
        factory = uuid -> new PlayerData(userDir.resolve(uuid + ".conf"));
    }

    public PlayerData must(Player player) {
        return data.computeIfAbsent(player.getUniqueId(), factory);
    }

    public Optional<PlayerData> get(Player player) {
        return get(player.getUniqueId());
    }

    public Optional<PlayerData> get(UUID uuid) {
        return Optional.ofNullable(data.get(uuid));
    }

    public void drop(Player player) {
        drop(player.getUniqueId());
    }

    public void drop(UUID uuid) {
        PlayerData playerData = data.remove(uuid);
        if (playerData != null) {
            playerData.save();
        }
    }

    public static PlayerManager getInstance() {
        return INSTANCE;
    }

    public static Cause getCause(Player player) {
        return Cause.source(getInstance().container)
                .notifier(player)
                .owner(player)
                .build();
    }
}
