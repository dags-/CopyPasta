package me.dags.copy.block;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.property.block.PassableProperty;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class BlockUtils {

    public static Vector3i findSolidFoundation(World world, Vector3i pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();

        boolean downAir = isAir(world, x, y, z);
        boolean upSolid = !downAir;

        for (int dy = 0; dy < 255; dy++) {
            int up = y + dy;
            if (isAir(world, x, up, z)) {
                if (upSolid) {
                    // air block above solid block
                    return new Vector3i(x, up, z);
                }
            } else {
                upSolid = true;
            }

            int down = y - dy;
            if (isAir(world, x, down, z)) {
                downAir = true;
            } else if (downAir) {
                return new Vector3i(x, down + 1, z);

            }

            if (down < 0 && up > 255) {
                break;
            }
        }

        return Vector3i.ZERO;
    }

    public static boolean isAir(World world, int x, int y, int z) {
        if (world.containsBlock(x, y, z)) {
            Optional<PassableProperty> property = world.getProperty(x, y, z, PassableProperty.class);
            return property.map(PassableProperty::getValue).orElse(true);
        }
        return true;
    }
}
