package me.dags.copy;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.brush.Brush;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
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
        ItemType item = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem).orElse(ItemTypes.NONE);
        Optional<Brush> brush = CopyPasta.getInstance().getData(player).flatMap(data -> data.getBrush(item));
        if (brush.isPresent() && player.hasPermission(brush.get().getPermission())) {
            Vector3i target = targetPosition(player, brush.get().getRange());
            brush.get().primary(player, target);
            event.setCancelled(true);
        }
    }

    @Listener
    public void interactSecondary(InteractItemEvent.Secondary.MainHand event, @Root Player player) {
        ItemType item = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem).orElse(ItemTypes.NONE);
        Optional<Brush> brush = CopyPasta.getInstance().getData(player).flatMap(data -> data.getBrush(item));
        if (brush.isPresent() && player.hasPermission(brush.get().getPermission())) {
            Vector3i target = targetPosition(player, brush.get().getRange());
            brush.get().secondary(player, target);
            event.setCancelled(true);
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
