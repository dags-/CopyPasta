package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import java.util.Random;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.block.Mappers;
import me.dags.copy.block.property.Axis;
import me.dags.copy.block.property.Facing;
import me.dags.copy.block.state.State;
import me.dags.copy.block.volume.VolumeMapper;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Action;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.History;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.value.Flip;
import me.dags.copy.brush.option.value.MapperSet;
import me.dags.copy.brush.option.value.Translation;
import me.dags.copy.operation.modifier.Filter;
import me.dags.copy.operation.modifier.Translate;
import me.dags.copy.registry.brush.BrushSupplier;
import me.dags.copy.util.fmt;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"clipboard", "copy"})
public class ClipboardBrush extends AbstractBrush {

    protected static final Random RANDOM = new Random();

    public static final Option<Flip> FLIP = Flip.OPTION;
    public static final Option<Boolean> AUTO_ROTATE = Option.of("rotate.auto", true);
    public static final Option<Boolean> RANDOM_ROTATE = Option.of("rotate.random", false);
    public static final Option<Boolean> PASTE_AIR = Option.of("air.paste", false);
    public static final Option<Boolean> REPLACE_AIR = Option.of("air.replace", false);
    public static final Option<Vector3i> PASTE_OFFSET = Option.of("offset", Vector3i.ZERO);
    public static final Option<Translation> TRANSLATE = Option.of("translate", Translation.NONE);
    public static final Option<MapperSet> MAPPER_SET = MapperSet.OPTION;

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
            if (PlayerManager.getInstance().must(player).isOperating()) {
                fmt.sub("Pasting...").tell(CopyPasta.NOTICE_TYPE, player);
            }
        } else {
            selector.secondary(player, pos, action);
        }
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        if (clipboard.isPresent()) {
            PlayerManager.getInstance().must(player).setOperating(true);
            Vector3i offset = getOption(PASTE_OFFSET);
            Vector3i position = pos.add(offset);
            Translation mode = getOption(TRANSLATE);
            Filter from = Filter.replaceAir(getOption(REPLACE_AIR));
            Filter to = Filter.pasteAir(getOption(PASTE_AIR));
            Translate translate = mode.getModifier(pos, offset);
            VolumeMapper mapper = getMapper(clipboard, player);
            clipboard.paste(player, history, position, mapper, from, to, translate);
        }
    }

    public void setClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    private VolumeMapper getMapper(Clipboard clipboard, Player player) {
        Facing phFacing = Facing.getHorizontal(player);
        Facing pvFacing = Facing.getVertical(player);

        int angle = 0;
        Flip flip = getOption(FLIP);
        boolean flipX = flip.flipX();
        boolean flipY = flip.flipY();
        boolean flipZ = flip.flipZ();

        if (getOption(AUTO_ROTATE)) {
            angle = clipboard.getHorizontalFacing().angle(phFacing, Axis.y);
        }

        if (getOption(RANDOM_ROTATE)) {
            int turns = RANDOM.nextInt(4);
            angle = turns * 90;
        }

        if (flip.auto() && pvFacing != Facing.none && clipboard.getVerticalFacing() != Facing.none) {
            flipY = pvFacing != clipboard.getVerticalFacing();
        }

        if (flip.random()) {
            flipX = RANDOM.nextBoolean();
            flipZ = RANDOM.nextBoolean();
        }

        MapperSet mapperSet = getOption(ClipboardBrush.MAPPER_SET);
        ImmutableList.Builder<State.Mapper> mappers = ImmutableList.builder();
        mappers.addAll(mapperSet);

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

        return new VolumeMapper(clipboard.getOrigin(), angle, flipX, flipY, flipZ, mappers.build());
    }

    public static BrushSupplier supplier() {
        return player -> new ClipboardBrush();
    }
}
