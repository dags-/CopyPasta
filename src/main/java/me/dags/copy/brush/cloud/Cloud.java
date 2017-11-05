package me.dags.copy.brush.cloud;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;

import java.util.Collection;
import java.util.List;

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

    private Cloud(int seed, int scale, int octaves, int radius, int height, int offset, float detail, float feather, Collection<BlockState> materials) {
        this.seed = seed;
        this.octaves = octaves;
        this.frequency = 1D / scale;
        this.max = ValueNoise.maxValue(octaves);
        this.radius = radius;
        this.radius2 = radius * radius;
        this.height = height;
        this.offset = Math.abs(offset);
        this.depth = detail;
        this.featherRadius2 = (radius * (1 - feather)) * (radius * (1 - feather));
        this.featherRange = radius2 - featherRadius2;
        this.total = materials.size() - 1;
        this.materials = ImmutableList.copyOf(materials);
        this.centerX = radius + 1;
        this.centerY = offset + 1;
        this.centerZ = radius + 1;
    }

    public Runnable createTask(FutureCallback<BlockVolume> callback, Vector3i pos, Cause cause) {
        return () -> {
            try {
                BlockVolume volume = apply(pos, cause).getImmutableBlockCopy();
                callback.onSuccess(volume);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
    }

    public BlockVolume apply(Vector3i pos, Cause cause) {
        int diameter = 1 + radius * 2;
        Vector3i size = new Vector3i(diameter, height + offset, diameter);
        MutableBlockVolume volume = Sponge.getRegistry().getExtentBufferFactory().createBlockBuffer(size);

        for (int dz = 0; dz <= radius; dz++) {
            for (int dx = 0; dx <= radius; dx++) {
                int dist2 = (dx * dx) + (dz * dz);
                if (dist2 < radius2) {
                    apply(volume, pos, dx, dz, dist2, cause);
                    if (dx != 0) {
                        apply(volume, pos, -dx, dz, dist2, cause);
                    }
                    if (dz != 0) {
                        apply(volume, pos, dx, -dz, dist2, cause);
                    }
                    if (dx != 0 && dz != 0) {
                        apply(volume, pos, -dx, -dz, dist2, cause);
                    }
                }
            }
        }

        return volume;
    }

    private void apply(MutableBlockVolume volume, Vector3i pos, int dx, int dz, float distance2, Cause cause) {
        int x = pos.getX() + dx;
        int y = pos.getY();
        int z = pos.getZ() + dz;
        double x1 = x * depth;
        double z1 = z * depth;

        double horizontalMod = modifier(distance2, radius2);
        double opacity = opacity(distance2, featherRadius2, featherRange);
        double eNoise = ValueNoise.getValue(x, y, z, seed, frequency, octaves) / max;

        int elevation = (int) Math.round(eNoise * height * horizontalMod);
        elevation = Math.max(offset + 3, elevation);

        int startY = -offset;
        int endY = startY + elevation;

        for (int dy = startY; dy <= endY; dy++) {
            double y1 = y + dy;
            double vNoise = ValueNoise.getValue(x1, y1, z1, seed, frequency, octaves) / max;
            double verticalMod = modifier(dy, dy < 0 ? startY : endY);

            int index = (int) Math.round(total * vNoise * opacity * verticalMod);
            index = Math.min(total, Math.max(0, index));

            BlockState material = materials.get(index);
            volume.setBlock(centerX + dx, centerY + dy, centerZ + dz, material, cause);
        }
    }

    private static double opacity(double value, double bound, double range) {
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

    public static Cloud of(int seed, int scale, int octaves, int radius, int height, int offset, float detail, float feather, Collection<BlockState> materials) {
        return new Cloud(seed, scale, octaves, radius, height, offset, detail, feather, materials);
    }
}
