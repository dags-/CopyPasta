package me.dags.copy.operation.calculator;

import me.dags.copy.operation.Operation;
import me.dags.copy.operation.visitor.Visitor3D;
import org.spongepowered.api.world.extent.BlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public class Radius3D implements Calculator {

    private final BlockVolume volume;
    private final int radius;
    private final int radius3;

    private int dx = 0;
    private int dy = 0;
    private int dz = 0;

    public Radius3D(BlockVolume volume, int radius) {
        this.volume = volume;
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
        while (dy <= radius) {
            while (dz <= radius) {
                while (dx <= radius) {
                    if ((dx * dx) + (dy * dy) + (dz * dz) < radius3) {
                        limit -= visitor.visit(volume, dx, dy, dz);
                        if (dx != 0) {
                            limit -= visitor.visit(volume, -dx, dy, dz);
                        }
                        if (dy != 0) {
                            limit -= visitor.visit(volume, dx, -dy, dz);
                        }
                        if (dz != 0) {
                            limit -= visitor.visit(volume, dx, dy, -dz);
                        }
                        if (dx != 0 && dy != 0) {
                            limit -= visitor.visit(volume, -dx, -dy, dz);
                        }
                        if (dx != 0 && dz != 0) {
                            limit -= visitor.visit(volume, -dx, dy, -dz);
                        }
                        if (dz != 0 && dy != 0) {
                            limit -= visitor.visit(volume, dx, -dy, -dz);
                        }
                        if (dz != 0 && dy != 0 && dz != 0) {
                            limit -= visitor.visit(volume, -dx, -dy, -dz);
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
}
