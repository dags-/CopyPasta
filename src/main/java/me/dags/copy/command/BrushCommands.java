package me.dags.copy.command;

import me.dags.commandbus.annotation.*;
import me.dags.commandbus.command.Flags;
import me.dags.commandbus.fmt.PagFormatter;
import me.dags.copy.PlayerData;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Parsable;
import me.dags.copy.brush.option.Value;
import me.dags.copy.registry.brush.BrushRegistry;
import me.dags.copy.registry.brush.BrushType;
import me.dags.copy.util.fmt;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.action.TextActions;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class BrushCommands {

    static  <T extends Brush> Optional<T> getBrush(Player player, Class<T> type) {
        Optional<Brush> brush = getBrush(player);
        if (brush.isPresent()) {
            if (type.isInstance(brush.get())) {
                return brush.map(type::cast);
            }
            fmt.error("Current brush is not a %s", type.getSimpleName());
        }
        return Optional.empty();
    }

    static Optional<Brush> getBrush(Player player) {
        Optional<ItemType> item = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);
        if (!item.isPresent()) {
            fmt.error("You are not holding an item").tell(player);
            return Optional.empty();
        }

        Optional<Brush> brush = PlayerManager.getInstance().get(player).flatMap(data -> data.getBrush(player));
        if (!brush.isPresent()) {
            fmt.error("You have not bound a brush to item %s", item.get().getName()).tell(player);
            return Optional.empty();
        }

        return brush;
    }

    @Permission
    @Command("wand|w <wand>")
    @Description("Bind the <wand> to your held item")
    public void brush(@Src Player player, BrushType type) {
        ItemType item = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem).orElse(ItemTypes.NONE);
        PlayerData data = PlayerManager.getInstance().must(player);
        if (item == ItemTypes.NONE) {
            fmt.sub("You are not holding an item").tell(player);
        } else if (player.hasPermission(type.getPermission())) {
            Optional<Brush> brush = type.create(player);
            if (brush.isPresent()) {
                data.setBrush(item, brush.get());
                fmt.info("Set brush ").stress(type).info(" to item ").stress(item.getName()).tell(player);
            }
        } else {
            fmt.error("You do not have permission to use that wand").tell(player);
        }
    }

    @Command("wand|w preset <name>")
    public void preset(@Src Player player) {

    }

    @Permission
    @Command("wand|w reset")
    @Description("Reset all options for your current wand to their defaults")
    public void reset(@Src Player player) {
        Optional<Brush> brush = getBrush(player);
        if (brush.isPresent()) {
            fmt.info("Reset brush ").stress(brush.get().getType()).tell(player);
            brush.get().getOptions().reset();
        }
    }

    @Permission
    @Command("wand|w remove")
    @Description("Remove the <wand> from your held item")
    public void remove(@Src Player player) {
        ItemType item = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem).orElse(ItemTypes.NONE);
        PlayerData data = PlayerManager.getInstance().must(player);
        if (item == ItemTypes.NONE) {
            fmt.sub("You are not holding an item").tell(player);
        } else {
            Optional<Brush> brush = data.getBrush(item);
            if (brush.isPresent()) {
                data.removeBrush(item);
                fmt.info("Removing wand ").stress(brush.get().getType())
                        .info(" from item ").stress(item.getType().getName()).tell(player);
            } else {
                fmt.error("You do not have a wand bound to that item").tell(player);
            }
            data.removeBrush(item);
        }
    }

    @Flag("a")
    @Permission
    @Command("wand|w list")
    @Description("List all wand types")
    public void list(@Src CommandSource source, Flags flags) {
        boolean aliases = flags.has("a");
        PagFormatter page = fmt.page();
        if (aliases) {
            BrushRegistry.getInstance().forEachAlias((s, type) -> page.line().subdued(" - ").stress(s).info(" (%s)", type));
        } else {
            BrushRegistry.getInstance().forEachUnique((s, type) -> page.line().subdued(" - ").stress(s));
        }
        page.sort(true).build().sendTo(source);
    }

    @Permission
    @Command("wand|w parse <options>")
    @Description("Parse a raw options string")
    public void parse(@Src Player player, @Join String raw) {
        Optional<Brush> brush = getBrush(player);
        if (brush.isPresent() && brush.get() instanceof Parsable) {
            ((Parsable) brush.get()).parse(raw);
            fmt.info("Parsed options:").tell(player);
            options(player);
        }
    }

    @Permission
    @Command("wand|w options")
    @Description("List all options and their values for the current wand")
    public void options(@Src Player player) {
        Optional<Brush> brush = getBrush(player);
        if (brush.isPresent()) {
            PagFormatter page = fmt.page();
            page.title().stress("%s Options:", brush.get().getType());
            Brush instance = brush.get();
            BrushType type = brush.get().getType();
            for (Option<?> option : type.getOptions()) {
                Object value = instance.getOption(option);
                page.line().subdued(" - ")
                        .stress(option).info("=").stress(value)
                        .info(" (%s)", option.getUsage())
                        .action(TextActions.suggestCommand("/set " + option + " "));
            }
            page.sort(true).build().sendTo(player);
        }
    }

    @Permission
    @Command("set|s <option> <value>")
    @Description("Set an option for your current wand")
    public void option(@Src Player player, Option<?> option, Value<?> value) {
        Optional<Brush> brush = getBrush(player);
        if (brush.isPresent()) {
            BrushType type = brush.get().getType();
            brush.get().setOption(option, value.get());
            fmt.info("Set ").stress(option).info("=").stress(value).info(" for brush ").stress(type).tell(player);
        }
    }
}
