package me.dags.copy.brush.line.iterator;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;

/**
 * @author dags <dags@dags.me>
 */
public class Line implements LineIterator {

    private final Vector3i from;
    private final Vector3i to;
    private final Vector3f grad;

    private int pos = 0;
    private int x;
    private int y;
    private int z;

    public Line(Vector3i from, Vector3i to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        float distance = LineIterator.dist(dx, dy, dz);
        this.to = to;
        this.from = from;
        this.x = from.getX();
        this.y = from.getY();
        this.z = from.getZ();
        this.grad = new Vector3f(dx / distance, dy / distance, dz / distance);
    }

    @Override
    public void reset() {
        pos = 0;
        x = from.getX();
        y = from.getY();
        z = from.getZ();
    }

    @Override
    public Vector3i nextPosition() {
        return new Vector3i(x, y, z);
    }

    @Override
    public boolean hasNext() {
        int cx = x, cy = y, cz = z;
        float dx, dy, dz;
        while (x == cx && y == cy && z == cz) {
            dx = pos * grad.getX();
            dy = pos * grad.getY();
            dz = pos * grad.getZ();

            float fx = from.getX() + dx;
            float fy = from.getY() + dy;
            float fz = from.getZ() + dz;

            x = (int) fx;
            y = (int) fy;
            z = (int) fz;

            pos++;
        }

        return !reachedEnd();
    }

    private boolean reachedEnd() {
        boolean end;
        end = ((grad.getX() <= 0 && x <= to.getX()) || (grad.getX() > 0 && x >= to.getX()));
        end = end && ((grad.getY() <= 0 && y <= to.getY()) || (grad.getY() > 0 && y >= to.getY()));
        end = end && ((grad.getZ() <= 0 && z <= to.getZ()) || (grad.getZ() > 0 && z >= to.getZ()));
        return end;
    }
}
