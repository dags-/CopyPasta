package me.dags.copy.brush;

import com.google.common.reflect.TypeToken;
import me.dags.copy.block.state.State;
import me.dags.copy.brush.option.Option;
import me.dags.copy.util.Serializable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class Mappers implements Iterable<State.Mapper>, Serializable<Mappers> {

    public static final Mappers EMPTY = new Mappers(Collections.emptyList());
    public static final Option<Mappers> OPTION = Option.of("mapper", Mappers.class, (Supplier<Mappers>) Mappers::new);

    private static final TypeToken<Mappers> TOKEN = TypeToken.of(Mappers.class);

    private final List<State.Mapper> mappers;

    private Mappers(List<State.Mapper> empty) {
        this.mappers = empty;
    }

    public Mappers() {
        this.mappers = new LinkedList<>();
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public void clear() {
        if (isPresent()) {
            mappers.clear();
        }
    }

    public void add(State.Mapper mapper) {
        if (isPresent()) {
            mappers.add(mapper);
        }
    }

    @Override
    public Iterator<State.Mapper> iterator() {
        return mappers.iterator();
    }

    @Override
    public TypeToken<Mappers> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<Mappers> getSerializer() {
        return SERIALIZER;
    }

    private static final TypeSerializer<Mappers> SERIALIZER = new TypeSerializer<Mappers>() {
        @Override
        public Mappers deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            Mappers mappers = new Mappers();
            for (Map.Entry<?, ?> entry : value.getChildrenMap().entrySet()) {
                State.Mapper mapper = State.mapper(entry.getKey().toString(), entry.getValue().toString());
                mappers.add(mapper);
            }
            return mappers;
        }

        @Override
        public void serialize(TypeToken<?> type, Mappers obj, ConfigurationNode value) throws ObjectMappingException {
            for (State.Mapper mapper : obj) {
                value.getNode(mapper.getMatch()).setValue(mapper.getReplace());
            }
        }
    };
}
