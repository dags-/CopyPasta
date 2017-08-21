package me.dags.copy.registry.schematic;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.schematic.Schematic;

/**
 * @author dags <dags@dags.me>
 */
public class CachedSchematic {

    private final ImmutableBlockVolume volume;
    private final Vector3i origin;

    private CachedSchematic(ImmutableBlockVolume volume, Vector3i origin) {
        this.volume = volume;
        this.origin = origin;
    }

    public ImmutableBlockVolume getVolume() {
        return volume;
    }

    public Vector3i getOrigin() {
        return origin;
    }

    public static CachedSchematic of(Schematic schematic, Vector3i origin) {
        return new CachedSchematic(schematic.getImmutableBlockCopy(), origin);
    }
}
