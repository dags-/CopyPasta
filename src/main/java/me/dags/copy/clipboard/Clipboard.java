package me.dags.copy.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.util.concurrent.FutureCallback;
import me.dags.commandbus.fmt.Fmt;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerData;
import me.dags.copy.block.Facing;
import me.dags.copy.operation.PasteOperation;
import me.dags.copy.operation.UndoOperation;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Clipboard {

    private final Vector3i origin;
    private final BlockVolume source;
    private final Facing verticalFacing;
    private final Facing horizontalFacing;
    private final History history = new History(5);

    private Clipboard(BlockVolume source, Vector3i origin, Facing horizontalFacing, Facing verticalFacing) {
        this.source = source.getImmutableBlockCopy();
        this.origin = origin;
        this.verticalFacing = verticalFacing;
        this.horizontalFacing = horizontalFacing;
    }

    public History getHistory() {
        return history;
    }

    public void paste(Player player, Vector3i pos, Cause cause) {
        PlayerData data = CopyPasta.getInstance().getData(player);

        if (data.isOperating()) {
            Fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }

        data.setOperating(true);

        ClipboardOptions options = data.ensureOptions();
        options.setClipboardFacingH(horizontalFacing, verticalFacing);
        options.setPlayerFacing(player);

        Transform transform = options.createTransform();
        Vector3i volumeOffset = transform.volumeOffset(source);
        Vector3i pastePosition = transform.apply(origin).add(pos).add(volumeOffset);

        FutureCallback<BlockVolume> callback = callback(player, pastePosition, options.pasteAir(), cause);
        Runnable asyncTransform = transform.createTask(source, cause, callback);
        CopyPasta.getInstance().submitAsync(asyncTransform);
    }

    public void undo(Player player) {
        PlayerData data = CopyPasta.getInstance().getData(player);

        if (data.isOperating()) {
            Fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }

        if (history.hasNext()) {
            data.setOperating(true);
            List<BlockSnapshot> record = history.popRecord();
            UndoOperation operation = new UndoOperation(record, player.getUniqueId());
            CopyPasta.getInstance().getOperationManager().queueOperation(operation);
        } else {
            Fmt.error("No more history to undo!").tell(CopyPasta.NOTICE_TYPE, player);
        }
    }

    private FutureCallback<BlockVolume> callback(Player player, Vector3i position, boolean air, Cause cause) {
        final UUID uuid = player.getUniqueId();
        final WeakReference<World> worldRef = new WeakReference<>(player.getWorld());

        return new FutureCallback<BlockVolume>() {
            @Override
            public void onSuccess(@Nullable BlockVolume result) {
                if (result != null) {
                    PasteOperation operation = new PasteOperation(cause, worldRef, uuid, result, position, history, air);
                    CopyPasta.getInstance().getOperationManager().queueOperation(operation);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Fmt.warn("Unable to transform the clipboard! See the console").tell(player);
                t.printStackTrace();
            }
        };
    }

    public static Clipboard of(Player player, Vector3i min, Vector3i max, Vector3i origin) {
        Vector3i offset = min.sub(origin);
        Facing verticalFacing = Facing.verticalFacing(player);
        Facing horizontalFacing = Facing.horizontalFacing(player);
        BlockVolume backing = player.getWorld().getBlockView(min, max).getRelativeBlockView();
        return new Clipboard(backing, offset, horizontalFacing, verticalFacing);
    }
}
