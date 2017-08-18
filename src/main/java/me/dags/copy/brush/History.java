package me.dags.copy.brush;

import org.spongepowered.api.block.BlockSnapshot;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class History {

    private final LinkedList<List<BlockSnapshot>> history = new LinkedList<>();
    private final int size;

    public History(int size) {
        this.size = size;
    }

    public List<BlockSnapshot> popRecord() {
        return history.removeLast();
    }

    public boolean hasNext() {
        return !history.isEmpty();
    }

    public List<BlockSnapshot> nextRecord() {
        List<BlockSnapshot> list = new LinkedList<>();
        if (history.size() < size) {
            history.add(list);
        } else {
            history.removeFirst();
            history.add(list);
        }
        return list;
    }

    public int getSize() {
        return history.size();
    }

    public int getMax() {
        return size;
    }
}
