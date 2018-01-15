package me.dags.copy.brush.terrain;

import com.flowpowered.math.vector.Vector3i;
import com.flowpowered.noise.module.Module;
import me.dags.commandbus.fmt.Fmt;
import me.dags.copy.block.BlockUtils;
import me.dags.copy.brush.*;
import me.dags.copy.brush.option.Checks;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Parsable;
import me.dags.copy.operation.phase.Calculate;
import me.dags.copy.registry.brush.BrushSupplier;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"terrain"})
public class TerrainBrush extends AbstractBrush implements Parsable {

    public static final Option<Palette> PALETTE = Palette.OPTION;
    public static final Option<Integer> BASE = Option.of("base", -1, Checks.range(-1, 255));
    public static final Option<Integer> OFFSET = Option.of("offset", 12, Checks.range(0, 255));
    public static final Option<Integer> VARIANCE = Option.of("variance", 75, Checks.range(0, 128));
    public static final Option<Integer> RADIUS = Option.of("radius", 24, Checks.range(1, 64));

    public static final Option<NoiseType> NOISE = Option.of("noise", NoiseType.PERLIN);
    public static final Option<Integer> SEED = Option.of("seed", 1337);
    public static final Option<Integer> SCALE = Option.of("scale", 98, Checks.range(2, 512));
    public static final Option<Integer> OCTAVES = Option.of("octaves", 3, Checks.range(1, 8));

    private TerrainBrush() {
        super(5);
    }

    @Override
    public List<Option<?>> getParseOptions() {
        return Arrays.asList(NOISE, SCALE, OCTAVES, RADIUS, BASE, OFFSET, VARIANCE);
    }

    @Override
    public String getPermission() {
        return "brush.terrain";
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        if (getOption(PALETTE).isEmpty()) {
            Fmt.warn("Your palette is empty! Use ").stress("/palette <blockstate>").tell(player);
            return;
        }
        super.secondary(player, pos, action);
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
        if (base == -1) {
            base = BlockUtils.findSolidFoundation(player.getWorld(), pos).getY();
        }

        int offset = getOption(OFFSET);
        int variance = getOption(VARIANCE);
        int lift = base - variance + offset;

        UUID uuid = player.getUniqueId();
        Predicate<BlockState> predicate = Calculate.applyAir(false);
//
//
//
//        Modifier modifier = Modifier.NONE;
//        Calculate calculate = new Calculate(player.getWorld(), )
//
//        Cause cause = PlayerManager.getCause(player);
//        List<LocatableBlockChange> changes = new LinkedList<>();
//
//        Calculator calculator = new Radius2D(player.getWorld(), player.getWorld(), pos, radius);
//        Tester tester = new Tester(player.getWorld(), changes, cause);
//        Applier applier = new Applier(player.getWorld(), uuid, changes, history, cause);
//        Visitor2D visitor = (w, v, x, z) -> {
//            double noise = module.getValue(x, 0, z);
//            int floor = BlockUtils.findSurface(w, x, z, 0, 255);
//            int height = (int) Math.round((variance * noise) + lift);
//            height = Math.min(255, Math.max(0, height));
//
//            for (int y = floor; y < height; y++) {
//                Location<World> location = new Location<>(w, x, y,  z);
//                LocatableBlockChange record = new LocatableBlockChange(location, palette.next());
//                changes.add(record);
//            }
//
//            return Math.abs(height - floor);
//        };
//
//        PlayerManager.getInstance().must(player).setOperating(true);
//        Operation operation = new PlaceOperation(uuid, calculator, tester, applier, visitor);
//        CopyPasta.getInstance().getOperationManager().queueOperation(operation);
    }

    public static BrushSupplier supplier() {
        return player -> new TerrainBrush();
    }
}
