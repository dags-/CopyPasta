package me.dags.copy.brush.replace;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.block.state.State;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.History;
import me.dags.copy.brush.Mappers;
import me.dags.copy.brush.option.Checks;
import me.dags.copy.brush.option.Option;
import me.dags.copy.event.LocatableBlockChange;
import me.dags.copy.operation.Callback;
import me.dags.copy.operation.Operation;
import me.dags.copy.operation.PlaceOperation;
import me.dags.copy.operation.VolumeMapper;
import me.dags.copy.operation.applier.Applier;
import me.dags.copy.operation.calculator.Calculator;
import me.dags.copy.operation.calculator.Volume;
import me.dags.copy.operation.tester.Tester;
import me.dags.copy.registry.brush.BrushSupplier;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"replace"})
public class ReplaceBrush extends AbstractBrush {

    public static final Option<Integer> RADIUS = Option.of("radius", 8, Checks.range(1, 48));
    public static final Option<Mappers> MAPPERS = Mappers.OPTION;

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
        Mappers mappers = getOption(MAPPERS);
        ImmutableList<State.Mapper> rules = ImmutableList.copyOf(mappers);

        Cause cause = PlayerManager.getCause(player);
        Extent extent = player.getWorld();
        Vector3i min = pos.sub(radius, radius, radius);
        Vector3i max = pos.add(radius, radius, radius);
        min = min.max(min.getX(), extent.getBlockMin().getY(), min.getZ());
        max = max.min(max.getX(), extent.getBiomeMax().getY(), max.getZ());

        Vector3i offset = min;
        BlockVolume relativeView = extent.getBlockView(min, max).getRelativeBlockView();
        ImmutableBlockVolume source = relativeView.getImmutableBlockCopy();

        VolumeMapper mapper = new VolumeMapper(0, false, false, false, rules);
        Callback callback = Callback.of(player, (owner, world, result) -> {
            List<LocatableBlockChange> changes = new LinkedList<>();
            Calculator calculator = new Volume(world, result);
            Tester tester = new Tester(world, changes, cause);
            Applier applier = new Applier(world, owner, changes, history, cause);
            Operation operation = new PlaceOperation(owner, calculator, tester, applier, (w, v, x, y, z) -> {
                Vector3i vec = offset.add(x, y, z);
                BlockState src = v.getBlock(x, y, z);
                BlockState dst = w.getBlock(vec);
                if (src != dst) {
                    Location<World> location = new Location<>(w, vec);
                    changes.add(new LocatableBlockChange(location, src));
                    return 1;
                }
                return 0;
            });

            CopyPasta.getInstance().getOperationManager().queueOperation(operation);
        });

        Runnable task = mapper.createTask(source, cause, callback);
        CopyPasta.getInstance().submitAsync(task);
    }

    public static BrushSupplier supplier() {
        return p -> new ReplaceBrush();
    }
}
