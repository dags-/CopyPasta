package me.dags.copy.registry.schematic;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.block.property.Facing;
import me.dags.copy.brush.clipboard.Clipboard;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.schematic.Schematic;

/**
 * @author dags <dags@dags.me>
 */
public class CachedSchematic extends Clipboard {

    public static final String FACING_H = "Horizontal Facing";
    public static final String FACING_V = "Vertical Facing";

    private CachedSchematic(ImmutableBlockVolume volume, Vector3i origin, Facing horizontal, Facing vertical) {
        super(volume, origin, horizontal, vertical);
    }

    public static CachedSchematic of(Schematic schematic, Vector3i origin, Facing horizontal, Facing vertical) {
        return new CachedSchematic(schematic.getImmutableBlockCopy(), origin, horizontal, vertical);
    }
}
