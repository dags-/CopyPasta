package me.dags.copy.command;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.copy.brush.stencil.StencilBrush;
import me.dags.copy.brush.stencil.StencilPalette;
import me.dags.copy.util.fmt;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class StencilCommands {

    @Permission
    @Command("stencil|st palette clear|reset")
    public void stencilClear(@Src Player player) {
        Optional<StencilBrush> brush = BrushCommands.getBrush(player, StencilBrush.class);
        if (brush.isPresent()) {
            brush.get().setOption(StencilBrush.PALETTE, StencilPalette.create());
        }
    }

    @Permission
    @Command("stencil|st palette <blockstate> <weight>")
    public void stencilMaterial(@Src Player player, BlockState block) {
        stencilMaterial(player, block, 1D);
    }

    @Permission
    @Command("stencil|st palette <blockstate> <weight>")
    public void stencilMaterial(@Src Player player, BlockState block, double weight) {
        Optional<StencilBrush> brush = BrushCommands.getBrush(player, StencilBrush.class);
        if (brush.isPresent()) {
            brush.get().getOption(StencilBrush.PALETTE).add(block, weight);
            fmt.info("Added ").stress(block).info("=").stress(weight).info(" to your palette").tell(player);
        }
    }
}
