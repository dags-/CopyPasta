import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.block.volume.Buffer;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.OptionHolder;
import me.dags.copy.brush.option.Options;

import java.util.Collection;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class TestBrush implements OptionHolder {

    public static final Option<Integer> SEED = Option.of("seed", 4);
    public static final Option<Float> FREQUENCY = Option.of("frequency", 0.25F);
    public static final Option<Integer> OCTAVES = Option.of("octaves", 4);
    public static final Option<Integer> RADIUS = Option.of("radius", 45);
    public static final Option<Float> FEATHER = Option.of("feather", 0.5F);
    public static final Option<Float> OPACITY = Option.of("opacity", 0.9F);
    public static final Option<Float> DENSITY = Option.of("density", 0.8F);
    public static final Option<Float> SCALE = Option.of("scale", 0.5F);
    public static final Option<Integer> HEIGHT = Option.of("height", 16);
    public static final Option<Float> CENTER = Option.of("center", 0.25F);
    public static final Option<Float> ROTATION = Option.of("rotation", 0F);

    public <T, V> Buffer.View<V> perform(Vector3i pos, Buffer.Factory<T, V> factory, Collection<T> materials) {
        Cloud2<T, V> cloud2 = new Cloud2<>(this, materials, new Vector2f(0.5F, 0.5F), factory);
        UUID uuid = UUID.randomUUID();
        return cloud2.apply(pos, uuid);
    }

    private final Options options = new Options();

    @Override
    public Options getOptions() {
        return options;
    }
}
