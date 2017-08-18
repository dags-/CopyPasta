package me.dags.copy.brush;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public enum Action {
    PRIMARY,
    SECONDARY,
    ;

    public static Action get(Player player) {
        return player.get(Keys.IS_SNEAKING).orElse(false) ? SECONDARY : PRIMARY;
    }
}
