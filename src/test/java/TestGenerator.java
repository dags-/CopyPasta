import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class TestGenerator {

    public static void main(String[] args) {
        int count = 16;
        int min = 8;
        int max = 255;
        int range = max - min;
        int alpha = (range / count);
        File dir = new File("textures");
        dir.mkdirs();

        for (int i = 0; i < count; i++) {
            generate(dir, String.format("cloud_white_%s", i), 16, (i * alpha) + min);
        }
    }

    private static void generate(File parent, String name, int size, int alpha) {
        Color color = new Color(255, 255, 255, alpha);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                image.setRGB(x, y, color.getRGB());
            }
        }

        File out = new File(parent, name + ".png");
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkerboard(File parent, String name, int size, int alpha) {
        Color color = new Color(255, 255, 255, alpha);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        int res = 2;

        for (int y = 0; y < size; y += res) {
            int xOff = (y & res);
            for (int x = xOff; x < size; x += res) {
                if ((x & res) == xOff) {
                    for (int dy = 0; dy < res; dy++) {
                        for (int dx = 0; dx < res; dx++) {
                            int px = x + dx;
                            int py = y + dy;
                            image.setRGB(px, py, color.getRGB());
                        }
                    }
                }
            }
        }

        File out = new File(parent, name + ".png");
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
