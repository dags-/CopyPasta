package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerData;
import me.dags.copy.block.BlockUtils;
import me.dags.copy.block.Mappers;
import me.dags.copy.block.property.Axis;
import me.dags.copy.block.property.Facing;
import me.dags.copy.block.state.State;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Action;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.ReMappers;
import me.dags.copy.brush.option.Option;
import me.dags.copy.fmt;
import me.dags.copy.operation.UndoOperation;
import me.dags.copy.operation.VolumeMapper;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.Random;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"clipboard", "cb"})
public class ClipboardBrush extends AbstractBrush {

    protected static final Random RANDOM = new Random();

    public static final Option<Boolean> FLIPX = Option.of("x.flip", boolean.class);
    public static final Option<Boolean> FLIPY = Option.of("y.flip", boolean.class);
    public static final Option<Boolean> FLIPZ = Option.of("z.flip", boolean.class);
    public static final Option<Boolean> AUTO_FLIP = Option.of("auto.flip", boolean.class);
    public static final Option<Boolean> AUTO_ROTATE = Option.of("auto.rotate", boolean.class);
    public static final Option<Boolean> RANDOM_ROTATE = Option.of("random.reotate", boolean.class);
    public static final Option<Boolean> RANDOM_FLIPH = Option.of("random.flip", boolean.class);
    public static final Option<Boolean> AIR = Option.of("air", boolean.class);
    public static final Option<Boolean> REQUIRE_SOLID = Option.of("solid", boolean.class);
    public static final Option<Integer> PASTE_OFFSET = Option.of("offset", int.class);
    public static final Option<ReMappers> MAPPERS = Option.of("mappers", ReMappers.class);

    private SelectorBrush selector = new SelectorBrush(this);

    private Clipboard clipboard = Clipboard.empty();

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
                setClipboard(null);
                fmt.info("Cleared clipboard").tell(player);
                return;
            }

            undo(player);
        } else {
            selector.primary(player, pos, action);
        }
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        if (clipboard.isPresent()) {
            if (getOption(REQUIRE_SOLID, false)) {
                pos = BlockUtils.findSolidFoundation(player.getWorld(), pos);
                if (pos == Vector3i.ZERO) {
                    return;
                }
            }

            int offset = getOption(PASTE_OFFSET, 0);
            pos = pos.add(0, offset, 0);

            VolumeMapper mapper = getMapper(clipboard, player);
            clipboard.paste(player, getHistory(), pos, mapper, getOption(AIR, false), CopyPasta.getInstance().getCause(player));
        } else {
            selector.secondary(player, pos, action);
        }
    }

    @Override
    public void undo(Player player) {
        PlayerData data = CopyPasta.getInstance().ensureData(player);

        if (data.isOperating()) {
            fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }

        if (getHistory().hasNext()) {
            data.setOperating(true);
            List<BlockSnapshot> record = getHistory().popRecord();
            UndoOperation operation = new UndoOperation(record, player.getUniqueId(), getHistory());
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
        boolean flipX = getOption(FLIPX, false);
        boolean flipY = getOption(FLIPY, false);
        boolean flipZ = getOption(FLIPZ, false);

        if (getOption(AUTO_ROTATE, true)) {
            angle = clipboard.getHorizontalFacing().angle(phFacing, Axis.y);
        }

        if (getOption(RANDOM_ROTATE, false)) {
            int turns = RANDOM.nextInt(4);
            angle = turns * 90;
        }

        if (getOption(AUTO_FLIP, true) && pvFacing != Facing.none && clipboard.getVerticalFacing() != Facing.none) {
            flipY = pvFacing != clipboard.getVerticalFacing();
        }

        if (getOption(RANDOM_FLIPH, false)) {
            flipX = RANDOM.nextBoolean();
            flipZ = RANDOM.nextBoolean();
        }

        ReMappers reMappers = getOption(MAPPERS, ReMappers.EMPTY);
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
}
