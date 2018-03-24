package me.dags.copy.brush.line.iterator;

import com.flowpowered.math.GenericMath;
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

        // the increment value needs to be tuned so that it is small enough that successive points are always
        // close together, but that we don't spend too much time gradually incrementing to get there.
        // if the line were straight, inc would be something like: 1 / (dist(p1-p0) + dist(p2-p1))
        // to account for curvature we use some 'magic' value to increase the distance a little, and decrease the increment
        float magic = 2.5F;
        this.inc = 1F / ((p0.distance(p1) + p1.distance(p2)) * magic);
    }

    @Override
    public void reset() {
        t = 0F;
    }

    @Override
    public boolean hasNext() {
        int cx = x, cy = y, cz = z;
        while (x == cx && y == cy && z == cz) {
            // Bezier Quadratic Curve: ((1-t)^2)p0 + (2(1-t)t)p1 + (t^2)p2

            float t0 = (1 - t) * (1 - t); // (1-t)^2
            float t1 = 2 * (1 - t) * t;   // 2(1-t)t
            float t2 = t * t;             // t^2

            float px = (p0.getX() * t0) + (p1.getX() * t1) + (p2.getX() * t2);
            float py = (p0.getY() * t0) + (p1.getY() * t1) + (p2.getY() * t2);
            float pz = (p0.getZ() * t0) + (p1.getZ() * t1) + (p2.getZ() * t2);

            x = GenericMath.floor(px);
            y = GenericMath.floor(py);
            z = GenericMath.floor(pz);
            t += inc;
        }
        return t < 1F;
    }

    @Override
    public Vector3i nextPosition() {
        return new Vector3i(x, y, z);
    }
}
