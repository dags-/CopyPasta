package me.dags.copy.brush.line.iterator;

import com.flowpowered.math.vector.Vector3i;

/**
 * @author dags <dags@dags.me>
 */
public interface LineIterator {

    void reset();

    boolean hasNext();

    Vector3i nextPosition();

    static float dist(float x, float y, float z) {
        return (float) Math.sqrt(dist2(x, y, z));
    }

    static float dist2(float x, float y, float z) {
        return (x * x) + (y * y) + (z * z);
    }

    LineIterator EMPTY = new LineIterator() {

        @Override
        public void reset() {

        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Vector3i nextPosition() {
            return Vector3i.ZERO;
        }
    };
}
