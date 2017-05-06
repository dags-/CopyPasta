package me.dags.copy.block;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class TraitUtils {

    public static BlockState rotateFacing(BlockState state, Axis rotationAxis, int angle) {
        Optional<BlockTrait<?>> trait = state.getTrait("facing");
        if (trait.isPresent()) {
            Optional<?> value = state.getTraitValue(trait.get());
            if (value.isPresent()) {
                Facing facing = Facing.fromName(value.get().toString());
                if (facing != null) {
                    Facing rotated = facing.rotate(rotationAxis, angle);
                    state = withTrait(state, trait.get(), rotated);
                }
            }
        }
        return state;
    }

    public static BlockState rotateAxis(BlockState state, Axis rotationAxis, int angle) {
        Optional<BlockTrait<?>> trait = state.getTrait("axis");
        if (trait.isPresent()) {
            Optional<?> value = state.getTraitValue(trait.get());
            if (value.isPresent()) {
                Axis axis = Axis.fromName(value.get().toString());
                if (axis != null) {
                    Axis rotated = axis.rotate(rotationAxis, angle);
                    state = withTrait(state, trait.get(), rotated);
                }
            }
        }
        return state;
    }

    // Doors are buggy:
    // - the facing direction of a door is determined by the lower-half, Sponge does not return the correct facing
    //   direction for the upper-half (defaults to whatever the default state posses)
    // - when processing 'flipY', the lower half's position is placed above the upper half's
    // - ideally, we'd then just correct the halves in the blockstate traits so that the upper becomes lower and visa versa
    //   (but! since the upper half does not contain the correct facing direction the resulting door will not be facing
    //   in the correct direction!)
    // - instead, just flip the vertical positions of the upper and lower halves
    public static int flipDoorY(BlockState state) {
        if (state.getTrait("hinge").isPresent()) {
            Optional<BlockTrait<?>> trait = state.getTrait("half");
            if (trait.isPresent()) {
                Optional<?> value = state.getTraitValue(trait.get());
                if (value.isPresent()) {
                    Half half = Half.fromName(value.get().toString());
                    if (half == Half.lower) {
                        return -1;
                    }
                    if (half == Half.upper) {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    public static BlockState flipFacing(BlockState state, Axis direction) {
        Optional<BlockTrait<?>> trait = state.getTrait("facing");
        if (trait.isPresent()) {
            Optional<?> value = state.getTraitValue(trait.get());
            if (value.isPresent()) {
                Facing facing = Facing.fromName(value.get().toString());
                if (facing != null) {
                    Facing flipped = facing.flip(direction);
                    state = withTrait(state, trait.get(), flipped);
                }
            }
        }
        return state;
    }

    public static BlockState flipHalf(BlockState state, Axis direction) {
        Optional<BlockTrait<?>> trait = state.getTrait("half");
        if (trait.isPresent()) {
            Optional<?> value = state.getTraitValue(trait.get());
            if (value.isPresent()) {
                Half half = Half.fromName(value.get().toString());
                if (half != null) {
                    Half flipped = half.flip(direction);
                    state = withTrait(state, trait.get(), flipped);
                }
            }
        }
        return state;
    }

    public static BlockState flipHinge(BlockState state, Axis direction) {
        Optional<BlockTrait<?>> trait = state.getTrait("hinge");
        if (trait.isPresent()) {
            Optional<?> value = state.getTraitValue(trait.get());
            if (value.isPresent()) {
                Hinge hinge = Hinge.fromName(value.get().toString());
                if (hinge != null) {
                    Hinge flipped = hinge.flip(direction);
                    state = withTrait(state, trait.get(), flipped);
                }
            }
        }
        return state;
    }

    private static BlockState withTrait(BlockState state, BlockTrait<?> trait, TraitValue lookup) {
        if (lookup != null) {
            for (Object value : trait.getPossibleValues()) {
                if (lookup.matches(value)) {
                    return state.withTrait(trait, value).orElse(state);
                }
            }
        }
        return state;
    }
}
