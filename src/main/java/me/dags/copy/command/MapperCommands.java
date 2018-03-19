package me.dags.copy.command;

import java.util.Optional;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.copy.block.state.State;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.value.MapperSet;
import me.dags.copy.util.fmt;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public class MapperCommands {

    @Command("mapper|map clear")
    @Permission("copypasta.command.map")
    @Description("Remove any re-mappers assigned to the wand")
    public void clear(@Src Player player) {
        Optional<Brush> brush = getMapperBrush(player);
        if (brush.isPresent()) {
            brush.get().setOption(MapperSet.OPTION, new MapperSet());
            fmt.info("Cleared your re-mappers for brush ").stress(brush.get().getType()).tell(player);
        }
    }

    @Command("mapper|map")
    @Permission("copypasta.command.map")
    @Description("Add a new re-mapper to the wand")
    public void mapper(@Src Player player, State.Mapper mapper) {
        Optional<Brush> brush = getMapperBrush(player);
        if (brush.isPresent()) {
            brush.get().getOption(MapperSet.OPTION).add(mapper);
            fmt.info("Added re-mapper to brush ").stress(brush.get().getType()).tell(player);
        }
    }

    private static Optional<Brush> getMapperBrush(Player player) {
        Optional<Brush> brush = BrushCommands.getBrush(player, Brush.class);
        if (brush.isPresent()) {
            if (brush.get().supports(MapperSet.OPTION)) {
                return brush;
            } else {
                fmt.error("Brush ").stress(brush.get().getType()).error(" doesn't support remapping").tell(player);
            }
        }
        return Optional.empty();
    }
}
