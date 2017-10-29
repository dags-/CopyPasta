package me.dags.copy.operation.applier;

import me.dags.copy.brush.History;
import me.dags.copy.event.LocatableBlockChange;
import me.dags.copy.operation.Operation;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Applier {

    private final UUID owner;
    private final Cause cause;
    private final History history;
    private final WeakReference<World> world;
    private final List<LocatableBlockChange> changes;

    private List<BlockSnapshot> records = null;
    private Iterator<LocatableBlockChange> iterator = null;

    public Applier(World world, UUID owner, List<LocatableBlockChange> changes, History history, Cause cause) {
        this.owner = owner;
        this.cause = cause;
        this.changes = changes;
        this.history = history;
        this.world = new WeakReference<>(world);
    }

    public void init() {
        records = history.nextRecord();
        iterator = changes.iterator();
    }

    public Operation.Phase apply(int limit) {
        World world = this.world.get();
        if (world == null) {
            return Operation.Phase.DISPOSE;
        }

        if (records == null || iterator == null) {
            init();
        }

        while (iterator.hasNext() && limit-- > 0) {
            LocatableBlockChange change = iterator.next();
            if (change.isValid()) {
                Location<World> location = change.getLocation();
                records.add(location.createSnapshot());
                world.setBlock(location.getBlockPosition(), change.getEndState(), BlockChangeFlag.NONE, cause);
                world.setNotifier(location.getBlockPosition(), owner);
                world.setCreator(location.getBlockPosition(), owner);
            }
        }

        if (iterator.hasNext()) {
            return Operation.Phase.APPLY;
        }

        return Operation.Phase.DISPOSE;
    }
}
