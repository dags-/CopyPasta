package me.dags.copy.brush.option;

import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class Checks {

    public static final Predicate ANY = o -> true;

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> any() {
        return (Predicate<T>) ANY;
    }

    public static <T extends Number> Predicate<T> range(T min, T max) {
        return new Range<>(min, max);
    }

    private static class Range<T extends Number> implements Predicate<T> {

        private final T min;
        private final T max;

        public Range(T min, T max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean test(T t) {
            return t.doubleValue() >= min.doubleValue() && t.doubleValue() <= max.doubleValue();
        }

        @Override
        public String toString() {
            return String.format("%s-%s", min, max);
        }
    }
}
