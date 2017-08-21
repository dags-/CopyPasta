package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import me.dags.commandbus.fmt.Fmt;
import me.dags.copy.CopyPasta;
import me.dags.copy.Mappers;
import me.dags.copy.block.BlockUtils;
import me.dags.copy.block.property.Axis;
import me.dags.copy.block.property.Facing;
import me.dags.copy.block.state.State;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Action;
import me.dags.copy.brush.Aliases;
import me.dags.copy.operation.VolumeMapper;
import me.dags.copy.registry.option.Option;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"clipboard", "cb"})
public class ClipboardBrush extends AbstractBrush {

    private static final Random RANDOM = new Random();

    public static final Option FLIPX = Option.of("flip_x");
    public static final Option FLIPY = Option.of("flip_y");
    public static final Option FLIPZ = Option.of("flip_z");
    public static final Option AUTO_ROTATE = Option.of("auto_rotate");
    public static final Option RANDOM_ROTATE = Option.of("random_rotate");
    public static final Option AUTO_FLIP = Option.of("auto_flip");
    public static final Option RANDOM_FLIPH = Option.of("random_flip_h");
    public static final Option RANDOM_FLIPV = Option.of("random_flip_v");
    public static final Option AIR = Option.of("air");
    public static final Option SOLID_FOUNDATION = Option.of("foundation");
    public static final Option MAPPERS = Option.of("mappers");

    private SelectorBrush selector = new SelectorBrush(this);

    private Clipboard clipboard = null;

    public void commitSelection(Player player, Vector3i min, Vector3i max, Vector3i origin, int size) {
        Clipboard clipboard = Clipboard.of(player, min, max, origin);
        setClipboard(clipboard);
        Fmt.info("Copied ").stress(size).info(" blocks").tell(player);
    }

    @Override
    public String getPermission() {
        return "brush.clipboard";
    }

    @Override
    public void primary(Player player, Vector3i pos, Action action) {
        Optional<Clipboard> clipBoard = getClipboard();
        if (clipBoard.isPresent()) {
            if (action == Action.SECONDARY) {
                setClipboard(null);
                Fmt.info("Cleared clipboard").tell(player);
                return;
            }

            clipBoard.get().undo(player);
        } else {
            selector.primary(player, pos, action);
        }
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        Optional<Clipboard> clipBoard = getClipboard();
        if (clipBoard.isPresent()) {
            if (getOption(SOLID_FOUNDATION, false)) {
                pos = BlockUtils.findSolidFoundation(player.getWorld(), pos);
                if (pos == Vector3i.ZERO) {
                    return;
                }
            }

            VolumeMapper mapper = getMapper(clipBoard.get(), player);
            clipBoard.get().paste(player, pos, mapper, getOption(AIR, false), CopyPasta.getInstance().getCause(player));
        } else {
            selector.secondary(player, pos, action);
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

        if (getOption(RANDOM_FLIPV, false)) {
            flipY = RANDOM.nextBoolean();
        }

        List<State.Mapper> list = getOption(MAPPERS, Collections.emptyList());
        ImmutableList.Builder<State.Mapper> mappers = ImmutableList.builder();
        mappers.addAll(list);

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

    private Optional<Clipboard> getClipboard() {
        return Optional.ofNullable(clipboard);
    }
}
