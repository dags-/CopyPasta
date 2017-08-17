package me.dags.copy;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.clipboard.Clipboard;
import me.dags.copy.clipboard.Selector;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class EventListener {

    @Listener
    public void interactPrimary(InteractItemEvent.Primary.MainHand event, @Root Player player) {
        if (player.hasPermission("copypasta.use")) {
            Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);
            if (inHand.isPresent()) {
                Optional<ItemType> wand = CopyPasta.getInstance().ensureData(player).getWand();
                if (wand.isPresent() && wand.get() == inHand.get()) {
                    event.setCancelled(true);

                    Optional<Clipboard> clipBoard = CopyPasta.getInstance().ensureData(player).getClipboard();
                    if (clipBoard.isPresent()) {
                        if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                            Optional<Selector> selector = CopyPasta.getInstance().ensureData(player).getSelector();
                            selector.ifPresent(s -> s.reset(player));
                        } else {
                            clipBoard.get().undo(player);
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void interactSecondary(InteractItemEvent.Secondary.MainHand event, @Root Player player) {
        if (player.hasPermission("copypasta.use")) {
            Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);

            if (inHand.isPresent()) {
                Optional<PlayerData> data = CopyPasta.getInstance().getData(player);

                if (!data.isPresent() || data.get().isRateLimited()) {
                    return;
                }

                Optional<ItemType> wand = data.flatMap(PlayerData::getWand);
                if (wand.isPresent() && wand.get() == inHand.get()) {
                    event.setCancelled(true);

                    Selector selector = data.get().ensureSelector();
                    Optional<Clipboard> clipBoard = data.get().getClipboard();
                    Vector3i target = targetPosition(player, selector.getRange());

                    if (clipBoard.isPresent()) {
                        clipBoard.get().paste(player, target, CopyPasta.getInstance().getCause(player));
                    } else {
                        selector.pos(player, target);
                    }
                }
            }
        }
    }

    @Listener
    public void disconnect(ClientConnectionEvent.Disconnect event) {
        CopyPasta.getInstance().dropData(event.getTargetEntity());
    }

    private static Vector3i targetPosition(Player player, int limit) {
        Optional<BlockRayHit<World>> hit = BlockRay.from(player)
                .stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
                .distanceLimit(limit)
                .end();

        return hit.map(BlockRayHit::getBlockPosition).orElse(Vector3i.ZERO);
    }
}
