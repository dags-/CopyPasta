package me.dags.copy.operation.phase;

import me.dags.copy.block.Snapshot;
import me.dags.copy.block.volume.BufferView;
import me.dags.copy.event.PlaceEvent;
import me.dags.copy.operation.Operation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class Test {

    private final UUID owner;
    private final World world;
    private final BufferView view;

    private Cause cause;
    private Iterator<Snapshot> iterator;

    public Test(UUID owner, World world, BufferView view) {
        this.owner = owner;
        this.world = world;
        this.view = view;
    }

    public Operation.Phase test(int limit) {
        if (cause == null) {
            Optional<Player> player = Sponge.getServer().getPlayer(owner);
            if (!player.isPresent()) {
                return Operation.Phase.CANCELLED;
            }
            cause = Cause.source(player.get())
                    .notifier(player.get())
                    .owner(player.get())
                    .build();
        }

        if (iterator == null) {
            iterator = view.iterator();
        }

        List<Snapshot> snapshots = new ArrayList<>(limit);
        while (iterator.hasNext() && limit-- > 0) {
            snapshots.add(iterator.next());
        }

        PlaceEvent event = new PlaceEvent(cause, world, snapshots);
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
