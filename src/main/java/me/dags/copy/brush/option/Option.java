package me.dags.copy.brush.option;

import com.google.common.base.Preconditions;
import me.dags.commandbus.utils.ClassUtils;
import org.spongepowered.api.CatalogType;

import javax.annotation.Nonnull;

/**
 * @author dags <dags@dags.me>
 */
public class Option<T> implements CatalogType {

    private final String key;
    private final Class<T> type;

    private Option(@Nonnull String key, @Nonnull Class<T> type) {
        this.key = key;
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public String getId() {
        return key;
    }

    @Override
    public String getName() {
        return key;
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

    public static <T> Option<T> of(@Nonnull String key, @Nonnull Class<T> type) {
        type = wrap(type);
        Preconditions.checkState(!type.isPrimitive(), "Primitive types not allowed!");
        return new Option<>(key, type);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> wrap(Class<T> type) {
        return (Class<T>) ClassUtils.wrapPrimitive(type);
    }
}
