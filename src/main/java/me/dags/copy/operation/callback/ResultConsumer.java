package me.dags.copy.operation.callback;

import me.dags.copy.block.volume.BufferView;
import org.spongepowered.api.world.World;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public interface ResultConsumer {

    void accept(UUID owner, World world, BufferView result);
}
