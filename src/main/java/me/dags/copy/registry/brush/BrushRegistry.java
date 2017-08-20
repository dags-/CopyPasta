package me.dags.copy.registry.brush;

import com.google.common.collect.ImmutableList;
import me.dags.copy.brush.Brush;
import org.spongepowered.api.registry.CatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class BrushRegistry implements CatalogRegistryModule<BrushType> {

    private static final BrushRegistry instance = new BrushRegistry();

    private final Map<Class<?>, BrushType> types = new HashMap<>();
    private final Map<String, BrushType> registry = new HashMap<>();

    private BrushRegistry(){}

    public <T extends Brush> void register(Class<T> type, Supplier<T> supplier) {
        BrushType brushType = BrushType.of(type.getSimpleName().toLowerCase(), type, supplier);
        registry.put(brushType.getId(), brushType);
        types.put(type, brushType);
    }

    @Override
    public Optional<BrushType> getById(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    @Override
    public Collection<BrushType> getAll() {
        return ImmutableList.copyOf(registry.values());
    }

    public static BrushRegistry getInstance() {
        return instance;
    }

    public static BrushType forClass(Class<?> type) {
        return getInstance().types.getOrDefault(type, BrushType.NONE);
    }
}
