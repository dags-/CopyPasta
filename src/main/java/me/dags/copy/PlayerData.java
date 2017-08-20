package me.dags.copy;

import com.google.common.base.Stopwatch;
import me.dags.copy.brush.Brush;
import me.dags.copy.registry.brush.BrushType;
import org.spongepowered.api.item.ItemType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class PlayerData {

    private final Map<ItemType, Brush> brushes = new HashMap<>();
    private final Map<Class<?>, ItemType> wands = new HashMap<>();

    private boolean operating = false;
    private Stopwatch cooldown = Stopwatch.createStarted();

    public boolean isCoolingDown() {
        if (cooldown.elapsed(TimeUnit.MILLISECONDS) > 250) {
            cooldown.reset().start();
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

    public Optional<? extends Brush> getBrush(BrushType type) {
        return getBrush(type.getType());
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

    public void assignBrush(Brush brush, ItemType type) {
        brushes.put(type, brush);
        wands.put(brush.getClass(), type);
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
}
