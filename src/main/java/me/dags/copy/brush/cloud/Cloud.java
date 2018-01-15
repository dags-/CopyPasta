package me.dags.copy.brush.cloud;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import me.dags.copy.block.volume.BufferBuilder;
import me.dags.copy.block.volume.BufferView;
import me.dags.copy.brush.Brush;
import org.spongepowered.api.block.BlockState;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
    private final int height;
    private final int offset;
    private final float depth;
    private final float featherRange;
    private final float featherRadius2;

    private final int centerX;
    private final int centerY;
    private final int centerZ;

    private final int total;
    private final List<BlockState> materials;

    public Cloud(Brush brush, Collection<BlockState> materials) {
        this.seed = brush.getOption(CloudBrush.SEED);
        this.octaves = brush.getOption(CloudBrush.OCTAVES);
        this.frequency = 1D / brush.getOption(CloudBrush.SCALE);
        this.max = ValueNoise.maxValue(octaves);
        this.radius = brush.getOption(CloudBrush.RADIUS);
        this.radius2 = radius * radius;
        this.height = brush.getOption(CloudBrush.HEIGHT);
        this.offset = Math.abs(brush.getOption(CloudBrush.OFFSET));
        this.depth = brush.getOption(CloudBrush.DETAIL);
        float feather = brush.getOption(CloudBrush.FEATHER);
        this.featherRadius2 = (radius * (1 - feather)) * (radius * (1 - feather));
        this.featherRange = radius * featherRadius2;
        this.total = materials.size() - 1;
        this.materials = ImmutableList.copyOf(materials);
        this.centerX = radius + 1;
        this.centerY = offset + 1;
        this.centerZ = radius + 1;
    }

    public Runnable createTask(UUID owner, Vector3i pos, FutureCallback<BufferView> callback) {
        return () -> {
            try {
                BufferView volume = apply(pos, owner).getView();
                callback.onSuccess(volume);
            } catch (Throwable t) {
                callback.onFailure(t);
            }
        };
    }

    public BufferBuilder apply(Vector3i pos, UUID owner) {
        int diameter = 1 + radius * 2;
        int maxHeight = this.height + (offset * 2) + 1;
        int volume = diameter * maxHeight * diameter;
        BufferBuilder buffer = new BufferBuilder(owner, pos, volume);

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

        return buffer;
    }

    private void apply(BufferBuilder buffer, Vector3i pos, int dx, int dz, float distance2) {
        int x = pos.getX() + dx;
        int y = pos.getY();
        int z = pos.getZ() + dz;

        double x1 = x * depth;
        double z1 = z * depth;

        double hMod = modifier(distance2, radius2);
        double feather = feather(distance2, featherRadius2, featherRange);
        double noise0 = ValueNoise.getValue(x, y, z, seed, frequency, octaves) / max;

        int elevation = (int) Math.round(noise0 * height * hMod);
        int startY = -offset;
        int endY = offset + elevation;

        for (int dy = startY; dy <= endY; dy++) {
            double y1 = y + dy;
            double vMod = modifier(dy, dy < 0 ? Math.min(-1, startY) : Math.max(1, endY));
            double noise1 = ValueNoise.getValue(x1, y1, z1, seed, frequency, octaves) / max;

            int index = (int) Math.round(total * noise1 * feather * vMod);
            index = Math.min(total, Math.max(0, index));

            BlockState material = materials.get(index);
            buffer.addAbsolute(material, centerX + dx, centerY + dy, centerZ + dz);
        }
    }

    private static double feather(double value, double bound, double range) {
        if (value > bound) {
            double d0 = value - bound;
            return 1D - (d0 / range);
        }
        return 1D;
    }

    private static double modifier(double value, double range) {
        double modifier = value / range;
        return 1D - (modifier * modifier);
    }
}
