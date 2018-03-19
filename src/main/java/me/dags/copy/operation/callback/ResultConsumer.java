package me.dags.copy.operation.callback;

import java.util.UUID;
import me.dags.copy.block.volume.BufferView;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface ResultConsumer {

    void accept(UUID owner, World world, BufferView result);
}
