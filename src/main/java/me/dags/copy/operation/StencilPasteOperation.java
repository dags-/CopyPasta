package me.dags.copy.operation;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.block.BlockUtils;
import me.dags.copy.brush.History;
import me.dags.copy.event.LocatableBlockChange;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class StencilPasteOperation extends PasteOperation {

    public StencilPasteOperation(Cause cause, WeakReference<World> world, UUID uuid, BlockVolume source, Vector3i position, History history, boolean air) {
        super(cause, world, uuid, source, position, history, air);
    }

    @Override
    public void calculate() {
        final World world = this.world.get();

        if (world == null) {
            cancelled = true;
            return;
        }

        source.getBlockWorker(cause).iterate((v, x, y, z) -> {
            BlockState state = v.getBlock(x, y, z);
            if (state.getType() == BlockTypes.AIR && !air) {
                return;
            }

            Vector3i pos = position.add(x, y, z);
            pos = BlockUtils.findSolidFoundation(world, pos, 1);

            if (!world.containsBlock(pos)) {
                return;
            }

            Location<World> location = world.getLocation(pos);
            if (location.getBlock() != state) {
                transactions.add(new LocatableBlockChange(location, state));
            }
        });
    }
}
