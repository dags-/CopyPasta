package me.dags.copy.command;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Src;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerData;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Value;
import me.dags.copy.fmt;
import me.dags.copy.registry.brush.BrushType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class BrushCommands {

    private Optional<Brush> getBrush(Player player) {
        Optional<ItemType> item = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);
        if (!item.isPresent()) {
            fmt.error("You are not holding an item").tell(player);
            return Optional.empty();
        }

        Optional<Brush> brush = CopyPasta.getInstance().getData(player).flatMap(data -> data.getBrush(item.get()));
        if (!brush.isPresent()) {
            fmt.error("You have not bound a brush to item %s", item.get().getName()).tell(player);
            return Optional.empty();
        }

        return brush;
    }

    @Command("copy <brush>")
    public void brush(@Src Player player, BrushType type) {
        ItemType item = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem).orElse(ItemTypes.NONE);
        PlayerData data = CopyPasta.getInstance().ensureData(player);

        if (item == ItemTypes.NONE) {
            data.removeBrush(type.getType());
            fmt.info("Removed %s brush", type.getName()).tell(player);
        } else {
            Optional<Brush> brush = type.create();
            if (brush.isPresent()) {
                data.resetBrush(brush.get(), item);
                fmt.info("Assigned %s brush to %s", type.getName(), item.getName()).tell(player);
            }
        }
    }

    @Command("copy <option> <value>")
    public void option(@Src Player player, Option<?> option, Value<?> value) {
        Optional<Brush> brush = getBrush(player);
        if (brush.isPresent()) {
            BrushType type = brush.get().getType();
            brush.get().setOption(option, value.getValue());
            fmt.info("Set ").stress(option).info("=").stress(value).info(" for brush ").stress(type).tell(player);
        }
    }
}
