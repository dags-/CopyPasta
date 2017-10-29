package me.dags.copy.operation.visitor;

import org.spongepowered.api.world.extent.BlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public interface Visitor2D {

    int visit(BlockVolume volume, int x, int z);
}
