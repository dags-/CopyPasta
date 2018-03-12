import com.flowpowered.math.vector.Vector3i;
import java.awt.Color;
import me.dags.copy.brush.line.iterator.PolygonIterator;

/**
 * @author dags <dags@dags.me>
 */
public class Test {

    private static int sides = 3;
    private static int lastX = 0;

    public static void main(String[] args) {
        Viewer viewer = new Viewer(512, 512);
        viewer.setRenderer((buffer, xOff, zOff) -> {
            int direction = xOff > lastX ? 1 : -1;
            lastX = xOff;

            Viewer.clear(buffer);
            Vector3i center = new Vector3i(256, 0, 256);
            Vector3i end = new Vector3i(256, 0, 50);

            PolygonIterator polygon = new PolygonIterator(center, end, sides = Math.max(2, sides + direction));
            while (polygon.hasNext()) {
                Vector3i pos = polygon.nextPosition();
                if (pos.getX() < 0 || pos.getX() >= buffer.getWidth()) {
                    continue;
                }
                if (pos.getZ() < 0 || pos.getZ() >= buffer.getHeight()) {
                    continue;
                }
                buffer.setRGB(pos.getX(), pos.getZ(), Color.RED.getRGB());
            }
        });
    }
}
