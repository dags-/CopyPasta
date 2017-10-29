package me.dags.copy.operation.calculator;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.operation.Operation;
import me.dags.copy.operation.visitor.Visitor3D;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;

import java.lang.ref.WeakReference;

/**
 * @author dags <dags@dags.me>
 */
public class Radius3D implements Calculator {

    private final WeakReference<World> world;
    private final BlockVolume volume;
    private final Vector3i offset;
    private final int radius;
    private final int radius3;

    private int dx = 0;
    private int dy = 0;
    private int dz = 0;

    public Radius3D(World world, Vector3i offset, int radius) {
        this(world, world, offset, radius);
    }

    public Radius3D(World world, BlockVolume volume, int radius) {
        this(world, volume, Vector3i.ZERO, radius);
    }

    public Radius3D(World world, BlockVolume volume, Vector3i offset, int radius) {
        this.world = new WeakReference<>(world);
        this.volume = volume;
        this.offset = offset;
        this.radius = radius;
        this.radius3 = radius * radius;
    }

    @Override
    public void reset() {
        dx = 0;
        dy = 0;
        dz = 0;
    }

    @Override
    public Operation.Phase iterate(int limit, Visitor3D visitor) {
        World world = this.world.get();
        if (world == null) {
            return Operation.Phase.ERROR;
        }

        while (dy <= radius) {
            while (dz <= radius) {
                while (dx <= radius) {
                    if ((dx * dx) + (dy * dy) + (dz * dz) < radius3) {
                        limit -= visitor.visit(world, volume, dx, dy, dz);

                        if (dx != 0) {
                            limit -= visit(visitor, world, -dx, dy, dz);
                        }
                        if (dy != 0) {
                            limit -= visit(visitor, world, dx, -dy, dz);
                        }
                        if (dz != 0) {
                            limit -= visit(visitor, world, dx, dy, -dz);
                        }
                        if (dx != 0 && dy != 0) {
                            limit -= visit(visitor, world, -dx, -dy, dz);
                        }
                        if (dx != 0 && dz != 0) {
                            limit -= visit(visitor, world, -dx, dy, -dz);
                        }
                        if (dz != 0 && dy != 0) {
                            limit -= visit(visitor, world, dx, -dy, -dz);
                        }
                        if (dz != 0 && dy != 0 && dz != 0) {
                            limit -= visit(visitor, world, -dx, -dy, -dz);
                        }
                    }
                    dx++;
                    if (limit <= 0) {
                        return Operation.Phase.CALCULATE;
                    }
                }
                dz++;
                dx = 0;
            }
            dy++;
            dz = 0;
        }
        return Operation.Phase.TEST;
    }

    private int visit(Visitor3D visitor, World world, int dx, int dy, int dz) {
        return visitor.visit(world, volume, offset.getX() + dx, offset.getY() + dy, offset.getZ() + dz);
    }
}
