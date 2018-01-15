package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.CopyPasta;
import me.dags.copy.block.property.Facing;
import me.dags.copy.block.volume.VolumeMapper;
import me.dags.copy.brush.History;
import me.dags.copy.operation.callback.Callback;
import me.dags.copy.operation.phase.Modifier;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public class Clipboard {

    private static final Clipboard EMPTY = new Clipboard();

    private final ImmutableBlockVolume source;
    private final Facing horizontalFacing;
    private final Facing verticalFacing;
    private final Vector3i origin;

    private Clipboard() {
        this.horizontalFacing = Facing.none;
        this.verticalFacing = Facing.none;
        this.source = null;
        this.origin = Vector3i.ZERO;
    }

    protected Clipboard(ImmutableBlockVolume source, Vector3i origin, Facing horizontalFacing, Facing verticalFacing) {
        this.source = source;
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

    public void paste(Player player, History history, Vector3i pos, VolumeMapper transform, Modifier modifier) {
        if (isPresent()) {
            Vector3i originOffset = transform.apply(origin); // rotate origin
            Vector3i volumeOffset = transform.volumeOffset(source); // rotate volume, find min
            Vector3i position = pos.add(originOffset).add(volumeOffset); // apply offsets to paste position
            Callback callback = Callback.place(player, history, modifier);
            Runnable task = transform.createTask(source, position, player.getUniqueId(), callback);
            CopyPasta.getInstance().submitAsync(task);
        }
    }

    public static Clipboard empty() {
        return EMPTY;
    }

    public static Clipboard of(Player player, Vector3i min, Vector3i max, Vector3i origin) {
        Vector3i offset = min.sub(origin);
        Facing verticalFacing = Facing.getVertical(player);
        Facing horizontalFacing = Facing.getHorizontal(player);
        BlockVolume backing = player.getWorld().getBlockView(min, max).getRelativeBlockView();
        return new Clipboard(backing.getImmutableBlockCopy(), offset, horizontalFacing, verticalFacing);
    }

    public static Clipboard stencil(Player player, ImmutableBlockVolume volume, Vector3i origin) {
        Facing verticalFacing = Facing.getVertical(player);
        Facing horizontalFacing = Facing.getHorizontal(player);
        return new Clipboard(volume, origin, horizontalFacing, verticalFacing);
    }
}
