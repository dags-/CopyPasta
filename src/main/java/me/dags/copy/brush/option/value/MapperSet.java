package me.dags.copy.brush.option.value;

import me.dags.config.Node;
import me.dags.copy.block.state.State;
import me.dags.copy.brush.option.Option;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class MapperSet implements Iterable<State.Mapper>, Node.Value<MapperSet> {

    public static final MapperSet EMPTY = new MapperSet(Collections.emptyList());
    public static final Option<MapperSet> OPTION = Option.of("mapper", MapperSet.class, (Supplier<MapperSet>) MapperSet::new);

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
    public MapperSet fromNode(Node node) {
        if (!node.isEmpty()) {
            MapperSet set = new MapperSet();
            node.iterate((k, v) -> {
                String match = k.toString();
                String replace = v.get("");
                if (!replace.isEmpty()) {
                    set.add(State.mapper(match, replace));
                }
            });
            return set;
        }
        return EMPTY;
    }

    @Override
    public void toNode(Node node) {
        for (State.Mapper mapper : this) {
            node.set(mapper.getMatch(), mapper.getReplace());
        }
    }

    @Override
    public String toString() {
        return "mappers";
    }
}
