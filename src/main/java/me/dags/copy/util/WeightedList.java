package me.dags.copy.util;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * @author dags <dags@dags.me>
 */
public class WeightedList<T> {

    private final Random random = new Random();
    private final NavigableMap<Double, T> map = Maps.newTreeMap();

    private double total = 0F;

    public WeightedList() {

    }

    public void iterate(BiConsumer<T, Double> consumer) {
        map.entrySet().forEach(e -> consumer.accept(e.getValue(), e.getKey()));
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return map.size();
    }

    public WeightedList<T> add(T value, double weight) {
        if (weight < 0) {
            return this;
        }
        map.put(total += weight, value);
        return this;
    }

    public T next(Random random) {
        double lookup = random.nextFloat() * total;
        Map.Entry<Double, T> entry = map.higherEntry(lookup);
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }

    public T next() {
        double lookup = random.nextFloat() * total;
        Map.Entry<Double, T> entry = map.higherEntry(lookup);
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }
}
