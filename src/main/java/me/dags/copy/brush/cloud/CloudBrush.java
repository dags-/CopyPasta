package me.dags.copy.brush.cloud;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.History;
import me.dags.copy.brush.option.Checks;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Parsable;
import me.dags.copy.event.LocatableBlockChange;
import me.dags.copy.operation.Callback;
import me.dags.copy.operation.Operation;
import me.dags.copy.operation.PlaceOperation;
import me.dags.copy.operation.applier.Applier;
import me.dags.copy.operation.calculator.Calculator;
import me.dags.copy.operation.calculator.Volume;
import me.dags.copy.operation.tester.Tester;
import me.dags.copy.registry.brush.BrushSupplier;
import me.dags.copy.util.fmt;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"cloud"})
public class CloudBrush extends AbstractBrush implements Parsable {

    public static final Option<Integer> SEED = Option.of("seed", 8008);
    public static final Option<Integer> SCALE = Option.of("scale", 32, Checks.range(2, 256));
    public static final Option<Integer> OCTAVES = Option.of("octaves", 4, Checks.range(1, 8));
    public static final Option<Integer> RADIUS = Option.of("radius", 48, Checks.range(1, 96));
    public static final Option<Integer> HEIGHT = Option.of("height", 8, Checks.range(1, 48));
    public static final Option<Integer> OFFSET = Option.of("offset", 3, Checks.range(1, 16));
    public static final Option<Float> DETAIL = Option.of("detail", 1.95F, Checks.range(0.5F, 5.0F));
    public static final Option<Float> DENSITY = Option.of("density", 0.25F, Checks.range(0F, 1F));
    public static final Option<Float> FEATHER = Option.of("feather", 0.45F, Checks.range(0F, 1F));
    public static final Option<BlockType> MATERIAL = Option.of("material", BlockType.class, () -> BlockTypes.STAINED_GLASS);
    public static final Option<String> TRAIT = Option.of("trait", "color");

    private List<BlockState> materials = Collections.emptyList();
    private BlockType type = BlockTypes.AIR;
    private String trait = "";
    private float density = 0F;

    private CloudBrush() {
        super(5);
        setOption(RANGE, 64);
    }

    @Override
    public List<Option<?>> getParseOptions() {
        return Arrays.asList(SCALE, OCTAVES, RADIUS, HEIGHT, OFFSET, DETAIL, DENSITY, FEATHER);
    }

    @Override
    public String getPermission() {
        return "brush.cloud";
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        Cause cause = PlayerManager.getCause(player);

        BlockType type = getOption(MATERIAL);
        String trait = getOption(TRAIT);
        float density = getOption(DENSITY);

        if (type != this.type || !trait.equals(this.trait) || density != this.density) {
            this.type = type;
            this.trait = trait;
            this.density = density;
            materials = getVariants(type, trait, density);
        }

        if (materials.size() <= 0) {
            fmt.error("No materials match block: %s, trait: %s", type, trait).tell(player);
            return;
        }

        Cloud cloud = Cloud.of(
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
        Runnable task = cloud.createTask(callback, pos, cause);
        CopyPasta.getInstance().submitAsync(task);
    }

    public static BrushSupplier supplier() {
        return p -> new CloudBrush();
    }

    private static List<BlockState> getVariants(BlockType type, String trait, float density) {
        return type.getTrait(trait).map(t -> getVariants(type, t, density)).orElse(Collections.emptyList());
    }

    private static List<BlockState> getVariants(BlockType type, BlockTrait<?> trait, float density) {
        ImmutableList.Builder<BlockState> builder = ImmutableList.builder();
        Collection<? extends Comparable<?>> values = trait.getPossibleValues();

        // pad with air blocks according to the density
        for (int i = Math.round(values.size() * (1 - density)); i > 0; i--) {
            builder.add(BlockTypes.AIR.getDefaultState());
        }

        // fill with variants of the given trait, sorted
        BlockState baseState = type.getDefaultState();
        values.stream()
                .sorted()
                .map(value -> baseState.withTrait(trait, value))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(builder::add);

        return builder.build();
    }
}
