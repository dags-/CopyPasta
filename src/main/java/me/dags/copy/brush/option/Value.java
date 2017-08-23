package me.dags.copy.brush.option;

import com.google.common.base.Preconditions;

/**
 * @author dags <dags@dags.me>
 */
public class Value<T> {

    private static final Value<?> EMPTY = new Value<>("empty");

    private final T value;

    public Value(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public static <T> Value<T> of(T val) {
        Preconditions.checkNotNull(val);
        return new Value<>(val);
    }

    @SuppressWarnings("unchecked")
    public static <T> Value<T> empty() {
        return (Value<T>) EMPTY;
    }
}
