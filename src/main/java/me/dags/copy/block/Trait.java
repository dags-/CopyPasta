package me.dags.copy.block;

import com.google.common.reflect.TypeToken;
import me.dags.copy.brush.option.Option;
import me.dags.copy.util.Serializable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

/**
 * @author dags <dags@dags.me>
 */
public class Trait implements Serializable<Trait> {

    private static final TypeToken<Trait> TOKEN = TypeToken.of(Trait.class);
    private static final TypeSerializer<Trait> SERIALIZER = new Serializer();
    public static final Option<Trait> TRAIT_OPTION = Option.of("trait", Trait.class, Trait::defaultTrait);
    public static final Option<BlockType> MATERIAL_OPTION = Option.of("material", BlockType.class, Trait::defaultMaterial);

    private final String name;

    public Trait(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public TypeToken<Trait> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<Trait> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Trait && ((Trait) o).name.equals(name);
    }

    public static BlockType defaultMaterial() {
        return Sponge.getRegistry().getType(BlockType.class, "conquest:cloud_white").orElse(BlockTypes.STAINED_GLASS);
    }

    public static Trait defaultTrait() {
        return Sponge.getRegistry().getType(BlockType.class, "conquest:cloud_white")
                .map(t -> new Trait("opacity"))
                .orElse(new Trait("color"));
    }

    private static class Serializer implements TypeSerializer<Trait> {

        @Override
        public Trait deserialize(TypeToken<?> type, ConfigurationNode value) {
            return new Trait(value.getString());
        }

        @Override
        public void serialize(TypeToken<?> type, Trait obj, ConfigurationNode value) {
            value.setValue(obj.getName());
        }
    }
}
