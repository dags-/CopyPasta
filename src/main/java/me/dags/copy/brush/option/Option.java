package me.dags.copy.brush.option;

import org.spongepowered.api.CatalogType;

/**
 * @author dags <dags@dags.me>
 */
public class Option<T> implements CatalogType {

    private final String key;
    private final Class<T> type;

    private Option(String key, Class<T> type) {
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
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return key;
    }

    public static <T> Option<T> of(String key, Class<T> type) {
        return new Option<>(key, type);
    }
}
