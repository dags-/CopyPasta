package me.dags.copy.brush;

import me.dags.config.Config;
import me.dags.config.Node;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Value;
import me.dags.copy.registry.brush.BrushRegistry;
import me.dags.copy.util.IgnoreSerialization;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Preset {

    private final Node node;

    private Preset(Node node) {
        this.node = node;
    }

    public Preset(String name, Brush brush) {
        node = Node.create();
        node.set("name", name);
        node.set("type", brush.getType().getId());
        write(brush, node.node("options"));
    }

    public String getName() {
        return node.get("name", "");
    }

    public Optional<BrushType> getType() {
        return BrushRegistry.getInstance().getById(node.get("type", ""));
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

        read(brush.get(), node.node("options"));
        return brush;
    }

    public static Optional<Preset> load(Path path) {
        if (Files.exists(path)) {
            return Optional.of(new Preset(Config.must(path)));
        } else {
            return Optional.empty();
        }
    }

    public static void save(Preset preset, Path path) {
        Config config = Config.must(path);
        config.set(preset.node.backing());
        config.save();
    }

    @SuppressWarnings("unchecked")
    public static void read(Brush brush, Node node) {
        for (Option<?> option : brush.getType().getOptions()) {
            Node child = node.node(option.getId());
            Object def = option.getDefault().get();
            Object value = def;

            if (!child.backing().isVirtual()) {
                if (value instanceof IgnoreSerialization) {
                    continue;
                }

                if (value instanceof Node.Deserializable) {
                    Node.Deserializable<?> deserializable = (Node.Deserializable) value;
                    value = child.get(deserializable);
                } else if (CatalogType.class.isAssignableFrom(option.getType())) {
                    Class<? extends CatalogType> clazz = (Class<? extends CatalogType>) option.getType();
                    Optional<? extends CatalogType> type = Sponge.getRegistry().getType(clazz, child.get(""));
                    if (type.isPresent()) {
                        value = type.get();
                    }
                } else {
                    value = child.backing().getValue(value);
                }
            }

            if (value != def && option.accepts(value)) {
                brush.setOption(option, value);
            }
        }
    }

    public static void write(Brush brush, Node node) {
        node.clear();
        brush.getOptions().forEach((option, o) -> {
            Value def = option.getDefault();
            if (def.isPresent() && def.get().equals(o)) {
                return;
            }

            Node value = node.node(option.getId());
            if (o instanceof Node.Serializable) {
                Node.Serializable serializable = (Node.Serializable) o;
                value.set(serializable);
            } else if (o instanceof CatalogType) {
                CatalogType t = (CatalogType) o;
                value.set(t.getId());
            } else {
                try {
                    value.set(o);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
