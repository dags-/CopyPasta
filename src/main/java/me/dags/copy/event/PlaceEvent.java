package me.dags.copy.event;

import com.google.common.collect.Lists;
import me.dags.copy.block.volume.Snapshot;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class PlaceEvent extends AbstractEvent implements BrushEvent {

    private final Cause cause;
    private final World world;
    private final List<Snapshot> view;

    private boolean cancelled;
    private List<Transaction<BlockSnapshot>> transactions;

    public PlaceEvent(Cause cause, World world, List<Snapshot> view) {
        this.cause = cause;
        this.world = world;
        this.view = view;
    }
    @Override
    public List<Transaction<BlockSnapshot>> getTransactions() {
        if (transactions == null) {
            transactions = new LinkedList<>();
            for (Snapshot snapshot : view) {
                transactions.add(snapshot.getTransaction(world));
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction<BlockSnapshot>> filter(Predicate<Location<World>> predicate) {
        List<Transaction<BlockSnapshot>> filtered = null;
        for (Snapshot snapshot : view) {
            if (!predicate.test(snapshot.getLocation(world))) {
                if (filtered == null) {
                    filtered = Lists.newLinkedList();
                }
                filtered.add(snapshot.getTransaction(world));
                snapshot.setValid(false);
            }
        }
        return filtered == null ? Collections.emptyList() : filtered;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
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
