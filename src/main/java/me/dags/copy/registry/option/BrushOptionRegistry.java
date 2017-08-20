package me.dags.copy.registry.option;

import com.google.common.collect.ImmutableList;
import me.dags.copy.registry.brush.BrushType;
import org.spongepowered.api.registry.CatalogRegistryModule;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class BrushOptionRegistry implements CatalogRegistryModule<BrushOption> {

    private static final BrushOptionRegistry instance = new BrushOptionRegistry();

    private final Map<BrushType, Set<String>> options = new HashMap<>();
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

    public boolean isValid(BrushType type, BrushOption option) {
        return options.getOrDefault(type, Collections.emptySet()).contains(option.getId());
    }

    public static BrushOptionRegistry getInstance() {
        return instance;
    }

    public void register(BrushType type, BrushOption option) {
        registry.put(option.getId(), option);
        Set<String> options = this.options.computeIfAbsent(type, t -> new HashSet<>());
        options.add(option.getId());
    }
}
