package me.dags.copy.command;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.copy.brush.schematic.SchematicEntry;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public class SchemCommands {

    @Permission
    @Command("schem|sch <path> <weight>")
    public void schem(@Src Player player, SchematicEntry entry, double weight) {

    }
}
