package me.dags.copy.operation.phase;

import me.dags.copy.block.volume.BufferView;
import me.dags.copy.block.volume.Snapshot;
import me.dags.copy.brush.History;
import me.dags.copy.operation.Operation;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.World;

import java.util.Iterator;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Apply {

    private final World world;
    private final BufferView view;
    private final History history;

    private List<BlockSnapshot> records;
    private Iterator<Snapshot> iterator;

    public Apply(World world, BufferView view, History history) {
        this.world = world;
        this.view = view;
        this.history = history;
    }

    public Operation.Phase apply(int limit) {
        if (records == null || iterator == null) {
            records = history.nextRecord();
            iterator = view.iterator();
        }

        while (iterator.hasNext() && limit-- > 0) {
            Snapshot snapshot = iterator.next();
            if (snapshot.isValid()) {
                records.add(snapshot.getFrom(world));
                snapshot.restore(world);
            }
        }

        if (iterator.hasNext()) {
            return Operation.Phase.APPLY;
        }

        return Operation.Phase.DISPOSE;
    }
}
