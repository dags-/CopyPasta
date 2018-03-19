package me.dags.copy;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import me.dags.config.Config;
import me.dags.config.Node;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.Preset;
import ninja.leaping.configurate.ConfigurationOptions;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * @author dags <dags@dags.me>
 */
public class PlayerData {

    private static final ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
    private final Map<ItemType, Brush> brushes = Maps.newHashMap();
    private final Config root;

    private boolean operating = false;
    private Stopwatch coolDown = Stopwatch.createStarted();

    public PlayerData(Path path) {
        root = Config.must(path);
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

    public void setBrush(ItemType item, Brush brush) {
        brushes.put(item, brush);
    }

    public void removeBrush(ItemType type) {
        brushes.remove(type);
    }

    public void load(Brush brush) {
        Node node = root.node(brush.getType().getId());
        Preset.read(brush, node);
    }

    public void save() {
        for (Brush brush : brushes.values()) {
            Node node = root.node(brush.getType().getId());
            Preset.write(brush, node);
        }
        root.save();
    }
}
