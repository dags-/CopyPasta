package me.dags.copy;

import com.google.common.base.Stopwatch;
import me.dags.copy.brush.Brush;
import me.dags.copy.registry.brush.BrushType;
import me.dags.copy.util.Utils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.item.ItemType;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class PlayerData {

    private static final ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);

    private final Map<ItemType, Brush> brushes = new HashMap<>();
    private final Map<Class<?>, ItemType> wands = new HashMap<>();
    private final HoconConfigurationLoader loader;
    private final ConfigurationNode rootNode;

    private boolean operating = false;
    private Stopwatch coolDown = Stopwatch.createStarted();

    public PlayerData(Path path) {
        loader = HoconConfigurationLoader.builder()
                .setDefaultOptions(options)
                .setPath(path)
                .build();

        rootNode = Utils.getRootNode(loader);
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

    public Optional<Brush> getBrush(ItemType item) {
        return Optional.ofNullable(brushes.get(item));
    }

    public Optional<Brush> getBrush(BrushType type) {
        return getBrush(type.getType()).map(b -> b);
    }

    public <T extends Brush> Optional<T> getBrush(Class<T> type) {
        ItemType item = wands.get(type);
        if (item != null) {
            Brush brush = brushes.get(item);
            if (brush != null && type.isInstance(brush)) {
                return Optional.of(type.cast(brush));
            }
        }
        return Optional.empty();
    }

    public boolean resetBrush(Brush brush) {
        ItemType item = wands.get(brush.getClass());
        return item != null && resetBrush(brush, item);
    }

    public boolean resetBrush(Brush brush, ItemType type) {
        brushes.put(type, brush);
        wands.put(brush.getClass(), type);
        return true;
    }

    public void removeBrush(ItemType type) {
        Brush brush = brushes.remove(type);
        if (brush != null) {
            wands.remove(brush.getClass());
        }
    }

    public void removeBrush(Class<? extends Brush> brush) {
        ItemType item = wands.remove(brush);
        if (item != null) {
            brushes.remove(item);
        }
    }

    public void save() {
        Utils.writeNode(loader, rootNode);
    }

    private ConfigurationNode getNode(String path) {
        return rootNode.getNode(path);
    }
}
