package me.dags.copy;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.copy.block.Axis;
import me.dags.copy.block.Facing;
import me.dags.copy.clipboard.ClipboardOptions;
import me.dags.copy.clipboard.Selector;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Commands {

    @Permission("toolkit.copy")
    @Command(alias = "copy")
    public void copy(@Caller Player player) {
        Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);
        PlayerData data = CopyPasta.getInstance().getData(player);
        if (inHand.isPresent()) {
            FMT.info("Set copy wand to ").stress(inHand.get().getName()).tell(CopyPasta.CHAT_TYPE, player);
            data.clear();
            data.setWand(inHand.get());
            data.setSelector(new Selector());
            data.setOptions(new ClipboardOptions());
        } else {
            FMT.info("Removed copy wand").tell(player);
            data.clear();
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "range", parent = "copy")
    public void range(@Caller Player player, @One("range") int range) {
        Optional<Selector> selector = CopyPasta.getInstance().getData(player).getSelector();
        if (selector.isPresent()) {
            selector.get().setRange(Math.max(1, Math.min(range, 25)));
            FMT.info("Set range to ").stress(selector.get().getRange()).tell(CopyPasta.CHAT_TYPE, player);
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "reset", parent = "copy")
    public void reset(@Caller Player player) {
        FMT.info("Resetting copy wand").tell(CopyPasta.CHAT_TYPE, player);
        CopyPasta.getInstance().dropData(player);
    }

    @Permission("toolkit.copy")
    @Command(alias = "rotate", parent = "copy auto")
    public void autoRotate(@Caller Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().getData(player).getOptions();
        if (options.isPresent()) {
            options.get().setAutoRotate(!options.get().autoRotate());
            FMT.info("Auto-rotate: ").stress(options.get().autoRotate()).tell(CopyPasta.CHAT_TYPE, player);
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "flip", parent = "copy auto")
    public void autoFlip(@Caller Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().getData(player).getOptions();
        if (options.isPresent()) {
            options.get().setAutoFlip(!options.get().autoFlip());
            FMT.info("Auto-flip: ").stress(options.get().autoFlip()).tell(CopyPasta.CHAT_TYPE, player);
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "flip", parent = "copy")
    public void flip(@Caller Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().getData(player).getOptions();
        if (options.isPresent()) {
            Axis axis = Facing.facing(player).getAxis();
            if (axis == Axis.x) {
                options.get().setFlipX(!options.get().flipX());
                FMT.info("Flip X: ").stress(options.get().flipX()).tell(CopyPasta.CHAT_TYPE, player);
            } else if (axis == Axis.y) {
                options.get().setFlipY(!options.get().flipY());
                FMT.info("Flip Y: ").stress(options.get().flipY()).tell(CopyPasta.CHAT_TYPE, player);
            } else if (axis == Axis.z) {
                options.get().setFlipZ(!options.get().flipZ());
                FMT.info("Flip Z: ").stress(options.get().flipZ()).tell(CopyPasta.CHAT_TYPE, player);
            }
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "flip", parent = "copy random")
    public void randomFlip(@Caller Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().getData(player).getOptions();
        if (options.isPresent()) {
            Axis axis = Facing.facing(player).getAxis();
            if (axis == Axis.y) {
                options.get().setRandomFlipV(!options.get().randomFlipV());
                FMT.info("Randomly flip vertically: ").stress(options.get().randomFlipV()).tell(CopyPasta.CHAT_TYPE, player);
            } else {
                options.get().setRandomFlipH(!options.get().randomFlipH());
                FMT.info("Randomly flip horizontally: ").stress(options.get().randomFlipV()).tell(CopyPasta.CHAT_TYPE, player);
            }
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "rotate", parent = "copy random")
    public void randomRotate(@Caller Player player) {
        Optional<ClipboardOptions> options = CopyPasta.getInstance().getData(player).getOptions();
        if (options.isPresent()) {
            options.get().setRandomRotate(!options.get().randomRotate());
            FMT.info("Set random rotation: ").stress(options.get().randomRotate()).tell(CopyPasta.CHAT_TYPE, player);
        }
    }
}
