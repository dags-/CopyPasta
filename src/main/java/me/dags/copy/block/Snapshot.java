package me.dags.copy.block;

import com.flowpowered.math.vector.Vector3i;
import java.util.UUID;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class Snapshot {

    private final BlockState state;
    private final BlockSnapshot.Builder builder = BlockSnapshot.builder();
    private boolean valid = true;

    private Vector3i position;
    private BlockSnapshot from;
    private BlockSnapshot to;
    private Location<World> location;
    private Transaction<BlockSnapshot> transaction;

    public Snapshot(BlockState state, Vector3i position, UUID owner) {
        this.state = state;
        this.position = position;
        this.builder.position(position)
                .blockState(state)
                .notifier(owner)
                .creator(owner);
    }

    public void setPosition(Vector3i position) {
        this.position = position;
        this.builder.position(position);
    }

    public BlockState getState() {
        return state;
    }

    public Vector3i getPosition() {
        return position;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
        if (transaction != null) {
            transaction.setValid(valid);
        }
    }

    public boolean isValid() {
        return transaction == null ? valid : transaction.isValid();
    }

    public Location<World> getLocation(World world) {
        if (location == null) {
            location = world.getLocation(position);
        }
        return location;
    }

    public Transaction<BlockSnapshot> getTransaction(World world) {
        if (transaction == null) {
            transaction = new Transaction<>(getFrom(world), getTo(world));
            transaction.setValid(valid);
        }
        return transaction;
    }

    public BlockSnapshot getFrom(World world) {
        if (from == null) {
            from = world.createSnapshot(position);
        }
        return from;
    }

    public BlockSnapshot getTo(World world) {
        if (to == null) {
            to = builder.world(world.getProperties()).build();
        }
        return to;
    }

    public void restore(World world) {
        getTo(world).restore(true, BlockChangeFlags.NONE);
    }
}
