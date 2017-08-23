package me.dags.copy.registry.brush;

import com.google.common.collect.ImmutableList;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author dags <dags@dags.me>
 */
public class BrushRegistry {

    private static final BrushRegistry instance = new BrushRegistry();

    private final Map<Class<?>, BrushType> types = new HashMap<>();
    private final Map<String, BrushType> registry = new HashMap<>();

    private BrushRegistry(){}

    public <T extends Brush> void register(Class<T> type, BrushSupplier supplier) {
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

        BrushType brushType = BrushType.of(aliases[0], type, supplier, options.build());
        types.put(type, brushType);

        for (String alias : aliases) {
            registry.put(alias, brushType);
        }
    }

    public Optional<BrushType> getById(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    public void forEachUnique(BiConsumer<String, BrushType> consumer) {
        types.values().forEach(type -> consumer.accept(type.getId(), type));
    }

    public void forEachAlias(BiConsumer<String, BrushType> consumer) {
        registry.forEach(consumer);
    }

    public static BrushRegistry getInstance() {
        return instance;
    }

    public static BrushType forClass(Class<?> type) {
        return getInstance().types.getOrDefault(type, BrushType.NONE);
    }
}
