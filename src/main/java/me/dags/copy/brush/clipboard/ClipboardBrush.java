package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import me.dags.copy.CopyPasta;
import me.dags.copy.Mappers;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.registry.option.BrushOption;
import me.dags.copy.registry.option.BrushOptionRegistry;
import me.dags.copy.operation.VolumeMapper;
import me.dags.copy.block.property.Axis;
import me.dags.copy.block.property.Facing;
import me.dags.copy.brush.Action;
import me.dags.copy.block.state.State;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * @author dags <dags@dags.me>
 */
public class ClipboardBrush extends AbstractBrush {

    private static final Random RANDOM = new Random();

    public static final BrushOption FLIPX = BrushOption.of("clipboard.flipx");
    public static final BrushOption FLIPY = BrushOption.of("clipboard.flipx");
    public static final BrushOption FLIPZ = BrushOption.of("clipboard.flipx");
    public static final BrushOption AUTO_ROTATE = BrushOption.of("clipboard.rotate.auto");
    public static final BrushOption RANDOM_ROTATE = BrushOption.of("clipboard.rotate.random");
    public static final BrushOption AUTO_FLIP = BrushOption.of("clipboard.flip.auto");
    public static final BrushOption RANDOM_FLIPH = BrushOption.of("clipboard.fliph.random");
    public static final BrushOption RANDOM_FLIPV = BrushOption.of("clipboard.flipv.random");
    public static final BrushOption AIR = BrushOption.of("clipboard.air");
    public static final BrushOption MAPPERS = BrushOption.of("clipboard.mappers");

    static {
        BrushOptionRegistry.getInstance().register(FLIPX);
        BrushOptionRegistry.getInstance().register(FLIPY);
        BrushOptionRegistry.getInstance().register(FLIPZ);
        BrushOptionRegistry.getInstance().register(AUTO_ROTATE);
        BrushOptionRegistry.getInstance().register(RANDOM_ROTATE);
        BrushOptionRegistry.getInstance().register(AUTO_FLIP);
        BrushOptionRegistry.getInstance().register(RANDOM_FLIPH);
        BrushOptionRegistry.getInstance().register(RANDOM_FLIPV);
        BrushOptionRegistry.getInstance().register(AIR);
        BrushOptionRegistry.getInstance().register(MAPPERS);
    }

    private SelectorBrush selector = new SelectorBrush(this);
    private Clipboard clipboard = null;

    @Override
    public String getPermission() {
        return "brush.clipboard";
    }

    @Override
    public void primary(Player player, Vector3i pos, Action action) {
        Optional<Clipboard> clipBoard = getClipboard();
        if (clipBoard.isPresent()) {
            if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                selector.primary(player, pos);
            } else {
                clipBoard.get().undo(player);
            }
        }
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        Optional<Clipboard> clipBoard = getClipboard();

        if (clipBoard.isPresent()) {
            VolumeMapper mapper = getMapper(clipBoard.get(), player);
            clipBoard.get().paste(player, pos, mapper, getOption(AIR, false), CopyPasta.getInstance().getCause(player));
        } else {
            selector.secondary(player, pos);
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

        if (getOption(AUTO_ROTATE, false)) {
            angle = clipboard.getHorizontalFacing().angle(phFacing, Axis.y);
        }

        if (getOption(RANDOM_ROTATE, false)) {
            int turns = RANDOM.nextInt(4);
            angle = turns * 90;
        }

        if (getOption(AUTO_FLIP, false) && pvFacing != Facing.none && clipboard.getVerticalFacing() != Facing.none) {
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
