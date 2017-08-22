package me.dags.copy.brush;

import me.dags.copy.block.state.State;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class ReMappers implements Iterable<State.Mapper> {

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
}
