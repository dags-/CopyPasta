package me.dags.copy.brush;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerData;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.option.Checks;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.OptionHolder;
import me.dags.copy.registry.brush.BrushRegistry;
import me.dags.copy.registry.brush.BrushType;
import me.dags.copy.util.fmt;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public interface Brush extends OptionHolder {

    Option<Integer> RANGE = Option.of("range", 128, Checks.range(1, 256));

    History getHistory();

    String getPermission();

    void primary(Player player, Vector3i pos, Action action);

    void secondary(Player player, Vector3i pos, Action action);

    void apply(Player player, Vector3i pos, History history);

    void undo(Player player, History history);

    default void primary(Player player, Vector3i pos) {
        if (isOperating(player)) {
            fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }
        primary(player, pos, Action.get(player));
    }

    default void secondary(Player player, Vector3i pos) {
        if (isOperating(player)) {
            fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }
        secondary(player, pos, Action.get(player));
    }

    default boolean isOperating(Player player) {
        return PlayerManager.getInstance().get(player).map(PlayerData::isOperating).orElse(false);
    }

    default BrushType getType() {
        return BrushRegistry.forClass(getClass());
    }

    default boolean supports(Option option) {
        return getType().getOption(option.getId()).isPresent();
    }

    default int getRange() {
        return getOptions().must(RANGE);
    }
}
