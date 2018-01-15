package me.dags.copy.operation.phase;

import me.dags.copy.block.volume.BufferView;
import me.dags.copy.block.volume.Snapshot;
import me.dags.copy.operation.Operation;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.World;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class Calculate {

    public static final Predicate<BlockState> ANY = s -> true;
    public static final Predicate<BlockState> NO_AIR = s -> s.getType() != BlockTypes.AIR;

    private final World world;
    private final BufferView view;
    private final Modifier modifier;

    private Iterator<Snapshot> iterator;

    public Calculate(World world, BufferView view, Modifier modifier) {
        this.view = view;
        this.world = world;
        this.modifier = modifier;
    }

    public Operation.Phase calculate(int limit) {
        if (iterator == null) {
            iterator = view.iterator();
        }

        while (iterator.hasNext() && limit-- > 0) {
            Snapshot snapshot = iterator.next();
            modifier.apply(world, snapshot);
        }

        if (iterator.hasNext()) {
            return Operation.Phase.CALCULATE;
        }

        return Operation.Phase.TEST;
    }

    public static Predicate<BlockState> applyAir(boolean air) {
        return air ? ANY : NO_AIR;
    }
}
