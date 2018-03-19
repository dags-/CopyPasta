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
            int inset = 64;

            Viewer.clear(buffer);

            LineIterator iterator = new Bezier(
                    new Vector3i(inset, 0, inset),
                    new Vector3i(x, 0, z),
                    new Vector3i(buffer.getWidth() - inset, 0, buffer.getHeight() - inset)
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
