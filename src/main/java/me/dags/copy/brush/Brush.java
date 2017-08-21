package me.dags.copy.brush;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.registry.brush.BrushRegistry;
import me.dags.copy.registry.brush.BrushType;
import me.dags.copy.registry.option.Option;
import me.dags.copy.registry.option.BrushOptions;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public interface Brush {

    Option RANGE = Option.of("range");

    BrushOptions getOptions();

    String getPermission();

    void primary(Player player, Vector3i pos, Action action);

    void secondary(Player player, Vector3i pos, Action action);

    default void primary(Player player, Vector3i pos) {
        primary(player, pos, Action.get(player));
    }

    default void secondary(Player player, Vector3i pos) {
        secondary(player, pos, Action.get(player));
    }

    default BrushType getType() {
        return BrushRegistry.forClass(this.getClass());
    }

    default int getRange() {
        return getOptions().ensure(RANGE, 5);
    }

    default <T> T getOption(Option option, T def) {
        return getOptions().get(option, def);
    }

    default void setOption(Option option, Object value) {
        getOptions().set(option, value);
    }

    default void setRange(int range) {
        getOptions().set(RANGE, range);
    }
}
