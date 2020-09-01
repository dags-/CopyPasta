package me.dags.copy;

import com.flowpowered.math.vector.Vector3i;
import java.util.Optional;
import me.dags.copy.brush.Brush;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class EventListener {

    @Listener
    public void interactPrimary(InteractItemEvent.Primary.MainHand event, @Root Player player) {
        Optional<PlayerData> data = PlayerManager.getInstance().get(player);
        if (data.isPresent() && data.get().hasCooledDown()) {
            Optional<Brush> brush = data.get().getBrush(player);
            if (brush.isPresent() && player.hasPermission(brush.get().getPermission())) {
                try {
                    Vector3i target = targetPosition(player, brush.get().getRange());
                    brush.get().primary(player, target);
                    event.setCancelled(true);
                } catch (Throwable t) {
                    PlayerManager.getInstance().handle(player, t);
                }
            }
        }
    }

    @Listener
    public void interactSecondary(InteractItemEvent.Secondary.MainHand event, @Root Player player) {
        Optional<PlayerData> data = PlayerManager.getInstance().get(player);
        if (data.isPresent() && data.get().hasCooledDown()) {
            Optional<Brush> brush = data.get().getBrush(player);
            if (brush.isPresent() && player.hasPermission(brush.get().getPermission())) {
                try {
                    Vector3i target = targetPosition(player, brush.get().getRange());
                    brush.get().secondary(player, target);
                    event.setCancelled(true);
                } catch (Throwable t) {
                    PlayerManager.getInstance().handle(player, t);
                }
            }
        }
    }

    @Listener
    public void disconnect(ClientConnectionEvent.Disconnect event) {
        PlayerManager.getInstance().drop(event.getTargetEntity());
    }

    private static Vector3i targetPosition(Player player, int limit) {
        Optional<BlockRayHit<World>> hit = BlockRay.from(player)
                .stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
                .distanceLimit(limit)
                .end();

        return hit.map(BlockRayHit::getBlockPosition).orElse(Vector3i.ZERO);
    }
}
