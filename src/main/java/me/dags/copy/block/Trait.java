package me.dags.copy.block;

import me.dags.copy.brush.option.Option;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

/**
 * @author dags <dags@dags.me>
 */
public class Trait {

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
}
