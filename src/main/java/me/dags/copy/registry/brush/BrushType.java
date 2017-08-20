package me.dags.copy.registry.brush;

import me.dags.copy.brush.Brush;
import org.spongepowered.api.CatalogType;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class BrushType implements CatalogType {

    public static BrushType NONE = new BrushType();

    private final String name;
    private final Class<? extends Brush> type;
    private final Supplier<? extends Brush> supplier;

    private BrushType() {
        name = "none";
        type =  Brush.class;
        supplier = () -> null;
    }

    private BrushType(String name, Class<? extends Brush> type, Supplier<? extends Brush> supplier) {
        this.name = name;
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
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    public static <T extends Brush> BrushType of(String name, Class<T> type, Supplier<T> supplier) {
        return new BrushType(name, type, supplier);
    }
}
