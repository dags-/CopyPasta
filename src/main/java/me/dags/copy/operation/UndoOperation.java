package me.dags.copy.operation;

import me.dags.commandbus.format.FMT;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.BlockChangeFlag;

import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class UndoOperation implements Operation {

    private final List<BlockSnapshot> snapshots;
    private final UUID owner;

    public UndoOperation(List<BlockSnapshot> snapshots, UUID owner) {
        this.snapshots = snapshots;
        this.owner = owner;
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

        Sponge.getServer().getPlayer(owner).ifPresent(player -> {
            PlayerData data = CopyPasta.getInstance().getData(player);
            data.getClipboard().ifPresent(clipboard -> {
                int size = clipboard.getHistory().getSize();
                int max = clipboard.getHistory().getMax();
                FMT.subdued("Undo Complete (%s / %s)", size, max).tell(CopyPasta.NOTICE_TYPE, player);
            });
        });
    }

    @Override
    public void dispose() {

    }
}
