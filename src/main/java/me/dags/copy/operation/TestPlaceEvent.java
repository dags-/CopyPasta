package me.dags.copy.operation;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class TestPlaceEvent extends AbstractEvent implements ChangeBlockEvent.Place {

    private final World world;
    private final Cause cause;
    private final List<LocatableBlockChange> changes;

    private boolean cancelled = false;

    public TestPlaceEvent(List<LocatableBlockChange> changes, World world, Cause cause) {
        this.changes = changes;
        this.world = world;
        this.cause = cause;
    }

    @Override
    public List<Transaction<BlockSnapshot>> filter(Predicate<Location<World>> predicate) {
        for (LocatableBlockChange change : changes) {
            if (!predicate.test(change.getLocation())) {
                change.setValid(false);
            }
        }
        return getTransactions();
    }

    @Override
    public void filterAll() {
        setCancelled(true);
    }

    @Override
    public List<Transaction<BlockSnapshot>> getTransactions() {
        return Collections.emptyList();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public World getTargetWorld() {
        return world;
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
