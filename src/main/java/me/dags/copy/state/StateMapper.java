package me.dags.copy.state;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class StateMapper implements State.Mapper {

    static final StateMapper EMPTY = new StateMapper(Collections.emptyMap());

    private final Map<BlockState, BlockState> states;

    StateMapper(Map<BlockState, BlockState> states) {
        this.states = states;
    }

    @Override
    public boolean isPresent() {
        return this != EMPTY;
    }

    @Override
    public BlockState map(BlockState state) {
        if (isPresent()) {
            return states.getOrDefault(state, state);
        }
        return state;
    }

    @Override
    public String toString() {
        return states.toString();
    }

    static State.Mapper mapper(Iterable<Merger> mergers) {
        Map<BlockState, BlockState> map = new HashMap<>();
        Sponge.getRegistry().getAllOf(BlockState.class).forEach(state -> {
            for (Merger merger : mergers) {
                BlockState result = merger.merge(state);
                if (result != state) {
                    map.put(state, result);
                }
            }
        });
        return new StateMapper(ImmutableMap.copyOf(map));
    }


    static State.Mapper mapper(State.Merger... mergers) {
        Map<BlockState, BlockState> map = new HashMap<>();
        Sponge.getRegistry().getAllOf(BlockState.class).forEach(state -> {
            for (Merger merger : mergers) {
                BlockState result = merger.merge(state);
                if (result != state) {
                    map.put(state, result);
                }
            }
        });
        return new StateMapper(ImmutableMap.copyOf(map));
    }
}
