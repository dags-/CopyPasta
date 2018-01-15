package me.dags.copy.brush.replace;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import me.dags.copy.CopyPasta;
import me.dags.copy.block.state.State;
import me.dags.copy.block.volume.VolumeMapper;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.History;
import me.dags.copy.brush.MapperSet;
import me.dags.copy.brush.option.Checks;
import me.dags.copy.brush.option.Option;
import me.dags.copy.operation.callback.Callback;
import me.dags.copy.operation.phase.Calculate;
import me.dags.copy.operation.phase.Modifier;
import me.dags.copy.registry.brush.BrushSupplier;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"replace"})
public class ReplaceBrush extends AbstractBrush {

    public static final Option<Integer> RADIUS = Option.of("radius", 8, Checks.range(1, 48));
    public static final Option<MapperSet> MAPPERS = MapperSet.OPTION;

    private ReplaceBrush() {
        super(5);
    }

    @Override
    public String getPermission() {
        return "brush.replace";
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        int radius = getOption(RADIUS);
        MapperSet mappers = getOption(MAPPERS);
        ImmutableList<State.Mapper> rules = ImmutableList.copyOf(mappers);

        Extent extent = player.getWorld();
        Vector3i min = pos.sub(radius, radius, radius);
        Vector3i max = pos.add(radius, radius, radius);

        min = min.max(min.getX(), extent.getBlockMin().getY(), min.getZ());
        max = max.min(max.getX(), extent.getBlockMax().getY(), max.getZ());

        Vector3i position = min;
        BlockVolume relativeView = extent.getBlockView(min, max).getRelativeBlockView();
        ImmutableBlockVolume source = relativeView.getImmutableBlockCopy();

        VolumeMapper mapper = new VolumeMapper(0, false, false, false, Calculate.ANY, rules);
        Callback callback = Callback.place(player, history, Modifier.NONE);
        Runnable task = mapper.createTask(source, position, player.getUniqueId(), callback);
        CopyPasta.getInstance().submitAsync(task);
    }

    public static BrushSupplier supplier() {
        return p -> new ReplaceBrush();
    }
}
