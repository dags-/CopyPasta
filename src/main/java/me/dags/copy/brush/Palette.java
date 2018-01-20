package me.dags.copy.brush;

import com.google.common.reflect.TypeToken;
import me.dags.copy.brush.option.Option;
import me.dags.copy.util.Serializable;
import me.dags.copy.util.WeightedList;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;

import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Palette extends WeightedList<BlockState> implements Serializable<Palette> {

    private static final TypeToken<Palette> TOKEN;
    public static final Option<Palette> OPTION;

    static {
        TOKEN = TypeToken.of(Palette.class);
        OPTION = Option.of("palette", Palette.class, Palette::createDefault);
    }

    private Palette() {

    }

    @Override
    public Palette add(BlockState value, double weight) {
        super.add(value, weight);
        return this;
    }

    public static Palette create() {
        return new Palette();
    }

    public static Palette createDefault() {
        return create().add(BlockTypes.WOOL.getDefaultState(), 1);
    }

    @Override
    public String toString() {
        return "palette";
    }

    @Override
    public TypeToken<Palette> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<Palette> getSerializer() {
        return new TypeSerializer<Palette>() {
            @Override
            public Palette deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
                Palette palette = create();
                for (Map.Entry<?, ? extends ConfigurationNode> e : value.getChildrenMap().entrySet()) {
                    Optional<BlockState> state = Sponge.getRegistry().getType(BlockState.class, e.getKey().toString());
                    if (state.isPresent()) {
                        palette.add(state.get(), e.getValue().getFloat());
                    }
                }

                if (palette.isEmpty()) {
                    return createDefault();
                }

                return palette;
            }

            @Override
            public void serialize(TypeToken<?> type, Palette obj, ConfigurationNode value) throws ObjectMappingException {
                obj.iterate((state, weight) -> value.getNode(state.toString()).setValue(weight));
            }
        };
    }
}
