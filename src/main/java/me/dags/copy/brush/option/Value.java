package me.dags.copy.brush.option;

import com.google.common.base.Preconditions;

/**
 * @author dags <dags@dags.me>
 */
public class Value<T> {

    private final T value;

    public Value(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public static <T> Value<T> of(T val) {
        Preconditions.checkNotNull(val);
        return new Value<>(val);
    }
}
