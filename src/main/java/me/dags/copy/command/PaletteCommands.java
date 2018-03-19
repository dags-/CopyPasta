package me.dags.copy.command;

import java.util.Optional;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.value.Palette;
import me.dags.copy.util.fmt;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public class PaletteCommands {

    @Command("palette clear")
    @Permission("copypasta.command.palette.clear")
    @Description("Remove all materials from the current wand's palette")
    public void paletteClear(@Src Player player) {
        Optional<Brush> brush = getPaletteBrush(player);
        if (brush.isPresent()) {
            fmt.info("Cleared your palette").tell(player);
            brush.get().setOption(Palette.OPTION, Palette.create());
        }
    }

    @Command("palette reset")
    @Permission("copypasta.command.palette.reset")
    @Description("Reset the current wand's palette to wool")
    public void paletteReset(@Src Player player) {
        Optional<Brush> brush = getPaletteBrush(player);
        if (brush.isPresent()) {
            fmt.info("Reset your palette").tell(player);
            brush.get().setOption(Palette.OPTION, Palette.createDefault());
        }
    }

    @Command("palette <blockstate> <weight>")
    @Permission("copypasta.command.palette.add")
    @Description("Add a material to the current wand's palette")
    public void stencilPalette(@Src Player player, BlockState block) {
        stencilMaterial(player, block, 1D);
    }

    @Command("palette <blockstate> <weight>")
    @Permission("copypasta.command.palette.add")
    @Description("Add a weighted material to the current wand's palette")
    public void stencilMaterial(@Src Player player, BlockState block, double weight) {
        Optional<Brush> brush = getPaletteBrush(player);
        if (brush.isPresent()) {
            brush.get().mustOption(Palette.OPTION).add(block, weight);
            fmt.info("Added ").stress(block).info("=").stress(weight).info(" to your palette").tell(player);
        }
    }

    private static Optional<Brush> getPaletteBrush(Player player) {
        Optional<Brush> brush = BrushCommands.getBrush(player, Brush.class);
        if (brush.isPresent()) {
            if (brush.get().supports(Palette.OPTION)) {
                return brush;
            } else {
                fmt.error("Brush ").stress(brush.get().getType()).error(" doesn't support a palette").tell(player);
            }
        }
        return Optional.empty();
    }
}
