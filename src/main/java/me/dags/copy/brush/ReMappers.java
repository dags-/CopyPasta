package me.dags.copy.brush;

import com.google.common.reflect.TypeToken;
import me.dags.copy.block.state.State;
import me.dags.copy.util.Serializable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class ReMappers implements Iterable<State.Mapper>, Serializable<ReMappers> {

    private static final TypeToken<ReMappers> TOKEN = TypeToken.of(ReMappers.class);
    public static final ReMappers EMPTY = new ReMappers(Collections.emptyList());

    private final List<State.Mapper> mappers;

    private ReMappers(List<State.Mapper> empty) {
        this.mappers = empty;
    }

    public ReMappers() {
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
    public TypeToken<ReMappers> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<ReMappers> getSerializer() {
        return SERIALIZER;
    }

    private static final TypeSerializer<ReMappers> SERIALIZER = new TypeSerializer<ReMappers>() {
        @Override
        public ReMappers deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            ReMappers mappers = new ReMappers();
            for (Map.Entry<?, ?> entry : value.getChildrenMap().entrySet()) {
                State.Mapper mapper = State.mapper(entry.getKey().toString(), entry.getValue().toString());
                mappers.add(mapper);
            }
            return mappers;
        }

        @Override
        public void serialize(TypeToken<?> type, ReMappers obj, ConfigurationNode value) throws ObjectMappingException {
            for (State.Mapper mapper : obj) {
                value.getNode(mapper.getMatch()).setValue(mapper.getReplace());
            }
        }
    };
}
