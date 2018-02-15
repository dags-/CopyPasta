package me.dags.copy.block;

import me.dags.config.Node;
import me.dags.copy.brush.option.Option;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

/**
 * @author dags <dags@dags.me>
 */
public class Trait implements Node.Value<Trait> {

    public static final Trait EMPTY = new Trait("#empty");
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
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Trait && ((Trait) o).name.equals(name);
    }

    @Override
    public Trait fromNode(Node node) {
        String name = node.get("#empty");
        if (name.isEmpty() || name.equals("#empty")) {
            return EMPTY;
        }
        return new Trait(name);
    }

    @Override
    public void toNode(Node node) {
        if (this != EMPTY) {
            node.set(name);
        }
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
