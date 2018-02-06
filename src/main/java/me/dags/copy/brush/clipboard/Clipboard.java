package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.CopyPasta;
import me.dags.copy.block.property.Facing;
import me.dags.copy.block.volume.VolumeMapper;
import me.dags.copy.brush.History;
import me.dags.copy.operation.callback.Callback;
import me.dags.copy.operation.modifier.Filter;
import me.dags.copy.operation.modifier.Translate;
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

    public Vector3i getOrigin() {
        return origin;
    }

    public Facing getHorizontalFacing() {
        return horizontalFacing;
    }

    public Facing getVerticalFacing() {
        return verticalFacing;
    }

    public void paste(Player player, History history, Vector3i position, VolumeMapper volumeMapper, Filter from, Filter to, Translate translate) {
        if (isPresent()) {
            Callback callback = Callback.of(player, history, from, to, translate);
            Runnable task = volumeMapper.createTask(source, position, player.getUniqueId(), callback);
            CopyPasta.getInstance().submitAsync(task);
        }
    }

    public static Clipboard empty() {
        return EMPTY;
    }

    public static Clipboard of(Player player, Vector3i min, Vector3i max, Vector3i origin) {
        Facing verticalFacing = Facing.getVertical(player);
        Facing horizontalFacing = Facing.getHorizontal(player);
        BlockVolume backing = player.getWorld().getBlockView(min, max).getRelativeBlockView();
        return new Clipboard(backing.getImmutableBlockCopy(), origin.sub(min), horizontalFacing, verticalFacing);
    }

    public static Clipboard stencil(Player player, ImmutableBlockVolume volume, Vector3i origin) {
        Facing verticalFacing = Facing.getVertical(player);
        Facing horizontalFacing = Facing.getHorizontal(player);
        return new Clipboard(volume, origin, horizontalFacing, verticalFacing);
    }
}
