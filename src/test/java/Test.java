import com.flowpowered.math.vector.Vector3i;
import java.awt.Color;
import me.dags.copy.brush.line.iterator.Bezier;
import me.dags.copy.brush.line.iterator.LineIterator;

/**
 * @author dags <dags@dags.me>
 */
public class Test {

    public static void main(String[] args) {
        Viewer viewer = new Viewer(512, 512);
        viewer.setRenderer((buffer, xOff, zOff) -> {
            int x = 256 + xOff;
            int z = 256 + zOff;
            LineIterator iterator;

            Viewer.clear(buffer);

//            Random random = new Random();
//            List<Vector3i> points = new LinkedList<>();
//            for (int i = 0; i < 25; i++) {
//                int px = random.nextInt(512);
//                int pz = random.nextInt(512);
//                points.add(new Vector3i(px, 0, pz));
//            }
//            iterator = new BezierPath(points);

            iterator = new Bezier(
                    new Vector3i(64, 0, 64),
                    new Vector3i(x, 0, z),
                    new Vector3i(buffer.getWidth() - 64, 0, buffer.getHeight() - 64)
            );

            while (iterator.hasNext()) {
                Vector3i pos = iterator.nextPosition();
                if (pos.getX() < 0 || pos.getX() >= buffer.getWidth()) {
                    continue;
                }
                if (pos.getZ() < 0 || pos.getZ() >= buffer.getHeight()) {
                    continue;
                }
                buffer.setRGB(pos.getX(), pos.getZ(), Color.RED.getRGB());
            }

            if (x >= 0 && x < buffer.getWidth() && z >= 0 && z < buffer.getHeight()) {
                buffer.setRGB(x, z, Color.WHITE.getRGB());
            }
        });
    }
}
