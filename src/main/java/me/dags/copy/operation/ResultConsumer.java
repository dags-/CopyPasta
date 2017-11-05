package me.dags.copy.operation;

import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public interface ResultConsumer {

    void accept(UUID owner, World world, BlockVolume result);
}
