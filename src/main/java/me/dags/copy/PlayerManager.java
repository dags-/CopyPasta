package me.dags.copy;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import me.dags.copy.util.Utils;
import me.dags.copy.util.fmt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;

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
        userDir = Utils.getDir(root.getDirectory(), "users");
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

    public void handle(Player player, Throwable t) {
        handle(player.getUniqueId(), t);
    }

    public void handle(UUID uuid, Throwable t) {
        PlayerData playerData = data.get(uuid);
        if (playerData != null) {
            playerData.setOperating(false);
            CopyPasta.getInstance().submitSync(() -> {
                t.printStackTrace();
                Sponge.getServer().getPlayer(uuid)
                        .ifPresent(fmt.warn("An error occurred: %s, see console", t.getClass().getSimpleName())::tell);
            });
        }
    }

    public static PlayerManager getInstance() {
        return INSTANCE;
    }
}
