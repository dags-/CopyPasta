package me.dags.copy.operation.callback;

import com.google.common.util.concurrent.FutureCallback;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.block.volume.BufferView;
import me.dags.copy.brush.History;
import me.dags.copy.operation.PlaceOperation;
import me.dags.copy.operation.modifier.Filter;
import me.dags.copy.operation.modifier.Translate;
import me.dags.copy.operation.phase.Apply;
import me.dags.copy.operation.phase.Calculate;
import me.dags.copy.operation.phase.Test;
import me.dags.copy.util.fmt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Callback implements FutureCallback<BufferView> {

    private final UUID owner;
    private final WeakReference<World> world;
    private final ResultConsumer callback;

    private Callback(UUID owner, World world, ResultConsumer callback) {
        this.owner = owner;
        this.callback = callback;
        this.world = new WeakReference<>(world);
    }

    @Override
    public void onSuccess(@Nullable BufferView result) {
        if (result == null) {
            onFailure(new NullPointerException("Returned BlockVolume was null"));
            return;
        }

        World world = this.world.get();
        if (world == null) {
            onFailure(new IllegalStateException("Lost reference to world"));
            return;
        }

        callback.accept(owner, world, result);
    }

    @Override
    public void onFailure(Throwable t) {
        CopyPasta.getInstance().submitSync(() -> {
            t.printStackTrace();

            Sponge.getServer().getPlayer(owner).ifPresent(player -> {
                PlayerManager.getInstance().get(player).ifPresent(data -> data.setOperating(false));
                fmt.error("An error occurred during operation, see console for details").tell(player);
            });
        });
    }

    public static Callback of(Player player, History history, Filter fromFilter, Filter toFilter, Translate transform) {
        return new Callback(player.getUniqueId(), player.getWorld(), (owner, world, result) -> {
            Calculate calculate = new Calculate(world, result, fromFilter, toFilter, transform);
            Test test = new Test(owner, world, result);
            Apply apply = new Apply(world, result, history);
            PlaceOperation place = new PlaceOperation(owner, calculate, test, apply);
            CopyPasta.getInstance().getOperationManager().queueOperation(place);
        });
    }
}
