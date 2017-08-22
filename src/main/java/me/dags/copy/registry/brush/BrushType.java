package me.dags.copy.registry.brush;

import com.google.common.collect.ImmutableList;
import me.dags.commandbus.AliasCatalogType;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class BrushType implements AliasCatalogType {

    public static final BrushType NONE = new BrushType();

    private final String name;
    private final List<String> aliases;
    private final List<Option<?>> options;
    private final Class<? extends Brush> type;
    private final Supplier<? extends Brush> supplier;

    private BrushType() {
        name = "none";
        aliases = ImmutableList.of(name);
        options = Collections.emptyList();
        type =  Brush.class;
        supplier = () -> null;
    }

    private BrushType(Class<? extends Brush> type, Supplier<? extends Brush> supplier, List<Option<?>> options, String... aliases) {
        this.name = aliases[0];
        this.aliases = ImmutableList.copyOf(aliases);
        this.options = options;
        this.type = type;
        this.supplier = supplier;
    }

    public Optional<Brush> create() {
        return Optional.ofNullable(supplier.get());
    }

    public Class<? extends Brush> getType() {
        return type;
    }

    public Optional<Option<?>> getOption(String id) {
        for (Option<?> option : options) {
            if (option.getId().equals(id)) {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    public Collection<Option<?>> getOptions() {
        return options;
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

    @Override
    public boolean equals(Object other) {
        return other != null && (other == this || other instanceof BrushType && ((BrushType) other).getName().equals(getName()));
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public static <T extends Brush> BrushType of(Class<T> type, Supplier<T> supplier, List<Option<?>> options, String... aliases) {
        return new BrushType(type, supplier, options, aliases);
    }
}
