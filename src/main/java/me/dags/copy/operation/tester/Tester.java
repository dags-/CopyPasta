package me.dags.copy.operation.tester;

import me.dags.copy.event.BrushPlaceEvent;
import me.dags.copy.event.LocatableBlockChange;
import me.dags.copy.operation.Operation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Tester {

    private final Cause cause;
    private final WeakReference<World> world;
    private final List<LocatableBlockChange> changes;
    private Iterator<LocatableBlockChange> iterator = null;

    public Tester(World world, List<LocatableBlockChange> changes, Cause cause) {
        this.cause = cause;
        this.world = new WeakReference<>(world);
        this.changes = changes;
    }

    public Operation.Phase test(int limit) {
        World world = this.world.get();
        if (world == null) {
            return Operation.Phase.DISPOSE;
        }

        if (iterator == null) {
            iterator = changes.iterator();
        }

        List<LocatableBlockChange> changes = new LinkedList<>();
        while (iterator.hasNext() && limit-- > 0) {
            changes.add(iterator.next());
        }

        BrushPlaceEvent event = new BrushPlaceEvent(changes, world, cause);
        boolean cancelled = Sponge.getEventManager().post(event);

        if (cancelled) {
            return Operation.Phase.CANCELLED;
        }

        if (iterator.hasNext()) {
            return Operation.Phase.TEST;
        }

        return Operation.Phase.APPLY;
    }
}
