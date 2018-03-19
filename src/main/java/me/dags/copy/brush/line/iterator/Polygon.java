package me.dags.copy.brush.line.iterator;

import com.flowpowered.math.vector.Vector3i;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Polygon extends Path {

    public Polygon(Vector3i center, Vector3i point1, int sides) {
        super(getCorners(center, point1, sides), true);
    }

    private static List<Vector3i> getCorners(Vector3i center, Vector3i p1, int sides) {
        List<Vector3i> corners = new LinkedList<>();
        Vector3i dist = p1.sub(center);

        float radius = LineIterator.dist(dist.getX(), dist.getY(), dist.getZ());
        float startRads = (float) Math.asin(dist.getX() / radius);
        float segmentRads = 2F / sides;

        startRads -= 0.5F; // translates to 12 o'clock rather than 3

        for (int i = 0; i < sides; i++) {
            double rads = (startRads + (i * segmentRads)) * Math.PI;
            double dx = radius * Math.cos(rads);
            double dz = radius * Math.sin(rads);
            corners.add(center.add(dx, 0, dz));
        }

        return corners;
    }
}
