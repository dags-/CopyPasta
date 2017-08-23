package me.dags.copy.brush;

import me.dags.copy.brush.option.Options;

/**
 * @author dags <dags@dags.me>
 */
public abstract class AbstractBrush implements Brush {

    private final Options options;
    private final History history;

    protected AbstractBrush() {
        this(0);
    }

    protected AbstractBrush(int size) {
        this.options = new Options();
        this.history = new History(size);
    }

    @Override
    public History getHistory() {
        return history;
    }

    @Override
    public Options getOptions() {
        return options;
    }
}
