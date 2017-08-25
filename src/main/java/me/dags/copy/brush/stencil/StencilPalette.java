package me.dags.copy.brush.stencil;

import com.google.common.reflect.TypeToken;
import me.dags.copy.util.Serializable;
import me.dags.copy.util.WeightedList;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;

import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class StencilPalette extends WeightedList<BlockState> implements Serializable<StencilPalette> {

    private static final TypeToken<StencilPalette> TOKEN = TypeToken.of(StencilPalette.class);

    private StencilPalette() {

    }

    @Override
    public StencilPalette add(BlockState value, double weight) {
        super.add(value, weight);
        return this;
    }

    public static StencilPalette create() {
        return new StencilPalette();
    }

    @Override
    public TypeToken<StencilPalette> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<StencilPalette> getSerializer() {
        return new TypeSerializer<StencilPalette>() {
            @Override
            public StencilPalette deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
                StencilPalette palette = create();
                for (Map.Entry<?, ? extends ConfigurationNode> e : value.getChildrenMap().entrySet()) {
                    Optional<BlockState> state = Sponge.getRegistry().getType(BlockState.class, e.getKey().toString());
                    if (state.isPresent()) {
                        palette.add(state.get(), e.getValue().getFloat());
                    }
                }
                return palette;
            }

            @Override
            public void serialize(TypeToken<?> type, StencilPalette obj, ConfigurationNode value) throws ObjectMappingException {
                obj.iterate((state, weight) -> value.getNode(state.toString()).setValue(weight));
            }
        };
    }
}
