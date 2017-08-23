package me.dags.copy.block.state;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class StateMerger implements State.Merger {

    private static final StateMerger EMPTY = new StateMerger();

    private final BlockType type;
    private final Map<String, Object> properties;
    private final State.Matcher matcher;

    private StateMerger() {
        this.type = BlockTypes.AIR;
        this.matcher = StateMatcher.EMPTY;
        this.properties = Collections.emptyMap();
    }

    private StateMerger(State.Matcher matcher, BlockType type, Map<String, Object> map) {
        this.type = type;
        this.matcher = matcher;
        this.properties = map;
    }

    @Override
    public boolean isPresent() {
        return this != EMPTY;
    }

    @Override
    public BlockState merge(BlockState in) {
        if (!isPresent()) {
            return in;
        }

        if (!matcher.matches(in)) {
            return in;
        }

        BlockState out = BlockAny.getBaseState(type, in);
        Map<BlockTrait<?>, ?> source = in.getTraitMap();
        Map<BlockTrait<?>, ?> traits = out.getTraitMap();

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            BlockTrait<?> trait = null;
            Object value = null;
            if (entry.getValue() == State.ANY_VALUE) {
                BlockTrait<?> currentTrait = getTrait(entry.getKey(), source);
                if (currentTrait != null) {
                    Object currentValue = source.get(currentTrait);
                    trait = getTrait(entry.getKey(), traits);
                    value = getValue(trait, currentValue);
                }
            } else {
                trait = getTrait(entry.getKey(), traits);
                value = getValue(trait, entry.getValue());
            }

            if (trait != null && value != null) {
                out = out.withTrait(trait, value).orElse(out);
            }
        }

        return out;
    }

    @Override
    public State.Mapper toMapper() {
        if (isPresent()) {
            ImmutableMap.Builder<BlockState, BlockState> builder = ImmutableMap.builder();

            Sponge.getRegistry().getAllOf(BlockState.class).forEach(state -> {
                BlockState merged = merge(state);
                if (merged != state) {
                    builder.put(state, merged);
                }
            });

            return new StateMapper(builder.build());
        }

        return StateMapper.EMPTY;
    }

    @Override
    public String toString() {
        return String.format("%s {%s=%s}", matcher, type, properties);
    }

    static State.Merger parse(String match, String replace) {
        State.Matcher matcher = State.matcher(match);
        State.Properties properties = State.properties(replace);

        if (matcher.isPresent() && properties.isPresent()) {
            BlockType type = properties.getType();
            Map<String, Object> props = new HashMap<>();
            if (type != BlockAny.TYPE) {
                for (Map.Entry<BlockTrait<?>, ?> entry : type.getDefaultState().getTraitMap().entrySet()) {
                    props.put(entry.getKey().getName(), State.ANY_VALUE);
                }
            }

            for (Map.Entry<String, Object> entry : properties.getProperties().entrySet()) {
                if (type == BlockAny.TYPE || props.containsKey(entry.getKey())) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }

            return new StateMerger(matcher, type, ImmutableMap.copyOf(props));
        }

        return StateMerger.EMPTY;
    }
}
