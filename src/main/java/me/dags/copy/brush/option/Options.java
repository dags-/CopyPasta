package me.dags.copy.brush.option;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author dags <dags@dags.me>
 */
public class Options {

    private final Map<Option, Object> options = new HashMap<>();

    public <T> T get(Option<T> option) {
        Object o = options.get(option);
        if (!option.accepts(o)) {
            Value<T> def = option.getDefault();
            if (def.isPresent()) {
                o = def.get();
            }
        }
        return option.getType().cast(o);
    }

    public <T> T must(Option<T> option) {
        Object o = options.get(option);
        if (!option.accepts(o)) {
            Value<T> def = option.getDefault();
            if (def.isPresent()) {
                o = def.get();
                options.put(option, o);
            }
        }
        return option.getType().cast(o);
    }

    public void set(Option option, Object value) {
        if (option.accepts(value)) {
            options.put(option, value);
        }
    }

    public void reset() {
        Collection<Option> keys = new LinkedList<>(options.keySet());
        for (Option option : keys) {
            Value value = option.getDefault();
            if (value.isPresent()) {
                set(option, value.get());
            }
        }
    }

    public void forEach(BiConsumer<Option, Object> consumer) {
        options.forEach(consumer);
    }
}
