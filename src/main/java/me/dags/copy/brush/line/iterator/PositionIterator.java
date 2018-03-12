package me.dags.copy.brush.line.iterator;

import com.flowpowered.math.vector.Vector3i;

/**
 * @author dags <dags@dags.me>
 */
public interface PositionIterator {

    void reset();

    boolean hasNext();

    Vector3i nextPosition();

    static float dist(float x, float y, float z) {
        return (float) Math.sqrt(dist2(x, y, z));
    }

    static float dist2(float x, float y, float z) {
        return (x * x) + (y * y) + (z * z);
    }
}
