package me.dags.copy.operation;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.BlockChangeFlag;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class UndoOperation implements Operation {

    private final List<BlockSnapshot> snapshots;

    public UndoOperation(List<BlockSnapshot> snapshots) {
        this.snapshots = snapshots;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void calculate() {

    }

    @Override
    public void test() {

    }

    @Override
    public void apply() {
        for (BlockSnapshot snapshot : snapshots) {
            snapshot.restore(true, BlockChangeFlag.NONE);
        }
    }

    @Override
    public void dispose() {

    }
}
