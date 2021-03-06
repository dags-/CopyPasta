package me.dags.copy.block.state;

import java.util.Collections;
import java.util.Map;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;

/**
 * @author dags <dags@dags.me>
 */
class StateMatcher implements State.Matcher {

    static StateMatcher EMPTY = new StateMatcher(BlockTypes.AIR, Collections.emptyMap());

    private final BlockType type;
    private final Map<String, Object> properties;

    private StateMatcher(BlockType type, Map<String, Object> map) {
        this.type = type;
        this.properties = map;
    }

    @Override
    public boolean isPresent() {
        return this != EMPTY;
    }

    @Override
    public boolean matches(BlockState state) {
        if (!isPresent()) {
            return false;
        }

        if (state.getType() != type && type != BlockAny.TYPE) {
            return false;
        }

        Map<BlockTrait<?>, ?> traits = state.getTraitMap();

        outer:
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            for (Map.Entry<BlockTrait<?>, ?> trait : traits.entrySet()) {
                if (trait.getKey().getName().equals(entry.getKey())) {
                    if (entry.getKey() == State.ANY_VALUE) {
                        continue outer;
                    }

                    if (trait.getValue().toString().equals(entry.getValue().toString())) {
                        continue outer;
                    }
                }
            }
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("%s=%s", type, properties);
    }

    static State.Matcher parse(String input) {
        State.Properties properties = State.properties(input);

        if (properties.getType() == BlockAny.TYPE && properties.getProperties().isEmpty()) {
            return StateMatcher.EMPTY;
        }

        if (properties.isPresent()) {
            return new StateMatcher(properties.getType(), properties.getProperties());
        }

        return StateMatcher.EMPTY;
    }
}
