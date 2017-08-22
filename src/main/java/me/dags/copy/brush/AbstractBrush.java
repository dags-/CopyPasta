package me.dags.copy.brush;

import me.dags.copy.brush.option.Options;

/**
 * @author dags <dags@dags.me>
 */
public abstract class AbstractBrush implements Brush {

    private final Options options = new Options();
    private final History history;

    protected AbstractBrush() {
        history = new History(5);
    }

    protected AbstractBrush(int size) {
        history = new History(size);
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
