package me.dags.copy.brush.stencil;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.brush.Action;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.clipboard.Clipboard;
import me.dags.copy.brush.clipboard.ClipboardBrush;
import me.dags.copy.brush.option.Option;
import me.dags.copy.registry.brush.BrushSupplier;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"stencil"})
public class StencilBrush extends ClipboardBrush {

    public static final Option<Stencil> STENCIL = Option.of("stencil", Stencil.EMPTY);
    public static final Option<BlockState> MATERIAL = Option.of("material", BlockState.class, BlockTypes.SAND::getDefaultState);

    @Override
    public String getPermission() {
        return "brush.stencil";
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        Stencil stencil = getOption(STENCIL);
        BlockState material = getOption(MATERIAL);

        if (!stencil.isPresent() || material.getType() == BlockTypes.AIR) {
            return;
        }

        StencilVolume volume = new StencilVolume(stencil, material);
        Clipboard clipboard = Clipboard.of(player, volume, stencil.getCenter().mul(-1), true);
        setClipboard(clipboard);

        apply(player, pos, getHistory());
    }

    public static BrushSupplier supplier() {
        return player -> new StencilBrush();
    }
}
