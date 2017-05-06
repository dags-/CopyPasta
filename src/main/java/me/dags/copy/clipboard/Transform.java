package me.dags.copy.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.util.concurrent.FutureCallback;
import me.dags.copy.block.Axis;
import me.dags.copy.block.Half;
import me.dags.copy.block.TraitUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Transform {

    private final int angle;
    private final double radians;
    private final boolean flipX;
    private final boolean flipY;
    private final boolean flipZ;

    Transform(int angle, boolean x, boolean y, boolean z) {
        this.angle = angle;
        this.radians = Math.toRadians(angle);
        this.flipX = x;
        this.flipY = y;
        this.flipZ = z;
    }

    public Runnable createTask(BlockVolume source, Cause cause, FutureCallback<BlockVolume> callback) {
        return new Task(cause, source, callback);
    }

    public Vector3i volumeOffset(BlockVolume volume) {
        Vector3i pos1 = apply(volume.getBlockMin());
        Vector3i pos2 = apply(volume.getBlockMax());
        return pos1.min(pos2);
    }

    public BlockVolume apply(BlockVolume source, Cause cause) {
        Vector3i pos1 = apply(source.getBlockMin());
        Vector3i pos2 = apply(source.getBlockMax());
        Vector3i min = pos1.min(pos2);
        Vector3i max = pos1.max(pos2);
        Vector3i size = max.sub(min).add(Vector3i.ONE);
        MutableBlockVolume buffer = Sponge.getRegistry().getExtentBufferFactory().createBlockBuffer(size);

        // can't use block-worker off the main thread!
        for (int y = source.getBlockMin().getY(); y <= source.getBlockMax().getY(); y++) {
            for (int z = source.getBlockMin().getZ(); z <= source.getBlockMax().getZ(); z++) {
                for (int x = source.getBlockMin().getX(); x <= source.getBlockMax().getX(); x++) {
                    visit(source, buffer, min, x, y, z, cause);
                }
            }
        }

        return buffer.getImmutableBlockCopy();
    }

    private void visit(BlockVolume src, MutableBlockVolume buffer, Vector3i offset, int x, int y, int z, Cause cause) {
        BlockState state = src.getBlock(x, y, z);
        if (state.getType() == BlockTypes.AIR) {
            return;
        }

        if (angle != 0) {
            int rx = rotateY(x, z, radians, -1);
            int rz = rotateY(z, x, radians, 1);
            x = rx - offset.getX();
            z = rz - offset.getZ();
            state = TraitUtils.rotateFacing(state, Axis.y, angle);
            state = TraitUtils.rotateAxis(state, Axis.y, angle);
        }

        if (flipY) {
            y = buffer.getBlockMax().getY() - y;

            int adjustment = TraitUtils.flipDoorY(state);
            y += adjustment;

            // not a door, so flip normally
            if (adjustment == 0) {
                state = TraitUtils.flipHalf(state, Axis.y);
                state = TraitUtils.flipFacing(state, Axis.y);
            }
        }

        if (flipX) {
            x = buffer.getBlockMax().getX() - x;
            state = TraitUtils.flipFacing(state, Axis.x);
            state = TraitUtils.flipHinge(state, Axis.x);
        }

        if (flipZ) {
            z = buffer.getBlockMax().getZ() - z;
            state = TraitUtils.flipFacing(state, Axis.z);
            state = TraitUtils.flipHinge(state, Axis.z);
        }

        if (buffer.containsBlock(x, y, z)) {
            buffer.setBlock(x, y, z, state, cause);
        }
    }

    public Vector3i apply(Vector3i pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();

        if (angle != 0) {
            int rx = rotateY(x, z, radians, -1);
            int rz = rotateY(z, x, radians, 1);
            x = rx;
            z = rz;
        }

        return new Vector3i(x, y, z);
    }

    private static int rotateY(int a, int b, double rads, int sign) {
        return (int) Math.round(a * Math.cos(rads) + (sign * b) * Math.sin(rads));
    }

    private class Task implements Runnable {

        private final Cause cause;
        private final BlockVolume source;
        private final FutureCallback<BlockVolume> callback;

        private Task(Cause cause, BlockVolume source, FutureCallback<BlockVolume> callback) {
            this.cause = cause;
            this.source = source;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                BlockVolume transformed = apply(source, cause);
                callback.onSuccess(transformed);
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }
    }
}
