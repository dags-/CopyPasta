package me.dags.copy.brush.line;

import com.flowpowered.math.vector.Vector3i;
import java.util.List;
import me.dags.config.Node;
import me.dags.copy.brush.line.iterator.Bezier;
import me.dags.copy.brush.line.iterator.BezierPath;
import me.dags.copy.brush.line.iterator.LineIterator;
import me.dags.copy.brush.line.iterator.Path;
import me.dags.copy.brush.line.iterator.Polygon;

/**
 * @author dags <dags@dags.me>
 */
public enum Line implements Node.Value<Line> {
    LINE(2),
    BEZIER(3),
    POLYGON(2),
    ;

    public final int points;

    Line(int minPoints) {
        this.points = minPoints;
    }

    @Override
    public void toNode(Node node) {
        node.set(toString().toLowerCase());
    }

    @Override
    public Line fromNode(Node node) {
        String name = node.get("");
        if (name.isEmpty()) {
            return LINE;
        }
        return valueOf(name.toUpperCase());
    }

    public LineIterator newIterator(List<Vector3i> points, int sides, boolean closed) {
        if (points.size() < this.points) {
            return LineIterator.EMPTY;
        }

        if (this == LINE) {
            if (points.size() > this.points) {
                return new Path(points, closed);
            }
            Vector3i p0 = points.get(0);
            Vector3i p1 = points.get(1);
            return new me.dags.copy.brush.line.iterator.Line(p0, p1);
        }

        if (this == BEZIER) {
            if (points.size() > this.points) {
                return new BezierPath(points);
            }
            Vector3i p0 = points.get(0);
            Vector3i p1 = points.get(1);
            Vector3i p2 = points.get(2);
            return new Bezier(p0, p1, p2);
        }

        if (this == POLYGON) {
            Vector3i p0 = points.get(0);
            Vector3i p1 = points.get(1);
            return new Polygon(p0, p1, sides);
        }

        return LineIterator.EMPTY;
    }
}
