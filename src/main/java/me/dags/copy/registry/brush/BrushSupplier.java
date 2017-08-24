package me.dags.copy.registry.brush;

import me.dags.copy.brush.Brush;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public interface BrushSupplier {

    Brush create(Player player);
}
