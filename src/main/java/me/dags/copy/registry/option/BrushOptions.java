package me.dags.copy.registry.option;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
public class BrushOptions {

    private final Map<String, Object> options = new HashMap<>();
    private final Set<String> validOptions = new HashSet<>();

    public void register(BrushOption option) {
        this.validOptions.add(option.getId());
    }

    public boolean isValid(String option) {
        return validOptions.contains(option);
    }

    public boolean isValid(BrushOption option) {
        return isValid(option.getId());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T def) {
        return (T) options.getOrDefault(key, def);
    }

    @SuppressWarnings("unchecked")
    public <T> T ensure(String key, T def) {
        return (T) options.computeIfAbsent(key, k -> def);
    }

    public void set(String key, Object value) {
        options.put(key, value);
    }
}
