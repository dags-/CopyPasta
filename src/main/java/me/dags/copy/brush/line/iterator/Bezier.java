package me.dags.copy.brush.line.iterator;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;

/**
 * @author dags <dags@dags.me>
 */
public class Bezier implements LineIterator {

    private final Vector3f p0;
    private final Vector3f p1;
    private final Vector3f p2;
    private final float inc;

    private float t = 0F;
    private int x = 0;
    private int y = 0;
    private int z = 0;

    public Bezier(Vector3i p0, Vector3i p1, Vector3i p2) {
        this.p0 = p0.toFloat();
        this.p1 = p1.toFloat();
        this.p2 = p2.toFloat();
        this.inc = 1F / (p0.distance(p1) * p1.distance(p2));
    }

    @Override
    public void reset() {
        t = 0;
    }

    @Override
    public boolean hasNext() {
        int cx = x, cy = y, cz = z;
        while (t <= 1F && x == cx && y == cy && z == cz) {
            Vector3f v = p0.mul(t).add(p1.mul(2 * t * (1 - t))).add(p2.mul(Math.pow(1 - t, 2)));
            x = v.getFloorX();
            y = v.getFloorY();
            z = v.getFloorZ();
            t += inc;
        }
        return t < 1F;
    }

    @Override
    public Vector3i nextPosition() {
        return new Vector3i(x, y, z);
    }
}
