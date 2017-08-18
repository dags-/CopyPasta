package me.dags.copy.registry.option;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.registry.CatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class BrushOptionRegistry implements CatalogRegistryModule<BrushOption> {

    private static final BrushOptionRegistry instance = new BrushOptionRegistry();

    private final Map<String, BrushOption> registry = new HashMap<>();

    private BrushOptionRegistry() {}

    @Override
    public Optional<BrushOption> getById(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    @Override
    public Collection<BrushOption> getAll() {
        return ImmutableList.copyOf(registry.values());
    }

    public static BrushOptionRegistry getInstance() {
        return instance;
    }

    public void register(BrushOption option) {
        registry.put(option.getId(), option);
    }
}
