package me.dags.copy.property;

import me.dags.copy.state.State;

import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */
public interface Property<T> {

    String getProperty();

    T rotate(Axis axis, int angle);

    T flip(Axis axis);

    static <T extends Property<T>> void rotate(String match, String replace, T[] values, Axis axis, int angle, Collection<State.Merger> collector) {
        for (T t : values) {
            T rotated = t.rotate(axis, angle);
            if (rotated != t) {
                String from = String.format("%s[%s]", match, t.getProperty());
                String to = String.format("%s[%s]", replace, rotated.getProperty());
                collector.add(State.merger(from, to));
            }
        }
    }

    static <T extends Property<T>> void flip(String match, String replace, T[] values, Axis direction, Collection<State.Merger> collector) {
        for (T t : values) {
            T rotated = t.flip(direction);
            if (rotated != t) {
                String from = String.format("%s[%s]", match, t.getProperty());
                String to = String.format("%s[%s]", replace, rotated.getProperty());
                collector.add(State.merger(from, to));
            }
        }
    }
}
