package me.dags.copy.brush;

import me.dags.copy.registry.option.BrushOptions;

/**
 * @author dags <dags@dags.me>
 */
public abstract class AbstractBrush implements Brush {

    private final BrushOptions options = new BrushOptions();

    @Override
    public BrushOptions getOptions() {
        return options;
    }
}
