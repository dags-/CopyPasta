package me.dags.copy;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Value;
import me.dags.copy.util.Serializable;
import me.dags.copy.util.Utils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class PlayerData {

    private static final ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
    private final Map<ItemType, Brush> brushes = Maps.newHashMap();
    private final HoconConfigurationLoader loader;
    private final ConfigurationNode root;

    private boolean operating = false;
    private Stopwatch coolDown = Stopwatch.createStarted();

    public PlayerData(Path path) {
        loader = HoconConfigurationLoader.builder()
                .setDefaultOptions(options)
                .setPath(path)
                .build();

        root = Utils.getRootNode(loader);
    }

    public boolean isCoolingDown() {
        if (coolDown.elapsed(TimeUnit.MILLISECONDS) > 250) {
            coolDown.reset().start();
            return false;
        }
        return true;
    }

    public boolean isOperating() {
        return operating;
    }

    public void setOperating(boolean operating) {
        this.operating = operating;
    }

    public Optional<Brush> getBrush(Player player) {
        return player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem).flatMap(this::getBrush);
    }

    public Optional<Brush> getBrush(ItemType item) {
        return Optional.ofNullable(brushes.get(item));
    }

    @SuppressWarnings("unchecked")
    public Brush apply(Brush brush) {
        if (brush != null) {
            ConfigurationNode node = root.getNode(brush.getType().getId());
            for (Option<?> option : brush.getType().getOptions()) {
                ConfigurationNode child = node.getNode(option.getId());
                Object value = option.getDefault().get();

                if (!child.isVirtual()) {
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
        return brush;
    }

    public void setBrush(ItemType item, Brush brush) {
        brushes.put(item, brush);
    }

    public void removeBrush(ItemType type) {
        brushes.remove(type);
    }

    @SuppressWarnings("unchecked")
    public void save() {
        for (Brush brush : brushes.values()) {
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
        Utils.writeNode(loader, root);
    }
}
