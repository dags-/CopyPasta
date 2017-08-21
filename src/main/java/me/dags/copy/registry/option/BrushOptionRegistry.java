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
public class BrushOptionRegistry implements CatalogRegistryModule<Option> {

    private static final BrushOptionRegistry instance = new BrushOptionRegistry();

    private final Map<String, Option> registry = new HashMap<>();

    private BrushOptionRegistry() {}

    @Override
    public Optional<Option> getById(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    @Override
    public Collection<Option> getAll() {
        return ImmutableList.copyOf(registry.values());
    }

    public void register(Option option) {
        registry.put(option.getId(), option);
    }

    public static BrushOptionRegistry getInstance() {
        return instance;
    }
}
