package me.dags.copy.block.volume;

import me.dags.copy.block.Snapshot;

import java.util.Iterator;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class BufferView implements Buffer.View<Snapshot> {

    private final List<Snapshot> list;

    public BufferView(List<Snapshot> list) {
        this.list = list;
    }

    @Override
    public Iterator<Snapshot> iterator() {
        return list.iterator();
    }
}
