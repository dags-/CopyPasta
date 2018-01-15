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
public class MapperSet implements Iterable<State.Mapper>, Serializable<MapperSet> {

    public static final MapperSet EMPTY = new MapperSet(Collections.emptyList());
    public static final Option<MapperSet> OPTION = Option.of("mapper", MapperSet.class, (Supplier<MapperSet>) MapperSet::new);

    private static final TypeToken<MapperSet> TOKEN = TypeToken.of(MapperSet.class);

    private final List<State.Mapper> mappers;

    private MapperSet(List<State.Mapper> empty) {
        this.mappers = empty;
    }

    public MapperSet() {
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
    public TypeToken<MapperSet> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<MapperSet> getSerializer() {
        return SERIALIZER;
    }

    private static final TypeSerializer<MapperSet> SERIALIZER = new TypeSerializer<MapperSet>() {
        @Override
        public MapperSet deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            MapperSet mappers = new MapperSet();
            for (Map.Entry<?, ?> entry : value.getChildrenMap().entrySet()) {
                State.Mapper mapper = State.mapper(entry.getKey().toString(), entry.getValue().toString());
                mappers.add(mapper);
            }
            return mappers;
        }

        @Override
        public void serialize(TypeToken<?> type, MapperSet obj, ConfigurationNode value) throws ObjectMappingException {
            for (State.Mapper mapper : obj) {
                value.getNode(mapper.getMatch()).setValue(mapper.getReplace());
            }
        }
    };
}
