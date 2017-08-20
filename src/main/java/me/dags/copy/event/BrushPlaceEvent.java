package me.dags.copy.event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
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
public class BrushPlaceEvent extends AbstractEvent implements BrushEvent {

    private final World world;
    private final Cause cause;
    private final List<LocatableBlockChange> changes;

    private boolean cancelled = false;
    private List<Transaction<BlockSnapshot>> transactions;

    public BrushPlaceEvent(List<LocatableBlockChange> changes, World world, Cause cause) {
        this.changes = changes;
        this.world = world;
        this.cause = cause;
    }

    @Override
    public List<Transaction<BlockSnapshot>> filter(Predicate<Location<World>> predicate) {
        List<Transaction<BlockSnapshot>> filtered = null;
        for (LocatableBlockChange change : changes) {
            if (!predicate.test(change.getLocation())) {
                if (filtered == null) {
                    filtered = Lists.newLinkedList();
                }
                filtered.add(change.getTransaction());
                change.setValid(false);
            }
        }
        return filtered != null ? filtered : Collections.emptyList();
    }

    @Override
    public void filterAll() {
        if (!cancelled) {
            cancelled = true;

            for (LocatableBlockChange change : changes) {
                change.setValid(false);
            }
        }
    }

    @Override
    public List<Transaction<BlockSnapshot>> getTransactions() {
        if (transactions == null) {
            ImmutableList.Builder<Transaction<BlockSnapshot>> builder = ImmutableList.builder();
            for (LocatableBlockChange change : changes) {
                builder.add(change.getTransaction());
            }
            transactions = builder.build();
        }
        return transactions;
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
