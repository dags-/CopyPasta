package me.dags.copy.operation.modifier;

import com.flowpowered.math.vector.Vector3i;
import java.util.function.BiConsumer;
import me.dags.copy.block.BlockUtils;
import me.dags.copy.block.Snapshot;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface Translate extends BiConsumer<World, Snapshot> {

    Translate NONE = (w, s) -> {};

    static Translate overlay(Vector3i position, Vector3i offset) {
        return (w, s) -> {
            Vector3i surface = BlockUtils.findSolidFoundation(w, s.getPosition());

            int x = s.getPosition().getX() + offset.getX();
            int y = surface.getY() + (s.getPosition().getY() - position.getY()) + offset.getY();
            int z = s.getPosition().getZ() + offset.getZ();

            s.setPosition(new Vector3i(x, y, z));
        };
    }

    static Translate surface(Vector3i position, Vector3i offset) {
        return new Translate() {

            private int surfaceY = -1;

            @Override
            public void accept(World world, Snapshot snapshot) {
                if (surfaceY == -1) {
                    surfaceY = BlockUtils.findSurfaceY(world, position);
                }

                int x = snapshot.getPosition().getX() + offset.getX();
                int y = surfaceY + (snapshot.getPosition().getY() - position.getY()) + offset.getY();
                int z = snapshot.getPosition().getZ() + offset.getZ();

                snapshot.setPosition(new Vector3i(x, y, z));
            }
        };
    }
}
