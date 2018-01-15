package me.dags.copy.block;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.property.AbstractProperty;
import org.spongepowered.api.data.property.block.HardnessProperty;
import org.spongepowered.api.data.property.block.PassableProperty;
import org.spongepowered.api.world.World;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class BlockUtils {

    private static final Function<PassableProperty, Boolean> PASSABLE = AbstractProperty::getValue;
    private static final Function<HardnessProperty, Boolean> SOFT = h -> h.getValue() != null && h.getValue() < 0.5D;
    private static final Predicate<BlockState> SKIP = b -> b.getProperty(PassableProperty.class).map(PASSABLE).orElse(false)
                    || b.getProperty(HardnessProperty.class).map(SOFT).orElse(false);

    public static Vector3i findSolidFoundation(World world, Vector3i pos) {
        int y = 255;
        while (SKIP.test(world.getBlock(pos.getX(), y, pos.getZ()))) {
            y--;
        }
        return new Vector3i(pos.getX(), y, pos.getZ());
    }
}
