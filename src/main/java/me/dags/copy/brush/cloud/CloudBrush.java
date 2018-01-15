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
import me.dags.copy.operation.callback.Callback;
import me.dags.copy.operation.modifier.Filter;
import me.dags.copy.operation.modifier.Translate;
import me.dags.copy.registry.brush.BrushSupplier;
import me.dags.copy.util.fmt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.entity.living.player.Player;

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
    public static final Option<BlockType> MATERIAL = Option.of("material", BlockType.class, CloudBrush::defaultMaterial);
    public static final Option<String> TRAIT = Option.of("trait", CloudBrush.defaultTrait());

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

        Cloud cloud = new Cloud(this, materials);
        PlayerManager.getInstance().must(player).setOperating(true);
        Callback callback = Callback.place(player, history, Filter.ANY, Filter.ANY, Translate.NONE);
        Runnable task = cloud.createTask(player.getUniqueId(), pos, callback);
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

    public static BlockType defaultMaterial() {
        return Sponge.getRegistry().getType(BlockType.class, "conquest:cloud_white").orElse(BlockTypes.STAINED_GLASS);
    }

    public static String defaultTrait() {
        return Sponge.getRegistry().getType(BlockType.class, "conquest:cloud_white").map(t -> "opacity").orElse("color");
    }
}
