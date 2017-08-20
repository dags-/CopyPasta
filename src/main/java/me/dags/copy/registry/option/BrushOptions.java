package me.dags.copy.registry.option;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class BrushOptions {

    private final Map<String, Object> options = new HashMap<>();

    public <T> T get(BrushOption option, T def) {
        return get(option.getId(), def);
    }

    public <T> T ensure(BrushOption option, T def) {
        return ensure(option.getId(), def);
    }

    public void set(BrushOption option, Object value) {
        set(option.getId(), value);
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
