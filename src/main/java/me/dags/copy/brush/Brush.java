package me.dags.copy.brush;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Options;
import me.dags.copy.registry.brush.BrushRegistry;
import me.dags.copy.registry.brush.BrushType;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public interface Brush {

    Option<Integer> RANGE = Option.of("range", 5);

    Options getOptions();

    History getHistory();

    String getPermission();

    void primary(Player player, Vector3i pos, Action action);

    void secondary(Player player, Vector3i pos, Action action);

    void undo(Player player);

    default void primary(Player player, Vector3i pos) {
        primary(player, pos, Action.get(player));
    }

    default void secondary(Player player, Vector3i pos) {
        secondary(player, pos, Action.get(player));
    }

    default BrushType getType() {
        return BrushRegistry.forClass(getClass());
    }

    default <T> T getOption(Option<T> option) {
        return getOptions().get(option);
    }

    default <T> T mustOption(Option<T> option) {
        return getOptions().must(option);
    }

    default void setOption(Option option, Object value) {
        getOptions().set(option, value);
    }

    default int getRange() {
        return getOptions().must(RANGE);
    }
}
