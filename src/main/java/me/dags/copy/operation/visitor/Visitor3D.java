package me.dags.copy.operation.visitor;

import org.spongepowered.api.world.extent.BlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public interface Visitor3D {

    int visit(BlockVolume volume, int x, int y, int z);
}
