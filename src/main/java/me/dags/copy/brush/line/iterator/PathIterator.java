package me.dags.copy.brush.line.iterator;

import com.flowpowered.math.vector.Vector3i;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class PathIterator implements PositionIterator {

    private final List<Vector3i> path;

    private int pos = 0;
    private LineIterator iterator = null;
    private Vector3i position = Vector3i.ZERO;

    public PathIterator(List<Vector3i> path) {
        this.path = path;
    }

    public boolean close() {
        return false;
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
            if (pos >= path.size()) {
                return false;
            }

            Vector3i from = path.get(pos++);
            Vector3i to;

            if (pos < path.size()) {
                to = path.get(pos);
            } else if (close()) {
                to = path.get(0);
            } else {
                return false;
            }

            iterator = new LineIterator(from, to);
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
