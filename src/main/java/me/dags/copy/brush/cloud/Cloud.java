package me.dags.copy.brush.cloud;

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import java.util.UUID;
import me.dags.copy.block.volume.BufferBuilder;
import me.dags.copy.block.volume.BufferView;
import me.dags.copy.brush.option.OptionHolder;
import org.spongepowered.api.block.BlockState;

/**
 * @author dags <dags@dags.me>
 */
public class Cloud {

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
    private final float incline;
    private final Vector2f facing;

    private final List<BlockState> materials;

    public Cloud(OptionHolder brush, ImmutableList<BlockState> materials, Vector2f facing) {
        this.seed = brush.getOption(CloudBrush.SEED);

        this.frequency = brush.getOption(CloudBrush.FREQUENCY) * 0.1D;
        this.octaves = brush.getOption(CloudBrush.OCTAVES);
        this.max = ValueNoise.maxValue(octaves);

        this.radius = brush.getOption(CloudBrush.RADIUS);
        this.radius2 = radius * radius;

        float feather = brush.getOption(CloudBrush.FEATHER);
        this.featherDist2 = (radius * feather) * (radius * feather);
        this.featherStart2 = radius2 - featherDist2;

        this.opacity = brush.getOption(CloudBrush.OPACITY);

        float density = brush.getOption(CloudBrush.DENSITY);
        this.materialAir = Math.round(materials.size() * (1 - density));
        this.materialRange = materials.size() - 1 + materialAir;

        float scale = brush.getOption(CloudBrush.SCALE);
        this.scaleFrequency = (1 - scale) * 0.125F;

        int height = brush.getOption(CloudBrush.HEIGHT);
        this.heightRange = (2 * height) + 1;

        this.center = brush.getOption(CloudBrush.CENTER);
        this.incline = brush.getOption(CloudBrush.INCLINE);
        this.facing = facing;

        this.materials = materials;
    }

    public Runnable createTask(UUID owner, Vector3i pos, FutureCallback<BufferView> callback) {
        return () -> {
            try {
                BufferView volume = apply(pos, owner);
                callback.onSuccess(volume);
            } catch (Throwable t) {
                callback.onFailure(t);
            }
        };
    }

    public BufferView apply(Vector3i pos, UUID owner) {
        int diameter = 1 + radius * 2;
        int volume = diameter * heightRange * diameter;
        BufferBuilder buffer = new BufferBuilder(owner, Vector3i.ZERO, volume);

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

    private void apply(BufferBuilder buffer, Vector3i pos, int dx, int dz, float distance2) {
        int x = pos.getX() + dx;
        int y = pos.getY();
        int z = pos.getZ() + dz;

        // amount of vertical offset based on distance from center, incline, and facing x,z ratio
        int rx = Math.round(dx * incline * facing.getX());
        int rz = Math.round(dz * incline * facing.getY());

        // height based on perlin(x,z) modified by distance from center & getFeatherMod amount
        double hMod = modifier(distance2, radius2);
        float feather = getFeatherModifier(distance2);
        float elevation = getElevation(x, z, hMod * feather);

        int minY = -Math.max(1, Math.round(elevation * center));
        int maxY = Math.round(minY + elevation);
        for (int dy = minY; dy <= maxY; dy++) {
            BlockState material = getMaterial(x, dy, z, minY, maxY, feather);
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

    private BlockState getMaterial(double x, double y, double z, double lower, double upper, double feather) {
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
