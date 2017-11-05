package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.CopyPasta;
import me.dags.copy.block.property.Facing;
import me.dags.copy.brush.History;
import me.dags.copy.brush.stencil.StencilBrush;
import me.dags.copy.event.LocatableBlockChange;
import me.dags.copy.operation.Callback;
import me.dags.copy.operation.Operation;
import me.dags.copy.operation.PlaceOperation;
import me.dags.copy.operation.VolumeMapper;
import me.dags.copy.operation.applier.Applier;
import me.dags.copy.operation.calculator.Calculator;
import me.dags.copy.operation.calculator.Volume;
import me.dags.copy.operation.tester.Tester;
import me.dags.copy.operation.visitor.Visitor3D;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Clipboard {

    private static final Clipboard EMPTY = new Clipboard();

    private final ImmutableBlockVolume source;
    private final Facing horizontalFacing;
    private final Facing verticalFacing;
    private final Vector3i origin;
    private final boolean stencil;

    private Clipboard() {
        this.horizontalFacing = Facing.none;
        this.verticalFacing = Facing.none;
        this.source = null;
        this.stencil = false;
        this.origin = Vector3i.ZERO;
    }

    protected Clipboard(ImmutableBlockVolume source, Vector3i origin, Facing horizontalFacing, Facing verticalFacing) {
        this(source, origin, horizontalFacing, verticalFacing, false);
    }

    protected Clipboard(ImmutableBlockVolume source, Vector3i origin, Facing horizontalFacing, Facing verticalFacing, boolean stencil) {
        this.source = source;
        this.origin = origin;
        this.stencil = stencil;
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

    public void paste(Player player, History history, Vector3i pos, Vector3i offset, VolumeMapper transform, boolean air, Cause cause) {
        if (!isPresent()) {
            return;
        }

        Vector3i volumeOffset = transform.volumeOffset(source);
        Vector3i pastePosition = transform.apply(origin).add(pos).add(volumeOffset);

        Callback callback = Callback.of(player, (owner, world, result) -> {
            List<LocatableBlockChange> changes = new LinkedList<>();
            Calculator calculator = new Volume(world, result);
            Tester tester = new Tester(world, changes, cause);
            Applier applier = new Applier(world, owner, changes, history, cause);
            Visitor3D visitor = getVisitor(pastePosition, offset, air, changes);
            Operation operation = new PlaceOperation(owner, calculator, tester, applier, visitor);
            CopyPasta.getInstance().getOperationManager().queueOperation(operation);
        });

        Runnable asyncTransform = transform.createTask(source, cause, callback);
        CopyPasta.getInstance().submitAsync(asyncTransform);
    }

    private Visitor3D getVisitor(Vector3i position, Vector3i offset, boolean air, List<LocatableBlockChange> changes) {
        if (stencil) {
            return StencilBrush.visitor(position, offset, air, changes);
        }
        return ClipboardBrush.visitor(position, offset, air, changes);
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
        return new Clipboard(volume, origin, horizontalFacing, verticalFacing, true);
    }
}
