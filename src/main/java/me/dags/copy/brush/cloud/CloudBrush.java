package me.dags.copy.brush.cloud;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.History;
import me.dags.copy.brush.option.Checks;
import me.dags.copy.brush.option.Option;
import me.dags.copy.event.LocatableBlockChange;
import me.dags.copy.operation.Callback;
import me.dags.copy.operation.Operation;
import me.dags.copy.operation.PlaceOperation;
import me.dags.copy.operation.UndoOperation;
import me.dags.copy.operation.applier.Applier;
import me.dags.copy.operation.calculator.Calculator;
import me.dags.copy.operation.calculator.Volume;
import me.dags.copy.operation.tester.Tester;
import me.dags.copy.registry.brush.BrushSupplier;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"cloud"})
public class CloudBrush extends AbstractBrush {

    public static final Option<Integer> SEED = Option.of("seed", 8008);
    public static final Option<Integer> SCALE = Option.of("scale", 32, Checks.range(2, 256));
    public static final Option<Integer> OCTAVES = Option.of("octaves", 4, Checks.range(1, 8));
    public static final Option<Integer> RADIUS = Option.of("radius", 48, Checks.range(1, 96));
    public static final Option<Integer> HEIGHT = Option.of("height", 8, Checks.range(1, 48));
    public static final Option<Integer> OFFSET = Option.of("offset", 3, Checks.range(1, 16));
    public static final Option<Float> DETAIL = Option.of("detail", 1.95F, Checks.range(0.5F, 5.0F));
    public static final Option<Float> DENSITY = Option.of("density", 0.25F, Checks.range(0F, 1F));
    public static final Option<Float> FEATHER = Option.of("feather", 0.45F, Checks.range(0F, 1F));

    private CloudBrush() {
        super(5);
        setOption(RANGE, 64);
    }

    @Override
    public String getPermission() {
        return "brush.cloud";
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        Cause cause = PlayerManager.getCause(player);
        List<BlockState> materials = Materials.applyDensity(Materials.glass, getOption(DENSITY));

        Cloud cloud2 = Cloud.of(
                getOption(SEED),
                getOption(SCALE),
                getOption(OCTAVES),
                getOption(RADIUS),
                getOption(HEIGHT),
                getOption(OFFSET),
                getOption(DETAIL),
                getOption(FEATHER),
                materials
        );

        Callback callback = Callback.of(player, (owner, world, result) -> {
            int offsetX = result.getBlockSize().getX() / 2;
            int offsetZ = result.getBlockSize().getZ() / 2;
            List<LocatableBlockChange> changes = new LinkedList<>();

            Calculator calculator = new Volume(world, result);
            Tester tester = new Tester(world, changes, cause);
            Applier applier = new Applier(world, owner, changes, history, cause);

            Operation operation = new PlaceOperation(owner, calculator, tester, applier, (w, v, x, y, z) -> {
                BlockState state = v.getBlock(x, y, z);
                if (state.getType() != BlockTypes.AIR) {
                    x += pos.getX() - offsetX;
                    y += pos.getY();
                    z += pos.getZ() - offsetZ;
                    if (w.getBlockType(x, y, z) == BlockTypes.AIR) {
                        Location<World> location = new Location<>(w, x, y, z);
                        changes.add(new LocatableBlockChange(location, state));
                        return 1;
                    }
                }
                return 0;
            });

            CopyPasta.getInstance().getOperationManager().queueOperation(operation);
        });

        PlayerManager.getInstance().must(player).setOperating(true);
        Runnable task = cloud2.createTask(callback, pos, cause);
        CopyPasta.getInstance().submitAsync(task);
    }

    @Override
    public void undo(Player player, History history) {
        if (history.getSize() > 0) {
            PlayerManager.getInstance().must(player).setOperating(true);
            LinkedList<BlockSnapshot> record = history.popRecord();
            UndoOperation undo = new UndoOperation(record, player.getUniqueId(), history);
            CopyPasta.getInstance().getOperationManager().queueOperation(undo);
        }
    }

    public static BrushSupplier supplier() {
        return p -> new CloudBrush();
    }
}
