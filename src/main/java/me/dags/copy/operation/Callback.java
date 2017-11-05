package me.dags.copy.operation;

import com.google.common.util.concurrent.FutureCallback;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.util.fmt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;
import sun.plugin.dom.exception.InvalidStateException;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Callback implements FutureCallback<BlockVolume> {

    private final UUID owner;
    private final WeakReference<World> world;
    private final ResultConsumer callback;

    private Callback(UUID owner, World world, ResultConsumer callback) {
        this.owner = owner;
        this.callback = callback;
        this.world = new WeakReference<>(world);
    }

    @Override
    public void onSuccess(@Nullable BlockVolume result) {
        if (result == null) {
            onFailure(new NullPointerException("Returned BlockVolume was null"));
            return;
        }

        World world = this.world.get();
        if (world == null) {
            onFailure(new InvalidStateException("Lost reference to world"));
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

    public static Callback of(Player player, ResultConsumer callback) {
        return new Callback(player.getUniqueId(), player.getWorld(), callback);
    }
}
