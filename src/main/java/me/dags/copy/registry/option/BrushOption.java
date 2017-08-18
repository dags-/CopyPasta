package me.dags.copy.registry.option;

import org.spongepowered.api.CatalogType;

/**
 * @author dags <dags@dags.me>
 */
public class BrushOption implements CatalogType {

    private final String key;

    private BrushOption(String key) {
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

    public static BrushOption of(String key) {
        return new BrushOption(key);
    }
}
