package me.dags.copy.operation.calculator;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.operation.Operation;
import me.dags.copy.operation.visitor.Visitor2D;
import me.dags.copy.operation.visitor.Visitor3D;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;

import java.lang.ref.WeakReference;

/**
 * @author dags <dags@dags.me>
 */
public class Volume implements Calculator {

    private final WeakReference<World> world;
    private final BlockVolume volume;
    private final Vector3i min;
    private final Vector3i max;
    private int dx = 0;
    private int dy = 0;
    private int dz = 0;

    public Volume(World world, BlockVolume volume) {
        this.world = new WeakReference<>(world);
        this.min = volume.getBlockMin();
        this.max = volume.getBlockMax();
        this.volume = volume;
        reset();
    }

    @Override
    public void reset() {
        dx = min.getX();
        dy = min.getY();
        dz = min.getZ();
    }

    @Override
    public Operation.Phase iterate(int limit, Visitor2D visitor) {
        return Operation.Phase.TEST;
    }

    @Override
    public Operation.Phase iterate(int limit, Visitor3D visitor) {
        World world = this.world.get();
        if (world == null) {
            return Operation.Phase.ERROR;
        }

        while (dy <= max.getY()) {
            while (dz <= max.getZ()) {
                while (dx <= max.getX()) {
                    limit -= visitor.visit(world, volume, dx, dy, dz);
                    dx++;

                    if (--limit <= 0) {
                        return Operation.Phase.CALCULATE;
                    }
                }
                dx = min.getX();
                dz++;
            }
            dz = min.getZ();
            dy++;
        }
        return Operation.Phase.TEST;
    }
}
