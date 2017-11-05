package me.dags.copy.brush.cloud;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.value.BaseValue;

import java.util.Collection;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
class Materials {

    static <T> List<BlockState> of(BlockType type, Key<? extends BaseValue<T>> key, T... properties) {
        ImmutableList.Builder<BlockState> builder = ImmutableList.builder();
        for (T property : properties) {
            builder.add(type.getDefaultState().with(key, property).orElse(air));
        }
        return builder.build();
    }

    static List<BlockState> of(BlockState... states) {
        return ImmutableList.copyOf(states);
    }

    static List<BlockState> applyDensity(Collection<BlockState> in, float density) {
        ImmutableList.Builder<BlockState> builder = ImmutableList.builder();
        int air = Math.round(in.size() * (1 - density));
        for (int i = 0; i < air; i++) {
            builder.add(Materials.air);
        }
        return builder.addAll(in).build();
    }

    static final List<BlockState> glass = of(
            BlockTypes.STAINED_GLASS,
            Keys.DYE_COLOR,
            DyeColors.BLACK,
            DyeColors.BLUE,
            DyeColors.BROWN,
            DyeColors.CYAN,
            DyeColors.GRAY,
            DyeColors.GREEN,
            DyeColors.LIGHT_BLUE,
            DyeColors.LIME,
            DyeColors.MAGENTA,
            DyeColors.ORANGE,
            DyeColors.PINK,
            DyeColors.PURPLE,
            DyeColors.RED,
            DyeColors.SILVER,
            DyeColors.WHITE,
            DyeColors.YELLOW
    );

    private static final BlockState air = BlockTypes.AIR.getDefaultState();
}
