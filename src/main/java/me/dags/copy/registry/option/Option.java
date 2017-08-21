package me.dags.copy.registry.option;

import org.spongepowered.api.CatalogType;

/**
 * @author dags <dags@dags.me>
 */
public class Option implements CatalogType {

    private final String key;

    private Option(String key) {
        this.key = key;
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

    public static Option of(String key) {
        return new Option(key);
    }
}
