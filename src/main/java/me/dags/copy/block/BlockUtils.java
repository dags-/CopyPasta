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
        boolean inAir = isAir(world, pos.getX(), pos.getY(), pos.getZ());
        Vector3i down = findDown(world, pos, inAir);
        Vector3i up = findUp(world, pos, inAir);

        if (down == Vector3i.ZERO) {
            return up;
        }

        if (up == Vector3i.ZERO) {
            return down;
        }

        int downDistance = pos.getY() - down.getY();
        int upDistance = down.getY() - pos.getY();

        if (downDistance < upDistance) {
            return down;
        }

        return up;
    }

    private static Vector3i findDown(World world, Vector3i pos, boolean currentAir) {
        boolean wasInAir = currentAir;
        int max = pos.getY();
        for (int dy = 0; dy < max; dy++) {
            if (isAir(world, pos.getX(), pos.getY() - dy, pos.getZ())) {
                wasInAir = true;
            } else if (wasInAir) {
                return pos.add(0, -dy, 0);
            }
        }
        return Vector3i.ZERO;
    }

    private static Vector3i findUp(World world, Vector3i pos, boolean currentAir) {
        boolean wasInSolid = !currentAir;
        int max = 255 - pos.getY();
        for (int dy = 0; dy < max; dy++) {
            if (!isAir(world, pos.getX(), pos.getY() + dy, pos.getZ())) {
                wasInSolid = true;
            } else if (wasInSolid) {
                return pos.add(0, dy - 1, 0);
            }
        }
        return Vector3i.ZERO;
    }

    private static boolean isAir(World world, int x, int y, int z) {
        if (world.containsBlock(x, y, z)) {
            Optional<PassableProperty> property = world.getProperty(x, y, z, PassableProperty.class);
            return property.map(PassableProperty::getValue).orElse(true);
        }
        return true;
    }
}
