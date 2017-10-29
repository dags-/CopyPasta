package me.dags.copy.operation.calculator;

import me.dags.copy.operation.Operation;
import me.dags.copy.operation.visitor.Visitor2D;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class Radius2D implements Calculator {

    private final World volume;
    private final int radius2;
    private final int radius;
    private int dx = 0;
    private int dz = 0;

    public Radius2D(World volume, int radius) {
        this.volume = volume;
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
        while (dz <= radius) {
            while (dx <= radius) {
                if ((dx * dx) + (dz * dz) < radius2) {
                    limit -= visitor.visit(volume, dx, dz);

                    if (dx != 0) {
                        limit -= visitor.visit(volume, -dx, dz);
                    }
                    if (dz != 0) {
                        limit -= visitor.visit(volume, dx, -dz);
                    }
                    if (dx != 0 && dz != 0) {
                        limit -= visitor.visit(volume, -dx, -dz);
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
}
