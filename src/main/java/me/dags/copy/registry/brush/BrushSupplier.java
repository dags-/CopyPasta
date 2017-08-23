package me.dags.copy.registry.brush;

import me.dags.copy.brush.Brush;

import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public interface BrushSupplier extends Supplier<Brush> {

    @Override
    Brush get();
}
