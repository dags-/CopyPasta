package me.dags.copy.brush.option.value;

import me.dags.config.Node;
import me.dags.copy.brush.option.Option;
import me.dags.copy.util.WeightedList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;

/**
 * @author dags <dags@dags.me>
 */
public class Palette extends WeightedList<BlockState> implements Node.Value<Palette> {

    public static final Option<Palette> OPTION = Option.of("palette", Palette.class, Palette::createDefault);

    private Palette() {

    }

    @Override
    public Palette add(BlockState value, double weight) {
        super.add(value, weight);
        return this;
    }

    @Override
    public Palette fromNode(Node node) {
        Palette palette = create();
        node.iterate((k, v) -> {
            String id = k.toString();
            double weight = v.get(1D);
            Sponge.getRegistry().getType(BlockState.class, id).ifPresent(state -> palette.add(state, weight));
        });
        return palette;
    }

    @Override
    public void toNode(Node node) {
        iterate((s, w) -> node.set(s.getId(), w));
    }

    @Override
    public String toString() {
        return "palette";
    }

    public static Palette create() {
        return new Palette();
    }

    public static Palette createDefault() {
        return create().add(BlockTypes.WOOL.getDefaultState(), 1);
    }
}
