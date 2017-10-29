package me.dags.copy.block;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.property.block.PassableProperty;
import org.spongepowered.api.world.World;

import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class BlockUtils {

    private static final Predicate<BlockState> PASSABLE = b -> b.getProperty(PassableProperty.class).map(PassableProperty::getValue).orElse(false);

    public static Vector3i findSolidFoundation(World world, Vector3i pos) {
        int y = findSurface(world, pos.getX(), pos.getZ(), 0, 255);
        while (PASSABLE.test(world.getBlock(pos.getX(), y, pos.getZ()))) {
            y--;
        }
        return new Vector3i(pos.getX(), y, pos.getZ());
    }

    public static int findSurface(World world, int x, int z, int min, int max) {
        // mid point between min and max
        int mid = min + ((max - min) >> 1);

        // if no change we have hit the target
        if (mid == min) {
            return mid;
        }

        if (world.getBlockType(x, mid, z) == BlockTypes.AIR) {
            // position above surface, search lower half
            return findSurface(world, x, z, min, mid);
        }

        // position below surface, search upper half
        return findSurface(world, x, z, mid, max);
    }
}
