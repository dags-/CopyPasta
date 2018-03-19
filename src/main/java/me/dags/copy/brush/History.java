package me.dags.copy.brush;

import java.util.LinkedList;
import org.spongepowered.api.block.BlockSnapshot;

/**
 * @author dags <dags@dags.me>
 */
public class History {

    private final LinkedList<LinkedList<BlockSnapshot>> history = new LinkedList<>();
    private final int size;

    public History(int size) {
        this.size = size;
    }

    public LinkedList<BlockSnapshot> popRecord() {
        return history.removeLast();
    }

    public boolean hasNext() {
        return !history.isEmpty();
    }

    public LinkedList<BlockSnapshot> nextRecord() {
        LinkedList<BlockSnapshot> list = new LinkedList<>();
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
