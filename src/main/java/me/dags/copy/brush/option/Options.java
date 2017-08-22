package me.dags.copy.brush.option;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Options {

    private final Map<Option, Object> options = new HashMap<>();

    public <T> T get(Option<T> option, T def) {
        Object o = options.get(option);
        if (o != null && option.getType().isInstance(o)) {
            return option.getType().cast(o);
        }
        return def;
    }

    public <T> T ensure(Option<T> option, Supplier<T> def) {
        Object o = options.get(option);
        if (o == null || o.getClass() != option.getType()) {
            T t = def.get();
            options.put(option, t);
            return t;
        }
        return option.getType().cast(o);
    }

    public void set(Option option, Object value) {
        options.put(option, value);
    }
}
