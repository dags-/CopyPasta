package me.dags.copy.registry.brush;

import me.dags.copy.brush.Brush;
import org.spongepowered.api.CatalogType;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class BrushType implements CatalogType {

    public static BrushType NONE = BrushType.of(null, null);

    private final String name;
    private final Class<? extends Brush> type;
    private final Supplier<? extends Brush> supplier;

    private BrushType() {
        name = "none";
        type =  Brush.class;
        supplier = () -> null;
    }

    private BrushType(Class<? extends Brush> type, Supplier<? extends Brush> supplier) {
        this.name = type.getSimpleName().toLowerCase();
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

    public static <T extends Brush> BrushType of(Class<T> type, Supplier<T> supplier) {
        return new BrushType(type, supplier);
    }
}
