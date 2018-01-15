package me.dags.copy.brush.stencil;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.util.Unserializable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.BitSet;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Stencil implements Unserializable {

    static final Stencil EMPTY = new Stencil(new BitSet(0), 0, 0);

    private final BitSet pixels;
    private final Vector3i offset;
    private final int width;
    private final int height;

    private Stencil(BitSet pixels, int width, int height) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.offset = new Vector3i(-width / 2, 0, -height / 2);
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public Vector3i getOffset() {
        return offset;
    }

    public Vector3i getMin() {
        return Vector3i.ZERO;
    }

    public Vector3i getMax() {
        return new Vector3i(width, 0, height);
    }

    public boolean contains(int x, int y, int z) {
        int index = (z * width) + x;
        return index > -1 && index < pixels.size() && pixels.get(index);
    }

    public static void write(Stencil stencil, OutputStream outputStream) throws IOException {
        try (DataOutputStream out = new DataOutputStream(outputStream)) {
            byte[] pixels = stencil.pixels.toByteArray();
            out.writeInt(stencil.width);
            out.writeInt(stencil.height);
            out.writeInt(pixels.length);
            out.write(pixels);
        }
    }

    public static Stencil read(InputStream inputStream) throws IOException {
        try (DataInputStream in = new DataInputStream(inputStream)) {
            int width = in.readInt();
            int height = in.readInt();
            int len = in.readInt();
            byte[] pixels = new byte[len];
            in.readFully(pixels);
            return new Stencil(BitSet.valueOf(pixels), width, height);
        }
    }

    public static Optional<Stencil> fromUrl(String url, int samples, float threshold) {
        try {
            return Optional.of(readStencil(read(url), samples, threshold));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Stencil readStencil(BufferedImage image, int samples, float threshold) {
        int width = image.getWidth() / samples;
        int height = image.getHeight() / samples;
        BitSet pixels = new BitSet(width * height);

        for (int y = 0; y < width; y++) {
            for (int x = 0; x < width; x++) {
                float darkness = 0;
                float count = 0;

                for (int dy = 0; dy < samples; dy++) {
                    int yPos = (y * samples) + dy;
                    for (int dx = 0; dx < samples; dx++) {
                        int xPos = (x * samples) + dx;
                        if (xPos < image.getWidth() && yPos < image.getHeight()) {
                            int rgb = image.getRGB(xPos, yPos);
                            darkness += getDarkness(rgb);
                            count++;
                        }
                    }
                }

                if (darkness / count >= threshold) {
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
