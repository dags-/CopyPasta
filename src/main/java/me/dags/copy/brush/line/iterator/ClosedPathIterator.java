package me.dags.copy.brush.line.iterator;

import com.flowpowered.math.vector.Vector3i;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class ClosedPathIterator extends PathIterator {

    public ClosedPathIterator(List<Vector3i> path) {
        super(path);
    }

    public boolean closed() {
        return true;
    }
}
