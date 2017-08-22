package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.util.concurrent.FutureCallback;
import me.dags.commandbus.fmt.Fmt;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerData;
import me.dags.copy.block.property.Facing;
import me.dags.copy.brush.History;
import me.dags.copy.operation.PasteOperation;
import me.dags.copy.operation.VolumeMapper;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Clipboard {

    private static final Clipboard EMPTY = new Clipboard();

    private final Facing verticalFacing;
    private final Facing horizontalFacing;
    private final BlockVolume source;
    private final Vector3i origin;

    private Clipboard() {
        this.verticalFacing = Facing.none;
        this.horizontalFacing = Facing.none;
        this.source = null;
        this.origin = Vector3i.ZERO;
    }

    protected Clipboard(BlockVolume source, Vector3i origin, Facing horizontalFacing, Facing verticalFacing) {
        this.source = source.getImmutableBlockCopy();
        this.origin = origin;
        this.verticalFacing = verticalFacing;
        this.horizontalFacing = horizontalFacing;
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public Facing getHorizontalFacing() {
        return horizontalFacing;
    }

    public Facing getVerticalFacing() {
        return verticalFacing;
    }

    public void paste(Player player, History history, Vector3i pos, VolumeMapper transform, boolean air, Cause cause) {
        if (!isPresent()) {
            return;
        }

        PlayerData data = CopyPasta.getInstance().ensureData(player);

        if (data.isOperating()) {
            Fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }

        data.setOperating(true);

        Vector3i volumeOffset = transform.volumeOffset(source);
        Vector3i pastePosition = transform.apply(origin).add(pos).add(volumeOffset);

        FutureCallback<BlockVolume> callback = callback(player, history, pastePosition, air, cause);
        Runnable asyncTransform = transform.createTask(source, cause, callback);
        CopyPasta.getInstance().submitAsync(asyncTransform);
    }

    private FutureCallback<BlockVolume> callback(Player player, History history, Vector3i position, boolean air, Cause cause) {
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

    public static Clipboard empty() {
        return EMPTY;
    }

    public static Clipboard of(Player player, Vector3i min, Vector3i max, Vector3i origin) {
        Vector3i offset = min.sub(origin);
        Facing verticalFacing = Facing.getVertical(player);
        Facing horizontalFacing = Facing.getHorizontal(player);
        BlockVolume backing = player.getWorld().getBlockView(min, max).getRelativeBlockView().getImmutableBlockCopy();
        return new Clipboard(backing, offset, horizontalFacing, verticalFacing);
    }
}
