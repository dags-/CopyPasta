package me.dags.copy.operation.modifier;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;

import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public interface Filter extends Predicate<BlockState> {

    Filter ANY = s -> true;

    Filter AIR = s -> s.getType() == BlockTypes.AIR;

    Filter NO_AIR = s -> s.getType() != BlockTypes.AIR;

    static Filter pasteAir(boolean paste) {
        return paste ? ANY : NO_AIR;
    }

    static Filter replaceAir(boolean replace) {
        return replace ? AIR : ANY;
    }
}
