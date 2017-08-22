package me.dags.copy.brush.schematic;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class SchematicList implements Iterable<SchematicEntry> {

    public static final SchematicList EMPTY = new SchematicList(Collections.emptyList());

    private final List<SchematicEntry> list;

    private SchematicList(List<SchematicEntry> list) {
        this.list = list;
    }

    public SchematicList() {
        this.list = new LinkedList<>();
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public int size() {
        return list.size();
    }

    public SchematicEntry get(int index) {
        return list.get(index);
    }

    public void add(SchematicEntry entry) {
        if (isPresent()) {
            this.list.add(entry);
        }
    }

    @Override
    public Iterator<SchematicEntry> iterator() {
        return list.iterator();
    }
}
