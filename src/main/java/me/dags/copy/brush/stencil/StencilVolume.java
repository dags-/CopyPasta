package me.dags.copy.brush.stencil;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.brush.Palette;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.extent.*;
import org.spongepowered.api.world.extent.worker.BlockVolumeWorker;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeMapper;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeMerger;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeReducer;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeVisitor;

import java.util.function.BiFunction;

/**
 * @author dags <dags@dags.me>
 */
public class StencilVolume implements ImmutableBlockVolume {

    private final Stencil stencil;
    private final Vector3i min;
    private final Vector3i max;
    private final Palette palette;
    private final BlockState air = BlockTypes.AIR.getDefaultState();

    StencilVolume(Stencil stencil, Palette palette) {
        this.stencil = stencil;
        this.min = stencil.getMin();
        this.max = stencil.getMax();
        this.palette = palette;
    }

    @Override
    public Vector3i getBlockMin() {
        return min;
    }

    @Override
    public Vector3i getBlockMax() {
        return max;
    }

    @Override
    public Vector3i getBlockSize() {
        return max;
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return x >= min.getX() && x <= max.getX() && z >= min.getZ() && z <= max.getZ();
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return stencil.contains(x, y, z) ? palette.next() : air;
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        return getBlock(x, y, z).getType();
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableBlockVolume getBlockView(Vector3i newMin, Vector3i newMax) {
        return this;
    }

    @Override
    public ImmutableBlockVolume getBlockView(DiscreteTransform3 transform) {
        return this;
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockVolumeWorker<? extends ImmutableBlockVolume> getBlockWorker(Cause cause) {
        return new BlockVolumeWorker<ImmutableBlockVolume>() {
            @Override
            public ImmutableBlockVolume getVolume() {
                return StencilVolume.this;
            }

            @Override
            public void map(BlockVolumeMapper mapper, MutableBlockVolume destination) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void merge(BlockVolume second, BlockVolumeMerger merger, MutableBlockVolume destination) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void iterate(BlockVolumeVisitor<ImmutableBlockVolume> visitor) {
                for (int y = getBlockMin().getY(); y <= getBlockMax().getY(); y++) {
                    for (int z = getBlockMin().getZ(); z <= getBlockMax().getZ(); z++) {
                        for (int x = getBlockMin().getX(); x <= getBlockMax().getX(); x++) {
                            visitor.visit(getVolume(), x, y, z);
                        }
                    }
                }
            }

            @Override
            public <T> T reduce(BlockVolumeReducer<T> reducer, BiFunction<T, T, T> merge, T identity) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
