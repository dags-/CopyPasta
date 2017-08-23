package me.dags.copy;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Value;
import me.dags.copy.util.Utils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
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

    public Brush apply(Brush brush) {
        ConfigurationNode node = root.getNode(brush.getType().getId());
        for (Option<?> option : brush.getType().getOptions()) {
            ConfigurationNode child = node.getNode(option.getId());
            Object value = child.getValue();
            if (option.accepts(value)) {
                brush.setOption(option, value);
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

    public void save() {
        for (Brush brush : brushes.values()) {
            brush.getOptions().forEach((option, o) -> {
                ConfigurationNode node = root.getNode(brush.getType().getId());
                Value def = option.getDefault();
                if (def.isPresent() && def.get().equals(o)) {
                    node.removeChild(option.getId());
                    return;
                }
                node.getNode(option.getId()).setValue(o);
            });
        }

        Utils.writeNode(loader, root);
    }
}
