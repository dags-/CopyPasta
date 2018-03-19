package me.dags.copy.block.volume;

import com.flowpowered.math.vector.Vector3i;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public interface Buffer<T, V> {

    void addRelative(T t, int x, int y, int z);

    void addAbsolute(T t, int x, int y, int z);

    View<V> getView();

    interface View<V> extends Iterable<V> {

    }

    interface Factory<T, V> {

        Buffer<T, V> create(UUID owner, Vector3i pos, int size);
    }
}
