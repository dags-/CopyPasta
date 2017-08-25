package me.dags.copy.brush.stencil;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.brush.Action;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.clipboard.Clipboard;
import me.dags.copy.brush.clipboard.ClipboardBrush;
import me.dags.copy.brush.option.Option;
import me.dags.copy.registry.brush.BrushSupplier;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"stencil"})
public class StencilBrush extends ClipboardBrush {

    public static final Option<Stencil> STENCIL = Option.of("stencil", Stencil.EMPTY);
    public static final Option<StencilPalette> PALETTE = Option.of("palette", StencilPalette
            .create().add(BlockTypes.WOOL.getDefaultState(), 0.1));

    @Override
    public String getPermission() {
        return "brush.stencil";
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        Stencil stencil = getOption(STENCIL);
        StencilPalette palette = getOption(PALETTE);

        if (!stencil.isPresent()) {
            return;
        }

        StencilVolume volume = new StencilVolume(stencil, palette);
        Clipboard clipboard = Clipboard.of(player, volume, stencil.getOffset(), true);
        setClipboard(clipboard);

        apply(player, pos, getHistory());
    }

    public static BrushSupplier supplier() {
        return player -> new StencilBrush();
    }
}
