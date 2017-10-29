package me.dags.copy.operation;

import org.spongepowered.api.world.extent.BlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public interface OperationVisitor {

    default int visit(BlockVolume volume, int x, int y, int z) {
        return 1;
    }

    default int visit(BlockVolume volume, int x, int z) {
        return 1;
    }
}
