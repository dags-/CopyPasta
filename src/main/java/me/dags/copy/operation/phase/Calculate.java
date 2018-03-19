package me.dags.copy.operation.phase;

import java.util.Iterator;
import me.dags.copy.block.Snapshot;
import me.dags.copy.block.volume.BufferView;
import me.dags.copy.operation.Operation;
import me.dags.copy.operation.modifier.Filter;
import me.dags.copy.operation.modifier.Translate;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class Calculate {

    private final World world;
    private final BufferView view;
    private final Filter toFilter;
    private final Filter fromFilter;
    private final Translate transform;

    private Iterator<Snapshot> iterator;

    public Calculate(World world, BufferView view, Filter fromFilter, Filter toFilter, Translate transform) {
        this.view = view;
        this.world = world;
        this.toFilter = toFilter;
        this.fromFilter = fromFilter;
        this.transform = transform;
    }

    public Operation.Phase calculate(int limit) {
        if (iterator == null) {
            iterator = view.iterator();
        }

        while (iterator.hasNext() && limit-- > 0) {
            Snapshot to = iterator.next();
            if (!toFilter.test(to.getState())) {
                to.setValid(false);
                continue;
            }

            transform.accept(world, to);
            BlockState from = world.getBlock(to.getPosition());

            if (!fromFilter.test(from)) {
                to.setValid(false);
            }
        }

        if (iterator.hasNext()) {
            return Operation.Phase.CALCULATE;
        }

        return Operation.Phase.TEST;
    }
}
