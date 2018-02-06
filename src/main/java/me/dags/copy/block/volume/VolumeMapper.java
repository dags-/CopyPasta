package me.dags.copy.block.volume;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.util.concurrent.FutureCallback;
import me.dags.copy.block.state.State;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class VolumeMapper {

    private final int angle;
    private final double radians;
    private final boolean flipX;
    private final boolean flipY;
    private final boolean flipZ;
    private final Vector3i origin;
    private final Collection<State.Mapper> mappers;

    public VolumeMapper(Vector3i origin, int angle, boolean x, boolean y, boolean z, Collection<State.Mapper> mappers) {
        this.angle = angle;
        this.radians = Math.toRadians(angle);
        this.flipX = x;
        this.flipY = y;
        this.flipZ = z;
        this.mappers = mappers;
        this.origin = origin;
    }

    public Runnable createTask(ImmutableBlockVolume source, Vector3i position, UUID owner, FutureCallback<BufferView> callback) {
        return new Task(source, position, owner, callback);
    }

    public BufferView apply(ImmutableBlockVolume source, Vector3i position, UUID owner) {
        Vector3i pos1 = apply(source.getBlockMin());
        Vector3i pos2 = apply(source.getBlockMax());
        Vector3i min = pos1.min(pos2);
        Vector3i max = pos1.max(pos2);
        Vector3i size = max.sub(min);

        int volume = size.getX() * (1 + size.getY()) * size.getZ();
        BufferBuilder buffer = new BufferBuilder(owner, position, volume);

        // can't use block-calculator off the main thread!
        for (int y = source.getBlockMin().getY(); y <= source.getBlockMax().getY(); y++) {
            for (int z = source.getBlockMin().getZ(); z <= source.getBlockMax().getZ(); z++) {
                for (int x = source.getBlockMin().getX(); x <= source.getBlockMax().getX(); x++) {
                    BlockState state = source.getBlock(x, y, z);
                    int relX = x - origin.getX();
                    int relY = y - origin.getY();
                    int relZ = z - origin.getZ();
                    visit(state, relX, relY, relZ, buffer);
                }
            }
        }

        return buffer.getView();
    }

    private void visit(BlockState state, int x, int y, int z, BufferBuilder buffer) {
        for (State.Mapper mapper : mappers) {
            state = mapper.map(state);
        }

        if (flipY) {
            y = -y;
            y += getFlipYOffset(state);
        }

        if (flipX) {
            x = -x;
        }

        if (flipZ) {
            z = -z;
        }

        if (angle != 0) {
            int rx = rotateY(x, z, radians, -1);
            int rz = rotateY(z, x, radians, 1);
            x = rx;
            z = rz;
        }

        buffer.addRelative(state, x, y, z);
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

    private static int getFlipYOffset(BlockState state) {
        Optional<BlockTrait<?>> trait = state.getTrait("half");
        if (trait.isPresent()) {
            Optional<?> value = state.getTraitValue(trait.get());
            if (value.isPresent()) {
                String half = value.get().toString();
                if (half.equals("upper")) {
                    return 1;
                }
                if (half.equals("lower")) {
                    return -1;
                }
            }
        }
        return 0;
    }

    private class Task implements Runnable {

        private final UUID owner;
        private final Vector3i position;
        private final ImmutableBlockVolume source;
        private final FutureCallback<BufferView> callback;

        private Task(ImmutableBlockVolume source, Vector3i position, UUID owner, FutureCallback<BufferView> callback) {
            this.owner = owner;
            this.source = source;
            this.position = position;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                BufferView transformed = apply(source, position, owner);
                callback.onSuccess(transformed);
            } catch (Throwable t) {
                callback.onFailure(t);
            }
        }
    }
}
