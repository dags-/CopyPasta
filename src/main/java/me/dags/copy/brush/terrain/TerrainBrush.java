package me.dags.copy.brush.terrain;

import com.flowpowered.math.vector.Vector3i;
import com.flowpowered.noise.module.Module;
import me.dags.commandbus.fmt.Fmt;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.block.BlockUtils;
import me.dags.copy.brush.*;
import me.dags.copy.brush.option.Option;
import me.dags.copy.event.LocatableBlockChange;
import me.dags.copy.operation.Operation;
import me.dags.copy.operation.PlaceOperation;
import me.dags.copy.operation.UndoOperation;
import me.dags.copy.operation.applier.Applier;
import me.dags.copy.operation.calculator.Calculator;
import me.dags.copy.operation.calculator.Radius2D;
import me.dags.copy.operation.tester.Tester;
import me.dags.copy.operation.visitor.Visitor2D;
import me.dags.copy.registry.brush.BrushSupplier;
import me.dags.copy.util.fmt;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"terrain", "ter"})
public class TerrainBrush extends AbstractBrush {

    public static final Option<Palette> PALETTE = Palette.OPTION;
    public static final Option<Integer> BASE = Option.of("base", 65);
    public static final Option<Integer> OFFSET = Option.of("offset", 12);
    public static final Option<Integer> VARIANCE = Option.of("variance", 75);
    public static final Option<Integer> RADIUS = Option.of("radius", 24);

    public static final Option<NoiseType> NOISE = Option.of("noise", NoiseType.PERLIN);
    public static final Option<Integer> SEED = Option.of("seed", 1337);
    public static final Option<Integer> SCALE = Option.of("scale", 98);
    public static final Option<Integer> OCTAVES = Option.of("octaves", 3);

    private TerrainBrush() {
        super(10);
    }

    @Override
    public String getPermission() {
        return "brush.mountain";
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
        if (getOption(PALETTE).isEmpty()) {
            Fmt.warn("Your palette is empty! Use ").stress("/palette <blockstate>").tell(player);
            return;
        }
        pos = BlockUtils.findSolidFoundation(player.getWorld(), pos);
        apply(player, pos, getHistory());
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        NoiseType type = getOption(NOISE);
        int seed = getOption(SEED);
        int scale = getOption(SCALE);
        int octaves = getOption(OCTAVES);
        Module module = type.getModule(seed, scale, octaves);

        Palette palette = getOption(PALETTE);
        int radius = getOption(RADIUS);
        int base = getOption(BASE);
        int offset = getOption(OFFSET);
        int variance = getOption(VARIANCE);
        int lift = base - variance + offset;

        UUID uuid = player.getUniqueId();
        Cause cause = PlayerManager.getCause(player);
        List<LocatableBlockChange> changes = new LinkedList<>();

        Calculator calculator = new Radius2D(player.getWorld(), player.getWorld(), pos, radius);
        Tester tester = new Tester(player.getWorld(), changes, cause);
        Applier applier = new Applier(player.getWorld(), uuid, changes, history, cause);
        Visitor2D visitor = (w, v, x, z) -> {
            double noise = module.getValue(x, 0, z);
            int floor = BlockUtils.findSurface(w, x, z, 0, 255);
            int height = (int) Math.round((variance * noise) + lift);
            height = Math.min(255, Math.max(0, height));

            for (int y = floor; y < height; y++) {
                Location<World> location = new Location<>(w, x, y,  z);
                LocatableBlockChange record = new LocatableBlockChange(location, palette.next());
                changes.add(record);
            }

            return Math.abs(height - floor);
        };

        PlayerManager.getInstance().must(player).setOperating(true);
        Operation operation = new PlaceOperation(uuid, calculator, tester, applier, visitor);
        CopyPasta.getInstance().getOperationManager().queueOperation(operation);
    }

    @Override
    public void undo(Player player, History history) {
        if (history.getSize() > 0) {
            LinkedList<BlockSnapshot> record = history.popRecord();
            UndoOperation undo = new UndoOperation(record, player.getUniqueId(), history);
            CopyPasta.getInstance().getOperationManager().queueOperation(undo);
        }
    }


    public static BrushSupplier supplier() {
        return player -> new TerrainBrush();
    }
}
