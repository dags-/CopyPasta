package me.dags.copy.registry.option;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class BrushOptions {

    private final Map<Option, Object> options = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(Option option, T def) {
        return (T) options.getOrDefault(option, def);
    }

    @SuppressWarnings("unchecked")
    public <T> T ensure(Option option, T def) {
        return (T) options.computeIfAbsent(option, k -> def);
    }

    public void set(Option option, Object value) {
        options.put(option, value);
    }
}
