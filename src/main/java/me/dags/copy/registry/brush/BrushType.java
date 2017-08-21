package me.dags.copy.registry.brush;

import com.google.common.collect.ImmutableList;
import me.dags.commandbus.AliasCatalogType;
import me.dags.copy.brush.Brush;
import me.dags.copy.registry.option.Option;
import me.dags.copy.registry.option.BrushOptionRegistry;
import org.spongepowered.api.CatalogType;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class BrushType implements AliasCatalogType {

    public static BrushType NONE = new BrushType();

    private final String name;
    private final List<String> aliases;
    private final Class<? extends Brush> type;
    private final Supplier<? extends Brush> supplier;

    private BrushType() {
        name = "none";
        aliases = ImmutableList.of(name);
        type =  Brush.class;
        supplier = () -> null;
    }

    private BrushType(Class<? extends Brush> type, Supplier<? extends Brush> supplier, String... aliases) {
        this.name = aliases[0];
        this.aliases = ImmutableList.copyOf(aliases);
        this.type = type;
        this.supplier = supplier;
    }

    public Optional<Brush> create() {
        return Optional.ofNullable(supplier.get());
    }

    public Class<? extends Brush> getType() {
        return type;
    }

    @Override
    public String getId() {
        return getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public String toString() {
        return name;
    }

    public static <T extends Brush> BrushType of(Class<T> type, Supplier<T> supplier, String... aliases) {
        return new BrushType(type, supplier, aliases);
    }
}
