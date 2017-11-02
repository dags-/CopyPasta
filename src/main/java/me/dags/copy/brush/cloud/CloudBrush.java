package me.dags.copy.brush.cloud;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.util.concurrent.FutureCallback;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Action;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.History;
import me.dags.copy.brush.option.Option;
import me.dags.copy.event.LocatableBlockChange;
import me.dags.copy.operation.Operation;
import me.dags.copy.operation.PlaceOperation;
import me.dags.copy.operation.UndoOperation;
import me.dags.copy.operation.applier.Applier;
import me.dags.copy.operation.calculator.Calculator;
import me.dags.copy.operation.calculator.Volume;
import me.dags.copy.operation.tester.Tester;
import me.dags.copy.registry.brush.BrushSupplier;
import me.dags.copy.util.fmt;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"cloud"})
public class CloudBrush extends AbstractBrush {

    public static final Option<Integer> SEED = Option.of("seed", 8008);
    public static final Option<Integer> SCALE = Option.of("scale", 32);
    public static final Option<Integer> OCTAVES = Option.of("octaves", 4);
    public static final Option<Integer> RADIUS = Option.of("radius", 48);
    public static final Option<Integer> HEIGHT = Option.of("height", 8);
    public static final Option<Integer> OFFSET = Option.of("offset", 3);
    public static final Option<Float> DETAIL = Option.of("detail", 1.95F);
    public static final Option<Float> DENSITY = Option.of("density", 0.25F);
    public static final Option<Float> FEATHER = Option.of("feather", 0.45F);

    private CloudBrush() {
        super(5);
        setOption(RANGE, 64);
    }

    @Override
    public String getPermission() {
        return "brush.cloud";
    }

    @Override
    public void primary(Player player, Vector3i pos, Action action) {
        if (PlayerManager.getInstance().must(player).isOperating()) {
            fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }
        undo(player, getHistory());
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        if (PlayerManager.getInstance().must(player).isOperating()) {
            fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }
        apply(player, pos, getHistory());
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        Function<DyeColor, BlockState> glass = d -> BlockTypes.STAINED_GLASS.getDefaultState().with(Keys.DYE_COLOR, d).orElse(BlockTypes.STAINED_GLASS.getDefaultState());
        List<BlockState> materials = new LinkedList<>();

        Collections.addAll(
                materials,
                glass.apply(DyeColors.BLACK),
                glass.apply(DyeColors.BLUE),
                glass.apply(DyeColors.BROWN),
                glass.apply(DyeColors.CYAN),
                glass.apply(DyeColors.GRAY),
                glass.apply(DyeColors.GREEN),
                glass.apply(DyeColors.LIGHT_BLUE),
                glass.apply(DyeColors.LIME),
                glass.apply(DyeColors.MAGENTA),
                glass.apply(DyeColors.ORANGE),
                glass.apply(DyeColors.PINK),
                glass.apply(DyeColors.PURPLE),
                glass.apply(DyeColors.RED),
                glass.apply(DyeColors.SILVER),
                glass.apply(DyeColors.WHITE),
                glass.apply(DyeColors.YELLOW)
        );

        Cloud cloud2 = Cloud.of(
                getOption(SEED),
                getOption(SCALE),
                getOption(OCTAVES),
                getOption(RADIUS),
                getOption(HEIGHT),
                getOption(OFFSET),
                getOption(DETAIL),
                getOption(DENSITY),
                getOption(FEATHER),
                materials
        );

        PlayerManager.getInstance().must(player).setOperating(true);
        Runnable task = cloud2.createTask(callback(player, pos, history), pos, PlayerManager.getCause(player));
        CopyPasta.getInstance().submitAsync(task);
    }

    @Override
    public void undo(Player player, History history) {
        if (history.getSize() > 0) {
            LinkedList<BlockSnapshot> record = history.popRecord();
            UndoOperation undo = new UndoOperation(record, player.getUniqueId(), history);
            CopyPasta.getInstance().getOperationManager().queueOperation(undo);
        }
    }

    private FutureCallback<BlockVolume> callback(Player player, Vector3i pos, History history) {
        return new FutureCallback<BlockVolume>() {
            @Override
            public void onSuccess(@Nullable BlockVolume result) {
                int offset = result.getBlockSize().getX() / 2;
                Cause cause = PlayerManager.getCause(player);
                List<LocatableBlockChange> changes = new LinkedList<>();
                Calculator calculator = new Volume(player.getWorld(), result);
                Tester tester = new Tester(player.getWorld(), changes, cause);
                Applier applier = new Applier(player.getWorld(), player.getUniqueId(), changes, history, cause);
                Operation operation = new PlaceOperation(player.getUniqueId(), calculator, tester, applier, (world, volume, x, y, z) -> {
                    BlockState state = volume.getBlock(x, y, z);
                    if (state.getType() != BlockTypes.AIR) {
                        x += pos.getX() - offset;
                        y += pos.getY();
                        z += pos.getZ() - offset;
                        if (world.getBlockType(x, y, z) != state.getType()) {
                            Location<World> location = new Location<>(world, x, y, z);
                            changes.add(new LocatableBlockChange(location, state));
                            return 1;
                        }
                    }
                    return 0;
                });
                CopyPasta.getInstance().getOperationManager().queueOperation(operation);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        };
    }

    public static BrushSupplier supplier() {
        return p -> new CloudBrush();
    }
}
