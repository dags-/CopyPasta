package me.dags.copy.state;

import me.dags.copy.property.Axis;
import me.dags.copy.property.Facing;
import me.dags.copy.property.Half;
import me.dags.copy.property.Property;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public interface State {

    Object ANY_VALUE = new Object() {
        @Override
        public String toString() {
            return "*";
        }
    };

    boolean isPresent();

    default BlockTrait<?> getTrait(String name, Map<BlockTrait<?>, ?> traits) {
        for (BlockTrait<?> trait : traits.keySet()) {
            if (trait.getName().equals(name)) {
                return trait;
            }
        }
        return null;
    }

    default Object getValue(BlockTrait<?> trait, Object value) {
        if (trait != null) {
            for (Object o : trait.getPossibleValues()) {
                if (value.toString().equals(o.toString())) {
                    return o;
                }
            }
        }
        return null;
    }

    interface Matcher extends State {

        boolean matches(BlockState state);
    }

    interface Merger extends State {

        BlockState merge(BlockState state);

        Mapper toMapper();
    }

    interface Mapper extends State, BlockVolumeMapper {

        BlockState map(BlockState state);

        default BlockState map(UnmodifiableBlockVolume volume, int x, int y, int z) {
            return map(volume.getBlock(x, y, z));
        }
    }

    interface Properties extends State {

        BlockType getType();

        Map<String, Object> getProperties();
    }

    static Properties properties(String in) {
        return StateProperties.parse(in);
    }

    static Matcher matcher(String in) {
        return StateMatcher.parse(in);
    }

    static Merger merger(String match, String replace) {
        return StateMerger.parse(match, replace);
    }

    static Mapper emptyMapper() {
        return StateMapper.EMPTY;
    }

    static Mapper mapper(String match, String replace) {
        return merger(match, replace).toMapper();
    }

    static Mapper mapper(Merger... mergers) {
        return StateMapper.mapper(mergers);
    }

    static Mapper mapper(Iterable<Merger> mergers) {
        return StateMapper.mapper(mergers);
    }

    static Mapper rotate(Axis axis, int angle) {
        return rotate("*", "*", axis, angle);
    }

    static Mapper rotate(String match, String replace, Axis axis, int angle) {
        List<Merger> mergers = new LinkedList<>();
        Property.rotate(match, replace, Axis.values(), axis, angle, mergers);
        Property.rotate(match, replace, Half.values(), axis, angle, mergers);
        Property.rotate(match, replace, Facing.values(), axis, angle, mergers);
        return mapper(mergers);
    }

    static Mapper flip(Axis direction) {
        return flip("*", "*", direction);
    }

    static Mapper flip(String match, String replace, Axis direction) {
        List<Merger> mergers = new LinkedList<>();
        Property.flip(match, replace, Axis.values(), direction, mergers);
        Property.flip(match, replace, Half.values(), direction, mergers);
        Property.flip(match, replace, Facing.values(), direction, mergers);
        return mapper(mergers);
    }
}
