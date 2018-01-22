import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.block.volume.Buffer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author dags <dags@dags.me>
 */
public class Main {

    public static void main(String[] args) {
        TestBrush brush = new TestBrush();
        JPanel view = new Preview(brush);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(slider("radius", 8, 128, 50, v -> brush.setOption(TestBrush.RADIUS, v)));
        controls.add(slider("height", 4, 64, 16, v -> brush.setOption(TestBrush.HEIGHT, v)));
        controls.add(slider("center", 0, 100, 30, 100F, v -> brush.setOption(TestBrush.CENTER, v)));
        controls.add(slider("freq", 2, 100, 30, 100, v -> brush.setOption(TestBrush.FREQUENCY, v)));
        controls.add(slider("scale", 2, 100, 50, 100, v -> brush.setOption(TestBrush.SCALE, v)));
        controls.add(slider("octaves", 1, 8, 4, v -> brush.setOption(TestBrush.OCTAVES, v)));
        controls.add(slider("feather", 0, 100, 50, 100F, v -> brush.setOption(TestBrush.FEATHER, v)));
        controls.add(slider("opacity", 0, 100, 90, 100F, v -> brush.setOption(TestBrush.OPACITY, v)));
        controls.add(slider("density", 0, 100, 80, 100F, v -> brush.setOption(TestBrush.DENSITY, v)));
        controls.add(slider("rotation", -100, 100, 0, 100F, v -> brush.setOption(TestBrush.ROTATION, v)));

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.add(view, BorderLayout.CENTER);
        layout.add(controls, BorderLayout.PAGE_END);

        JFrame frame = new JFrame();
        frame.add(layout);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static class Preview extends JPanel {

        private final TestBrush brush;
        private final List<Color> materials;
        private final Buffer.Factory<Color, State> factory = (owner, pos, size) -> new Buffer<Color, State>() {

            private final List<State> colors = new LinkedList<>();

            @Override
            public void addRelative(Color state, int x, int y, int z) {
                colors.add(new State(state, x - pos.getX(), y - pos.getY(), z - pos.getZ()));
            }

            @Override
            public void addAbsolute(Color color, int x, int y, int z) {
                colors.add(new State(color, x, y, z));
            }

            @Override
            public View<State> getView() {
                return colors::iterator;
            }
        };

        private final Color sky = new Color(90, 175, 220);
        private final BufferedImage plan = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        private final BufferedImage section = new BufferedImage(128, 64, BufferedImage.TYPE_INT_ARGB);

        private float xOff = 0, zOff = 0;
        private int x = 0, z = 0;

        private Preview(TestBrush brush) {
            this.brush = brush;
            int max = 16;
            float fact = 255F / max;
            this.materials = IntStream.range(0, max).boxed()
                    .map(i -> Math.round(fact * i))
                    .map(i -> new Color(255, 255, 255, i))
                    .collect(Collectors.toList());

            this.setPreferredSize(new Dimension(256, 384));
        }

        @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            clear();

            Buffer.View<State> view = brush.perform(new Vector3i(Math.round(64 + x), 32, Math.round(64 + z)), factory, materials);
            for (State state : view) {
                record(state);
            }

            graphics.drawImage(plan, 0, 0, 256, 256, this);
            graphics.setColor(Color.white);
            graphics.fillRect(0, 256, 256, 10);
            graphics.drawImage(section, 0, 256 + 10, 256, 128, this);

            repaint();

//            zOff -= 0.25;
            x = Math.round(xOff);
            z = Math.round(zOff);
        }

        private void clear() {
            clear(plan, sky);
            clear(section, sky);
        }

        private void clear(BufferedImage img, Color color) {
            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    img.setRGB(x, y, color.getRGB());
                }
            }
        }

        private void record(State state) {
            int x = state.x - this.x;
            int z = state.z - this.z;

            if (state.y == 32 && x >= 0 && x < plan.getWidth() && z >= 0 && z < plan.getHeight()) {
                int pixel = plan.getRGB(x, z);
                Color color = blend(state.color, new Color(pixel));
                plan.setRGB(x, z, color.getRGB());
            }

            int y = (section.getHeight() - state.y);
            if (z == 64 && x >= 0 && x < section.getWidth() && y >= 0 && y < section.getHeight()) {
                int pixel = section.getRGB(x, y);
                Color color = blend(state.color, new Color(pixel));
                section.setRGB(x, y, color.getRGB());
            }
        }

        private Color blend(Color over, Color under) {
            float oa = over.getAlpha() / 255F;
            float ua = 1 - oa;
            int red = Math.round((under.getRed() * ua) + (over.getRed() * oa));
            int green = Math.round((under.getGreen() * ua) + (over.getGreen() * oa));
            int blue = Math.round((under.getBlue() * ua) + (over.getBlue() * oa));
            return new Color(red, green, blue);
        }
    }

    private static JPanel slider(String name, int min, int max, int def, Consumer<Integer> consumer) {
        JSlider slider = new JSlider(new DefaultBoundedRangeModel(def, 1, min, max));
        slider.setPreferredSize(new Dimension(164, 20));
        slider.addChangeListener(e -> consumer.accept(slider.getValue()));

        JLabel label = new JLabel(name);
        label.setPreferredSize(new Dimension(64, 20));

        JPanel wrapper = new JPanel();
        wrapper.add(label);
        wrapper.add(slider);
        return wrapper;
    }

    private static JPanel slider(String name, int min, int max, int def, float order, Consumer<Float> consumer) {
        JSlider slider = new JSlider(new DefaultBoundedRangeModel(def, 1, min, max));
        slider.setPreferredSize(new Dimension(164, 20));
        slider.addChangeListener(e -> {
            float value = slider.getValue() / order;
            consumer.accept(value);
        });
        JLabel label = new JLabel(name);
        label.setPreferredSize(new Dimension(64, 20));

        JPanel wrapper = new JPanel();
        wrapper.add(label);
        wrapper.add(slider);
        return wrapper;
    }

    private static class State {

        private final Color color;
        private final int x, y, z;

        private State(Color color, int x, int y, int z) {
            this.color = color;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
