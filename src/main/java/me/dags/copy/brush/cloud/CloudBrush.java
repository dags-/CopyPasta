package me.dags.copy.brush.cloud;

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.block.Trait;
import me.dags.copy.block.property.Facing;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.History;
import me.dags.copy.brush.option.Checks;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Parsable;
import me.dags.copy.operation.callback.Callback;
import me.dags.copy.operation.modifier.Filter;
import me.dags.copy.operation.modifier.Translate;
import me.dags.copy.registry.brush.BrushSupplier;
import me.dags.copy.util.fmt;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"cloud"})
public class CloudBrush extends AbstractBrush implements Parsable {

    public static final Option<Integer> SEED = Option.of("seed", 1);
    public static final Option<Float> FREQUENCY = Option.of("frequency", 0.5F, Checks.range(0F, 1F));
    public static final Option<Integer> OCTAVES = Option.of("octaves", 4, Checks.range(1, 8));
    public static final Option<Integer> RADIUS = Option.of("radius", 38, Checks.range(1, 96));
    public static final Option<Float> FEATHER = Option.of("feather", 0.4F, Checks.range(0F, 1F));
    public static final Option<Float> OPACITY = Option.of("opacity", 0.5F, Checks.range(0F, 1F));
    public static final Option<Float> DENSITY = Option.of("density", 0.6F, Checks.range(0F, 1F));
    public static final Option<Float> SCALE = Option.of("scale", 0.1F, Checks.range(0F, 1F));
    public static final Option<Integer> HEIGHT = Option.of("height", 10, Checks.range(2, 48));
    public static final Option<Float> CENTER = Option.of("center", 0.3F, Checks.range(0F, 1F));
    public static final Option<Float> INCLINE = Option.of("incline", 0F, Checks.range(-1F, 1F));
    public static final Option<Boolean> REPLACE_AIR = Option.of("air.replace", true);
    public static final Option<BlockType> MATERIAL = Trait.MATERIAL_OPTION;
    public static final Option<Trait> TRAIT = Trait.TRAIT_OPTION;

    private static final ImmutableList<BlockState> EMPTY = ImmutableList.copyOf(Collections.emptyList());

    private ImmutableList<BlockState> materials = EMPTY;
    private BlockType type = BlockTypes.AIR;
    private Trait trait = new Trait("none");
    private float density = 0F;

    private CloudBrush() {
        super(5);
        setOption(RANGE, 32);
    }

    @Override
    public List<Option<?>> getParseOptions() {
        return Arrays.asList(FREQUENCY, OCTAVES, RADIUS, FEATHER, OPACITY, DENSITY, SCALE, HEIGHT, CENTER, INCLINE);
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        BlockType type = getOption(MATERIAL);
        Trait trait = getOption(TRAIT);
        float density = getOption(DENSITY);

        if (type != this.type || !trait.equals(this.trait) || density != this.density) {
            this.type = type;
            this.trait = trait;
            this.density = density;
            this.materials = getVariants(type, trait.getName());
        }

        if (materials.size() <= 0) {
            fmt.error("No materials match block: %s, trait: %s", type, trait).tell(player);
            return;
        }

        Vector2f facing = Facing.getFacingF(player);
        Cloud cloud = new Cloud(this, materials, facing);
        Filter filter = Filter.replaceAir(getOption(REPLACE_AIR));
        PlayerManager.getInstance().must(player).setOperating(true);
        Callback callback = Callback.of(player, history, filter, Filter.ANY, Translate.NONE);
        Runnable task = cloud.createTask(player.getUniqueId(), pos, callback);
        CopyPasta.getInstance().submitAsync(task);
        fmt.sub("Pasting...").tell(CopyPasta.NOTICE_TYPE, player);
    }

    public static BrushSupplier supplier() {
        return p -> new CloudBrush();
    }

    private static ImmutableList<BlockState> getVariants(BlockType type, String trait) {
        return type.getTrait(trait).map(t -> getVariants(type, t)).orElse(EMPTY);
    }

    private static ImmutableList<BlockState> getVariants(BlockType type, BlockTrait<?> trait) {
        ImmutableList.Builder<BlockState> builder = ImmutableList.builder();
        builder.add(BlockTypes.AIR.getDefaultState());
        BlockState baseState = type.getDefaultState();
        trait.getPossibleValues().stream()
                .sorted()
                .map(value -> baseState.withTrait(trait, value))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(builder::add);
        return builder.build();
    }
}
