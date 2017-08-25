package me.dags.copy.util;

import com.google.common.collect.Maps;

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

    protected WeightedList() {

    }

    public void iterate(BiConsumer<T, Double> consumer) {
        map.entrySet().forEach(e -> consumer.accept(e.getValue(), e.getKey()));
    }

    public WeightedList<T> add(T value, double weight) {
        if (weight < 0) {
            return this;
        }
        map.put(total += weight, value);
        return this;
    }

    public T next() {
        double lookup = random.nextFloat() * total;
        return map.higherEntry(lookup).getValue();
    }
}
