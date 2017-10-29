package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerData;
import me.dags.copy.PlayerManager;
import me.dags.copy.block.BlockUtils;
import me.dags.copy.block.Mappers;
import me.dags.copy.block.property.Axis;
import me.dags.copy.block.property.Facing;
import me.dags.copy.block.state.State;
import me.dags.copy.brush.*;
import me.dags.copy.brush.option.Option;
import me.dags.copy.event.LocatableBlockChange;
import me.dags.copy.operation.UndoOperation;
import me.dags.copy.operation.VolumeMapper;
import me.dags.copy.operation.visitor.Visitor3D;
import me.dags.copy.registry.brush.BrushSupplier;
import me.dags.copy.util.fmt;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"clipboard", "c"})
public class ClipboardBrush extends AbstractBrush {

    protected static final Random RANDOM = new Random();

    public static final Option<Boolean> FLIPX = Option.of("x.flip", false);
    public static final Option<Boolean> FLIPY = Option.of("y.flip", false);
    public static final Option<Boolean> FLIPZ = Option.of("z.flip", false);
    public static final Option<Boolean> AUTO_FLIP = Option.of("auto.flip", true);
    public static final Option<Boolean> AUTO_ROTATE = Option.of("auto.rotate", true);
    public static final Option<Boolean> RANDOM_ROTATE = Option.of("random.rotate", false);
    public static final Option<Boolean> RANDOM_FLIPH = Option.of("random.flip", false);
    public static final Option<Boolean> AIR = Option.of("air", false);
    public static final Option<Boolean> REQUIRE_SOLID = Option.of("solid", false);
    public static final Option<Vector3i> PASTE_OFFSET = Option.of("offset", Vector3i.ZERO);
    public static final Option<ReMappers> MAPPERS = Option.of("mappers", ReMappers.class, ReMappers::new);

    private SelectorBrush selector = new SelectorBrush(this);
    private Clipboard clipboard = Clipboard.empty();

    public ClipboardBrush() {
        super(5);
        setOption(RANGE, 5);
    }

    public void commitSelection(Player player, Vector3i min, Vector3i max, Vector3i origin, int size) {
        Clipboard clipboard = Clipboard.of(player, min, max, origin);
        setClipboard(clipboard);
        fmt.info("Copied ").stress(size).info(" blocks").tell(player);
    }

    @Override
    public String getPermission() {
        return "brush.clipboard";
    }

    @Override
    public void primary(Player player, Vector3i pos, Action action) {
        if (clipboard.isPresent()) {
            if (action == Action.SECONDARY) {
                setClipboard(Clipboard.empty());
                fmt.info("Cleared clipboard").tell(player);
                return;
            }

            undo(player, getHistory());
        } else {
            selector.primary(player, pos, action);
        }
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        if (clipboard.isPresent()) {
            apply(player, pos, getHistory());
        } else {
            selector.secondary(player, pos, action);
        }
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        if (!clipboard.isPresent()) {
            return;
        }

        if (getOption(REQUIRE_SOLID)) {
            pos = BlockUtils.findSolidFoundation(player.getWorld(), pos);
            if (pos == Vector3i.ZERO) {
                return;
            }
        }

        PlayerManager.getInstance().must(player).setOperating(true);
        Vector3i offset = getOption(PASTE_OFFSET);
        boolean withAir = getOption(AIR);
        VolumeMapper mapper = getMapper(clipboard, player);
        clipboard.paste(player, history, pos, offset, mapper, withAir, PlayerManager.getCause(player));
    }

    @Override
    public void undo(Player player, History history) {
        PlayerData data = PlayerManager.getInstance().must(player);

        if (data.isOperating()) {
            fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }

        if (history.hasNext()) {
            data.setOperating(true);
            LinkedList<BlockSnapshot> record = history.popRecord();
            UndoOperation operation = new UndoOperation(record, player.getUniqueId(), history);
            CopyPasta.getInstance().getOperationManager().queueOperation(operation);
        } else {
            fmt.error("No more history to undo!").tell(CopyPasta.NOTICE_TYPE, player);
        }
    }

    public void setClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    private VolumeMapper getMapper(Clipboard clipboard, Player player) {
        Facing phFacing = Facing.getHorizontal(player);
        Facing pvFacing = Facing.getVertical(player);

        int angle = 0;
        boolean flipX = getOption(FLIPX);
        boolean flipY = getOption(FLIPY);
        boolean flipZ = getOption(FLIPZ);

        if (getOption(AUTO_ROTATE)) {
            angle = clipboard.getHorizontalFacing().angle(phFacing, Axis.y);
        }

        if (getOption(RANDOM_ROTATE)) {
            int turns = RANDOM.nextInt(4);
            angle = turns * 90;
        }

        if (getOption(AUTO_FLIP) && pvFacing != Facing.none && clipboard.getVerticalFacing() != Facing.none) {
            flipY = pvFacing != clipboard.getVerticalFacing();
        }

        if (getOption(RANDOM_FLIPH)) {
            flipX = RANDOM.nextBoolean();
            flipZ = RANDOM.nextBoolean();
        }

        ReMappers reMappers = getOption(MAPPERS);
        ImmutableList.Builder<State.Mapper> mappers = ImmutableList.builder();
        mappers.addAll(reMappers);

        if (angle != 0) {
            mappers.add(Mappers.getRotationY(angle));
        }

        if (flipX) {
            mappers.add(Mappers.getFlipX());
        }

        if (flipY) {
            mappers.add(Mappers.getFlipY());
        }

        if (flipZ) {
            mappers.add(Mappers.getFlipZ());
        }

        return new VolumeMapper(angle, flipX, flipY, flipZ, mappers.build());
    }

    public static BrushSupplier supplier() {
        return player -> new ClipboardBrush();
    }

    public static Visitor3D visitor(Vector3i position, Vector3i offset, boolean air, List<LocatableBlockChange> changes) {
        return (w, v, x, y, z) -> {
            BlockState state = v.getBlock(x, y, z);
            if (state.getType() == BlockTypes.AIR && !air) {
                return 0;
            }

            x += position.getX();
            y += position.getY();
            z += position.getZ();

            if (!w.containsBlock(x, y, z)) {
                return 0;
            }

            Location<World> location = w.getLocation(offset.add(x, y, z));
            if (location.getBlock() != state) {
                changes.add(new LocatableBlockChange(location, state));
            }

            return 1;
        };
    }
}
