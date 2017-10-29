package me.dags.copy.command;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.copy.brush.schematic.SchematicBrush;
import me.dags.copy.brush.schematic.SchematicList;
import me.dags.copy.registry.schematic.SchematicEntry;
import me.dags.copy.util.fmt;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class SchemCommands {

    @Permission
    @Command("schem|sch clear")
    public void clear(@Src Player player) {
        Optional<SchematicBrush> brush = BrushCommands.getBrush(player, SchematicBrush.class);
        if (brush.isPresent()) {
            brush.get().setOption(SchematicBrush.SCHEMATICS, new SchematicList());
            fmt.info("SchematicList has been init").tell(player);
        }
    }

    @Permission
    @Command("schem|sch <path> <weight>")
    public void schem(@Src Player player, Collection<SchematicEntry> entries) {
        schem(player, entries, 1D);
    }

    @Permission
    @Command("schem|sch <path> <weight>")
    public void schem(@Src Player player, Collection<SchematicEntry> entries, double weight) {
        Optional<SchematicBrush> brush = BrushCommands.getBrush(player, SchematicBrush.class);
        if (brush.isPresent()) {
            SchematicList list = brush.get().mustOption(SchematicBrush.SCHEMATICS);
            for (SchematicEntry entry : entries) {
                list.add(entry, weight);
                fmt.info("Added schematic ").stress(entry.getPath()).info(" to your schematic list").tell(player);
            }
        }
    }
}
