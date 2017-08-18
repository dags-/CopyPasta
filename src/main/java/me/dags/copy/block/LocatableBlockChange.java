package me.dags.copy.block;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public final class LocatableBlockChange {

    private final Location<World> location;
    private final BlockState endState;

    private boolean valid = true;
    private Transaction<BlockSnapshot> transaction;

    public LocatableBlockChange(Location<World> location, BlockState state) {
        this.location = location;
        this.endState = state;
    }

    public boolean isValid() {
        return transaction == null ? valid : transaction.isValid();
    }

    public BlockState getEndState() {
        return endState;
    }

    public Location<World> getLocation() {
        return location;
    }

    public void setValid(boolean valid) {
        this.valid = valid;

        if (transaction != null) {
            transaction.setValid(valid);
        }
    }

    public Transaction<BlockSnapshot> getTransaction() {
        if (transaction == null) {
            BlockSnapshot from = location.createSnapshot();
            BlockSnapshot to = endState.snapshotFor(location);
            transaction = new Transaction<>(from, to);
            transaction.setValid(valid);
        }

        return transaction;
    }
}
