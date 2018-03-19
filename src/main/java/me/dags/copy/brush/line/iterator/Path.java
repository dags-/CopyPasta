package me.dags.copy.brush.line.iterator;

import com.flowpowered.math.vector.Vector3i;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Path implements LineIterator {

    private final boolean closed;
    private final List<Vector3i> path;

    private int pos = 0;
    private LineIterator iterator = null;
    private Vector3i position = Vector3i.ZERO;

    public Path(List<Vector3i> path, boolean closed) {
        this.path = path;
        this.closed = closed;
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
            } else if (closed) {
                to = path.get(0);
            } else {
                return false;
            }

            iterator = new Line(from, to);
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
