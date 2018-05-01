package me.dags.copy.operation;

import com.flowpowered.math.vector.Vector3i;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.block.Snapshot;
import me.dags.copy.brush.History;
import me.dags.copy.brush.line.iterator.LineIterator;
import me.dags.copy.brush.option.value.Palette;
import me.dags.copy.event.PlaceEvent;
import me.dags.copy.operation.modifier.Translate;
import me.dags.copy.util.fmt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class IterateOperation implements Operation {

    private static final LinkedList<BlockSnapshot> EMPTY_RECORD = new LinkedList<>();

    private final UUID owner;
    private final World world;
    private final History history;
    private final Palette palette;
    private final Translate translate;
    private final LineIterator iterator;

    private List<Snapshot> snapshots = Collections.emptyList();
    private LinkedList<BlockSnapshot> record = EMPTY_RECORD;

    public IterateOperation(World world, UUID owner, LineIterator iterator, Translate translate, Palette palette, History history) {
        this.owner = owner;
        this.world = world;
        this.history = history;
        this.palette = palette;
        this.iterator = iterator;
        this.translate = translate;
    }

    @Override
    public Phase calculate(int limit) {
        // start new list of changes
        snapshots = new LinkedList<>();

        // calculate the next lot of positions
        while (limit-- > 0 && iterator.hasNext()) {
            Vector3i position = iterator.nextPosition();
            BlockState state = palette.next();
            Snapshot snapshot = new Snapshot(state, position, owner);
            translate.accept(world, snapshot);
            snapshots.add(snapshot);
        }

        // go to test phase
        return Phase.TEST;
    }

    @Override
    public Phase test(int limit) {
        // is player still online
        Optional<Player> player = Sponge.getServer().getPlayer(owner);
        if (!player.isPresent()) {
            return Phase.CANCELLED;
        }

        // create new event cause
        Cause cause = PlayerManager.getInstance().getCause(player.get());

        // post event
        PlaceEvent event = new PlaceEvent(cause, world, snapshots);
        boolean cancel = Sponge.getEventManager().post(event);

        // event cancelled
        if (cancel) {
            // calculate next points on snapshots
            if (iterator.hasNext()) {
                return Phase.CALCULATE;
            }
            return Phase.CANCELLED;
        }

        // event allowed, go to apply phase
        return Phase.APPLY;
    }

    @Override
    public Phase apply(int limit) {
        // apply the current list of snapshots
        for (Snapshot snapshot : snapshots) {
            if (snapshot.isValid()) {
                if (record.isEmpty()) {
                    record = history.nextRecord();
                }
                record.add(snapshot.getFrom(world));
                snapshot.restore(world);
            }
        }

        // go back to calculate phase if more stuff to draw
        if (iterator.hasNext()) {
            return Phase.CALCULATE;
        }

        // dispose
        return Phase.DISPOSE;
    }

    @Override
    public void dispose(Phase phase) {
        Sponge.getServer().getPlayer(owner).ifPresent(player -> {
            if (phase == Operation.Phase.ERROR) {
                fmt.error("Error occurred during operation").tell(CopyPasta.NOTICE_TYPE, player);
            }
            if (phase == Operation.Phase.CANCELLED) {
                fmt.error("Operation cancelled by plugin").tell(CopyPasta.NOTICE_TYPE, player);
            }
            if (phase == Operation.Phase.DISPOSE) {
                fmt.stress("Operation complete").tell(CopyPasta.NOTICE_TYPE, player);
            }
        });

        PlayerManager.getInstance().get(owner).ifPresent(data -> data.setOperating(false));
    }
}
