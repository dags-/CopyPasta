package me.dags.copy.brush.stencil;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.block.VolumeVisitor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Stencil {

    static final Stencil EMPTY = new Stencil(new BitSet(0), 0, 0);

    private final BitSet pixels;
    private final int width;
    private final int height;

    private Stencil(BitSet pixels, int width, int height) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
    }

    private Stencil(byte[] data, int width, int height) {
        this(BitSet.valueOf(data), width, height);
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public void iterate(VolumeVisitor visitor) {
        int xOff = width / 2;
        int zOff = height / 2;
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                int index = (z * width) + x;
                if (pixels.get(index)) {
                    visitor.visit(x - xOff, 0, z - zOff);
                }
            }
        }
    }

    public List<Vector3i> getPoints(Vector3i center) {
        List<Vector3i> pixels = new LinkedList<>();
        iterate((x, y, z) -> pixels.add(center.add(x, y, z)));
        return pixels;
    }

    public static Optional<Stencil> fromUrl(String url, int scale, float min) {
        try {
            return Optional.of(readStencil(read(url), scale, min));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Stencil readStencil(BufferedImage image, int scale, float min) {
        int width = image.getWidth() / scale;
        int height = image.getHeight() / scale;
        BitSet pixels = new BitSet(width * height);

        for (int y = 0; y < width; y++) {
            for (int x = 0; x < width; x++) {
                float darkness = 0;
                float count = 0;

                for (int dy = 0; dy < scale; dy++) {
                    int yPos = (y * scale) + dy;
                    for (int dx = 0; dx < scale; dx++) {
                        int xPos = (x * scale) + dx;
                        if (xPos < image.getWidth() && yPos < image.getHeight()) {
                            int rgb = image.getRGB(xPos, yPos);
                            darkness += getDarkness(rgb);
                            count++;
                        }
                    }
                }

                float avg = darkness / count;
                if (avg > min) {
                    int index = (width * y) + x;
                    pixels.set(index, true);
                }
            }
        }

        return new Stencil(pixels, width, height);
    }

    private static BufferedImage read(String url) throws IOException {
        return ImageIO.read(new URL(url));
    }

    private static float getDarkness(int color) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        float avg = ((r + g + b) / 3F) / 255F;
        return 1 - avg;
    }

    private static int rgb(int r, int g, int b) {
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;
        return rgb;
    }
}
