package me.dags.copy.operation.calculator;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.operation.Operation;
import me.dags.copy.operation.visitor.Visitor2D;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;

import java.lang.ref.WeakReference;

/**
 * @author dags <dags@dags.me>
 */
public class Radius2D implements Calculator {

    private final WeakReference<World> world;
    private final BlockVolume volume;
    private final Vector3i offset;
    private final int radius2;
    private final int radius;
    private int dx = 0;
    private int dz = 0;

    public Radius2D(World world, Vector3i offset, int radius) {
        this(world, world, offset, radius);
    }

    public Radius2D(World world, BlockVolume volume, int radius) {
        this(world, volume, Vector3i.ZERO, radius);
    }

    public Radius2D(World world, BlockVolume volume, Vector3i offset, int radius) {
        this.world = new WeakReference<>(world);
        this.volume = volume;
        this.offset = offset;
        this.radius = radius;
        this.radius2 = radius * radius;
        reset();
    }

    @Override
    public void reset() {
        dx = 0;
        dz = 0;
    }

    @Override
    public Operation.Phase iterate(int limit, Visitor2D visitor) {
        World world = this.world.get();
        if (world == null) {
            return Operation.Phase.ERROR;
        }

        while (dz <= radius) {
            while (dx <= radius) {
                if ((dx * dx) + (dz * dz) < radius2) {
                    limit -= visit(visitor, world, dx, dz);

                    if (dx != 0) {
                        limit -= visit(visitor, world, -dx, dz);
                    }
                    if (dz != 0) {
                        limit -= visit(visitor, world, dx, -dz);
                    }
                    if (dx != 0 && dz != 0) {
                        limit -= visit(visitor, world, -dx, -dz);
                    }
                }

                dx++;

                if (limit <= 0) {
                    return Operation.Phase.CALCULATE;
                }
            }
            dx = 0;
            dz++;
        }
        return Operation.Phase.TEST;
    }

    private int visit(Visitor2D visitor2D, World world, int dx, int dz) {
        return visitor2D.visit(world, volume, offset.getX() + dx, offset.getZ() + dz);
    }
}
