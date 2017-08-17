package me.dags.copy;

import me.dags.commandbus.annotation.*;
import me.dags.commandbus.fmt.Fmt;
import me.dags.copy.clipboard.ClipboardOptions;
import me.dags.copy.clipboard.Selector;
import me.dags.copy.property.Axis;
import me.dags.copy.property.Facing;
import me.dags.copy.state.State;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Commands {

    @Permission
    @Command(alias = "copy")
    @Description("Set your copy  wand to the current item")
    public void copy(@Src Player player) {
        Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);
        PlayerData data = CopyPasta.getInstance().ensureData(player);
        if (inHand.isPresent()) {
            Fmt.info("Set copy wand to ").stress(inHand.get().getName()).tell(CopyPasta.CHAT_TYPE, player);
            data.clear();
            data.setWand(inHand.get());
            data.setSelector(new Selector());
            data.setOptions(new ClipboardOptions());
        } else {
            Fmt.info("Removed copy wand").tell(player);
            data.clear();
        }
    }

    @Permission
    @Command(alias = "range", parent = "copy")
    @Description("Set the range of your copy wand")
    public void range(@Src Player player, @One("range") int range) {
        Optional<Selector> selector = CopyPasta.getInstance().ensureData(player).getSelector();
        if (selector.isPresent()) {
            selector.get().setRange(Math.max(1, Math.min(range, 25)));
            Fmt.info("Set range to ").stress(selector.get().getRange()).tell(CopyPasta.CHAT_TYPE, player);
        }
    }

    @Permission
    @Command(alias = "reset", parent = "copy")
    @Description("Clear your clipboard, history, selection and wand")
    public void reset(@Src Player player) {
        Fmt.info("Resetting copy wand").tell(CopyPasta.CHAT_TYPE, player);
        CopyPasta.getInstance().dropData(player);
    }

    @Permission
    @Command(alias = "air", parent = "copy")
    @Description("Toggle pasting of air blocks for your clipboard")
    public void pasteAir(@Src Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().ensureData(player).getOptions();
        if (options.isPresent()) {
            options.get().setPasteAir(!options.get().pasteAir());
            Fmt.info("Paste air: ").stress(options.get().pasteAir()).tell(CopyPasta.CHAT_TYPE, player);
        }
    }

    @Permission
    @Command(alias = "rotate", parent = "copy auto")
    @Description("Toggle auto-rotation of your clipboard")
    public void autoRotate(@Src Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().ensureData(player).getOptions();
        if (options.isPresent()) {
            options.get().setAutoRotate(!options.get().autoRotate());
            Fmt.info("Auto-rotate: ").stress(options.get().autoRotate()).tell(CopyPasta.CHAT_TYPE, player);
        }
    }

    @Permission
    @Command(alias = "flip", parent = "copy auto")
    @Description("Toggle the auto-flipping of your clipboard (when looking up/down)")
    public void autoFlip(@Src Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().ensureData(player).getOptions();
        if (options.isPresent()) {
            options.get().setAutoFlip(!options.get().autoFlip());
            Fmt.info("Auto-flip: ").stress(options.get().autoFlip()).tell(CopyPasta.CHAT_TYPE, player);
        }
    }

    @Permission
    @Command(alias = "flip", parent = "copy")
    @Description("Toggle flipping of your clipboard in the direction you are looking")
    public void flip(@Src Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().ensureData(player).getOptions();
        if (options.isPresent()) {
            Axis axis = Facing.getFacing(player).getAxis();
            if (axis == Axis.x) {
                options.get().setFlipX(!options.get().flipX());
                Fmt.info("Flip x: ").stress(options.get().flipX()).tell(CopyPasta.CHAT_TYPE, player);
            } else if (axis == Axis.y) {
                options.get().setFlipY(!options.get().flipY());
                Fmt.info("Flip y: ").stress(options.get().flipY()).tell(CopyPasta.CHAT_TYPE, player);
            } else if (axis == Axis.z) {
                options.get().setFlipZ(!options.get().flipZ());
                Fmt.info("Flip z: ").stress(options.get().flipZ()).tell(CopyPasta.CHAT_TYPE, player);
            }
        }
    }

    @Permission
    @Command(alias = "flip", parent = "copy random")
    @Description("Toggle random flipping of your clipboard for each paste")
    public void randomFlip(@Src Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().ensureData(player).getOptions();
        if (options.isPresent()) {
            Axis axis = Facing.getFacing(player).getAxis();
            if (axis == Axis.y) {
                options.get().setRandomFlipV(!options.get().randomFlipV());
                Fmt.info("Randomly flip vertically: ").stress(options.get().randomFlipV()).tell(CopyPasta.CHAT_TYPE, player);
            } else {
                options.get().setRandomFlipH(!options.get().randomFlipH());
                Fmt.info("Randomly flip horizontally: ").stress(options.get().randomFlipV()).tell(CopyPasta.CHAT_TYPE, player);
            }
        }
    }

    @Permission
    @Command(alias = "rotate", parent = "copy random")
    @Description("Toggle random rotation of your clipboard for each paste")
    public void randomRotate(@Src Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().ensureData(player).getOptions();
        if (options.isPresent()) {
            options.get().setRandomRotate(!options.get().randomRotate());
            Fmt.info("Set random rotation: ").stress(options.get().randomRotate()).tell(CopyPasta.CHAT_TYPE, player);
        }
    }

    @Permission
    @Command(alias = {"map", "replace"}, parent = "copy")
    @Description("Remap matching blocks/variants")
    public void replace(@Src Player player, String match, String replace) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().ensureData(player).getOptions();
        if (options.isPresent()) {
            State.Mapper mapper = State.mapper(match, replace);
            if (mapper.isPresent()) {
                options.get().addMapper(mapper);
                Fmt.info("Added mapper: ").stress("%s -> %s", match, replace).tell(player);
            } else {
                Fmt.error("Unable to parse mapper for %s -> %s", match, replace).tell(player);
            }
        }
    }

    @Permission
    @Command(alias = {"clear", "reset"}, parent = "copy map")
    @Description("Clear all block remaps")
    public void clearMappers(@Src Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().ensureData(player).getOptions();
        if (options.isPresent()) {
            options.get().clearMappers();
            Fmt.info("Cleared mappers").tell(player);
        }
    }
}
