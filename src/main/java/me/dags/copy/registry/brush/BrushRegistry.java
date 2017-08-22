package me.dags.copy.registry.brush;

import com.google.common.collect.ImmutableList;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import org.spongepowered.api.registry.CatalogRegistryModule;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class BrushRegistry implements CatalogRegistryModule<BrushType> {

    private static final BrushRegistry instance = new BrushRegistry();

    private final Map<Class<?>, BrushType> types = new HashMap<>();
    private final Map<String, BrushType> registry = new HashMap<>();

    private BrushRegistry(){}

    public <T extends Brush> void register(Class<T> type, Supplier<T> supplier) {
        ImmutableList.Builder<Option<?>> options = ImmutableList.builder();
        for (Field field : type.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == Option.class) {
                try {
                    Option option = (Option) field.get(null);
                    options.add(option);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        String[] aliases = type.isAnnotationPresent(Aliases.class) ? type.getAnnotation(Aliases.class).value() : new String[]{type.getSimpleName().toLowerCase()};

        BrushType brushType = BrushType.of(type, supplier, options.build(), aliases);
        types.put(type, brushType);

        for (String alias : brushType.getAliases()) {
            registry.put(alias, brushType);
        }
    }

    @Override
    public Optional<BrushType> getById(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    @Override
    public Collection<BrushType> getAll() {
        return ImmutableList.copyOf(registry.values());
    }

    public static BrushRegistry getInstance() {
        return instance;
    }

    public static BrushType forClass(Class<?> type) {
        return getInstance().types.getOrDefault(type, BrushType.NONE);
    }
}
