import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import me.dags.copy.block.volume.Buffer;
import me.dags.copy.brush.cloud.CloudBrush;
import me.dags.copy.brush.cloud.ValueNoise;
import me.dags.copy.brush.option.OptionHolder;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Cloud2<T, V> {

    private final int seed;
    private final int octaves;
    private final double frequency;
    private final double max;
    private final int radius;
    private final int radius2;
    private final float featherDist2;
    private final float featherStart2;
    private final float opacity;
    private final int materialAir;
    private final int materialRange;
    private final float scaleFrequency;
    private final int heightRange;
    private final float center;
    private final float rotation;
    private final Vector2f facing;

    private final List<T> materials;
    private final Buffer.Factory<T, V> factory;

    public Cloud2(OptionHolder brush, Collection<T> materials, Vector2f facing, Buffer.Factory<T, V> factory) {
        this.seed = brush.mustOption(CloudBrush.SEED);

        this.frequency = brush.mustOption(TestBrush.FREQUENCY) * 0.1D;
        this.octaves = brush.mustOption(TestBrush.OCTAVES);
        this.max = ValueNoise.maxValue(octaves);

        this.radius = brush.mustOption(TestBrush.RADIUS);
        this.radius2 = radius * radius;

        float feather = brush.mustOption(TestBrush.FEATHER);
        this.featherDist2 = (radius * feather) * (radius * feather);
        this.featherStart2 = radius2 - featherDist2;

        this.opacity = brush.mustOption(TestBrush.OPACITY);

        float density = brush.mustOption(TestBrush.DENSITY);
        this.materialAir = Math.round(materials.size() * (1 - density));
        this.materialRange = materials.size() - 1 + materialAir;

        float scale = brush.mustOption(TestBrush.SCALE);
        this.scaleFrequency = (1 - scale) * 0.2F;

        int height = brush.mustOption(TestBrush.HEIGHT);
        this.heightRange = (2 * height) + 1;

        this.center = brush.mustOption(TestBrush.CENTER);
        this.rotation = brush.mustOption(TestBrush.ROTATION);
        this.facing = facing;

        this.materials = ImmutableList.copyOf(materials);
        this.factory = factory;
    }

    public Runnable createTask(UUID owner, Vector3i pos, FutureCallback<Buffer.View<V>> callback) {
        return () -> {
            try {
                Buffer.View<V> volume = apply(pos, owner);
                callback.onSuccess(volume);
            } catch (Throwable t) {
                callback.onFailure(t);
            }
        };
    }

    public Buffer.View<V> apply(Vector3i pos, UUID owner) {
        int diameter = 1 + radius * 2;
        int volume = diameter * heightRange * diameter;
        Buffer<T, V> buffer = factory.create(owner, Vector3i.ZERO, volume);

        for (int dz = 0; dz <= radius; dz++) {
            for (int dx = 0; dx <= radius; dx++) {
                int dist2 = (dx * dx) + (dz * dz);
                if (dist2 < radius2) {
                    apply(buffer, pos, dx, dz, dist2);
                    if (dx != 0) {
                        apply(buffer, pos, -dx, dz, dist2);
                    }
                    if (dz != 0) {
                        apply(buffer, pos, dx, -dz, dist2);
                    }
                    if (dx != 0 && dz != 0) {
                        apply(buffer, pos, -dx, -dz, dist2);
                    }
                }
            }
        }

        return buffer.getView();
    }

    private void apply(Buffer<T, V> buffer, Vector3i pos, int dx, int dz, float distance2) {
        int x = pos.getX() + dx;
        int y = pos.getY();
        int z = pos.getZ() + dz;

        // amount of vertical offset based on distance from center, rotation amount, and facing x,z ratio
        int rx = Math.round(dx * rotation * facing.getX());
        int rz = Math.round(dz * rotation * facing.getY());

        // height based on perlin(x,z) modified by distance from center & getFeatherMod amount
        double hMod = modifier(distance2, radius2);
        float feather = getFeatherModifier(distance2);
        float elevation = getElevation(x, z, hMod * feather);

        int minY = -Math.max(1, Math.round(elevation * center));
        int maxY = Math.round(minY + elevation);
        for (int dy = minY; dy <= maxY; dy++) {
            T material = getMaterial(x, dy, z, minY, maxY, feather);
            buffer.addAbsolute(material, x, y + dy + rx + rz, z);
        }
    }

    private float getElevation(double x, double z, double mod) {
        double noise = ValueNoise.getValue(x, 0, z, seed, frequency, octaves) / max;
        return (float) (heightRange * noise * mod);
    }

    private float getFeatherModifier(float dist2) {
        if (dist2 > featherStart2) {
            return 1F - (dist2 - featherStart2) / featherDist2;
        }
        return 1F;
    }

    private T getMaterial(double x, double y, double z, double lower, double upper, double feather) {
        double mod = modifier(y, y < 0.5 ? lower : upper);
        double noise = ValueNoise.getValue(x, y, z, seed, scaleFrequency, octaves) / max;
        double value = (materialRange * noise * feather * mod) - materialAir;
        int index = Math.min(materialRange, Math.max(0, (int) Math.round(opacity * value)));
        return materials.get(index);
    }

    private static double modifier(double value, double range) {
        double modifier = value / range;
        return 1D - (modifier * modifier);
    }
}
