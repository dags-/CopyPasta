package me.dags.copy.brush;

import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Value;
import me.dags.copy.registry.brush.BrushRegistry;
import me.dags.copy.registry.brush.BrushType;
import me.dags.copy.util.IgnoreSerialization;
import me.dags.copy.util.Serializable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Preset {

    private final ConfigurationNode node;

    private Preset(ConfigurationNode node) {
        this.node = node;
    }

    public Preset(String name, Brush brush) {
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().build();
        node = loader.createEmptyNode();
        node.getNode("name").setValue(name);
        node.getNode("type").setValue(brush.getType().getId());
        ConfigurationNode options = node.getNode("options");
        write(brush, options);
    }

    public String getName() {
        return node.getNode("name").getString("");
    }

    public Optional<BrushType> getType() {
        return BrushRegistry.getInstance().getById(node.getNode("type").getString(""));
    }

    public Optional<Brush> getInstance(Player player) {
        Optional<BrushType> type = getType();
        if (!type.isPresent()) {
            return Optional.empty();
        }

        Optional<Brush> brush = type.get().create(player, false);
        if (!brush.isPresent()) {
            return Optional.empty();
        }

        read(brush.get(), node.getNode("options"));
        return brush;
    }

    public static Optional<Preset> load(Path path) {
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setPath(path).build();
        try {
            ConfigurationNode node = loader.load();
            return Optional.of(new Preset(node));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static void save(Preset preset, Path path) {
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setPath(path).build();
        try {
            loader.save(preset.node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void read(Brush brush, ConfigurationNode node) {
        for (Option<?> option : brush.getType().getOptions()) {
            ConfigurationNode child = node.getNode(option.getId());
            Object value = option.getDefault().get();

            if (!child.isVirtual()) {
                if (value instanceof IgnoreSerialization) {
                    continue;
                }

                if (value instanceof Serializable) {
                    try {
                        Serializable<?> serializable = Serializable.class.cast(value);
                        value = serializable.getSerializer().deserialize(serializable.getToken(), child);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (CatalogType.class.isAssignableFrom(option.getType())) {
                    Class<? extends CatalogType> clazz = (Class<? extends CatalogType>) option.getType();
                    Optional<? extends CatalogType> type = Sponge.getRegistry().getType(clazz, child.getString());
                    if (type.isPresent()) {
                        value = type.get();
                    }
                } else {
                    value = child.getValue();
                }
            }

            if (option.accepts(value)) {
                brush.setOption(option, value);
            }
        }
    }

    public static void write(Brush brush, ConfigurationNode root) {
        brush.getOptions().forEach((option, o) -> {
            ConfigurationNode node = root.getNode(brush.getType().getId());
            node.removeChild(option.getId());

            Value def = option.getDefault();
            if (def.isPresent() && def.get().equals(o)) {
                return;
            }

            ConfigurationNode value = node.getNode(option.getId());
            if (o instanceof Serializable) {
                try {
                    Serializable serializable = Serializable.class.cast(o);
                    serializable.getSerializer().serialize(serializable.getToken(), o, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (o instanceof CatalogType) {
                CatalogType t = (CatalogType) o;
                value.setValue(t.getId());
            } else {
                try {
                    value.setValue(o);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
