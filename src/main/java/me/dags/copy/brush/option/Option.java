package me.dags.copy.brush.option;

import me.dags.commandbus.utils.ClassUtils;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Option<T> {

    private final String key;
    private final Class<T> type;
    private final Predicate<T> validator;
    private final Supplier<Value<T>> defaultValue;

    private Option(@Nonnull String key, @Nonnull Class<T> type, @Nonnull Supplier<Value<T>> defaultValue, Predicate<T> predicate) {
        this.key = key;
        this.type = type;
        this.validator = predicate;
        this.defaultValue = defaultValue;
    }

    public String getId() {
        return key;
    }

    public Class<T> getType() {
        return type;
    }

    public Value<T> getDefault() {
        return defaultValue.get();
    }

    public boolean validate(Object in) {
        return accepts(in) && validator.test(type.cast(in));
    }

    public boolean accepts(Object o) {
        return o != null && getType().isInstance(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option<?> option = (Option<?>) o;
        return getType() == option.getType() && key.equals(option.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key;
    }

    public String getUsage() {
        if (validator != Checks.ANY) {
            return String.format("%s[%s]", type.getSimpleName(), validator.toString());
        }
        return type.getSimpleName();
    }

    public static <T> Option<T> of(String key, T value) {
        return of(key, value, Checks.any());
    }

    public static <T> Option<T> of(String key, T value, Predicate<T> validator) {
        return of(key, wrap(value.getClass()), () -> value, validator);
    }

    public static <T> Option<T> of(String key, Class<T> type, Supplier<T> supplier) {
        return of(key, type, supplier, Checks.any());
    }

    public static <T> Option<T> of(String key, Class<T> type, Supplier<T> supplier, Predicate<T> validator) {
        Class<T> t = wrap(type);
        Supplier<Value<T>> s = () -> Value.of(supplier.get());
        return new Option<>(key, t, s, validator);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> wrap(Class<?> type) {
        return (Class<T>) ClassUtils.wrapPrimitive(type);
    }
}
