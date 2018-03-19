package me.dags.copy.brush.line.iterator;

import com.flowpowered.math.vector.Vector3i;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class BezierPath implements LineIterator {

    private final List<Vector3i> path;

    private int pos = 0;
    private LineIterator iterator = null;
    private Vector3i position = Vector3i.ZERO;

    public BezierPath(List<Vector3i> path) {
        this.path = path;
    }

    @Override
    public void reset() {
        pos = 0;
        iterator = null;
        position = Vector3i.ZERO;
    }

    @Override
    public boolean hasNext() {
        if (iterator == null) {
            if (pos + 2 >= path.size()) {
                return false;
            }

            Vector3i p0 = path.get(pos);
            Vector3i p1 = path.get(pos + 1);
            Vector3i p2 = path.get(pos + 2);
            pos += 2;

            iterator = new Bezier(p0, p1, p2);
        }

        boolean hasNext = iterator.hasNext();
        position = iterator.nextPosition();

        if (!hasNext) {
            iterator = null;
            hasNext = pos < path.size();
        }

        return hasNext;
    }

    @Override
    public Vector3i nextPosition() {
        return position;
    }
}
