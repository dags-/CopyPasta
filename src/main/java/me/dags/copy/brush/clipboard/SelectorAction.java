package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public interface SelectorAction {

    void commit(Player player, Vector3i min, Vector3i max, Vector3i origin, int size);
}
