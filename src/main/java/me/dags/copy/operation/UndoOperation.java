package me.dags.copy.operation;

import java.util.LinkedList;
import java.util.UUID;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.History;
import me.dags.copy.util.fmt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;

/**
 * @author dags <dags@dags.me>
 */
public class UndoOperation implements Operation {

    private final LinkedList<BlockSnapshot> snapshots;
    private final History history;
    private final UUID owner;

    public UndoOperation(LinkedList<BlockSnapshot> snapshots, UUID owner, History history) {
        this.snapshots = snapshots;
        this.history = history;
        this.owner = owner;
    }

    @Override
    public Phase calculate(int limit) {
        return Phase.TEST;
    }

    @Override
    public Phase test(int limit) {
        return Phase.APPLY;
    }

    @Override
    public Phase apply(int limit) {
        while (!snapshots.isEmpty() && limit-- > 0) {
            BlockSnapshot snapshot = snapshots.pollLast();
            snapshot.restore(true, BlockChangeFlags.NONE);
        }

        if (!snapshots.isEmpty()) {
            return Phase.APPLY;
        }

        return Phase.DISPOSE;
    }

    @Override
    public void dispose(Phase phase) {
        PlayerManager.getInstance().get(owner).ifPresent(data -> data.setOperating(false));
        Sponge.getServer().getPlayer(owner).ifPresent(player -> {
            int size = history.getSize();
            int max = history.getMax();
            fmt.sub("Undo Complete (%s / %s)", size, max).tell(CopyPasta.NOTICE_TYPE, player);
        });
    }
}
