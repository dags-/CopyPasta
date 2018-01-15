package me.dags.copy.operation.phase;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.block.BlockUtils;
import me.dags.copy.block.volume.Snapshot;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface Modifier {

    void apply(World world, Snapshot snapshot);

    Modifier NONE = (w, s) -> {};

    static Modifier overlay(Vector3i position, Vector3i offset) {
        return (w, s) -> {
            Vector3i surface = BlockUtils.findSolidFoundation(w, s.getPosition());
            int x = s.getPosition().getX() + offset.getX();
            int y = surface.getY() + position.getY() - s.getPosition().getY();
            int z = s.getPosition().getZ() + offset.getZ();
            s.setPosition(new Vector3i(x, y, z));
        };
    }

    static Modifier foundation(Vector3i position, Vector3i offset) {
        return new Modifier() {

            private int surfaceY = -1;

            @Override
            public void apply(World world, Snapshot snapshot) {
                if (surfaceY == -1) {
                    surfaceY = BlockUtils.findSolidFoundation(world, position).getY();
                }

                int x = snapshot.getPosition().getX() + offset.getX();
                int y = surfaceY + position.getY() - snapshot.getPosition().getY() + offset.getY();
                int z = snapshot.getPosition().getZ() + offset.getZ();
                snapshot.setPosition(new Vector3i(x, y, z));
            }
        };
    }
}
