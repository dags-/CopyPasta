package me.dags.copy.brush.schematic;

import com.google.common.reflect.TypeToken;
import me.dags.copy.util.Serializable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class SchematicList implements Iterable<SchematicEntry>, Serializable<SchematicList> {

    private static final TypeToken<SchematicList> TOKEN = TypeToken.of(SchematicList.class);
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

    public void addAll(Iterable<SchematicEntry> entries) {
        entries.forEach(list::add);
    }

    @Override
    public Iterator<SchematicEntry> iterator() {
        return list.iterator();
    }

    @Override
    public TypeToken<SchematicList> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<SchematicList> getSerializer() {
        return SERIALIZER;
    }

    public static Supplier<SchematicList> supplier() {
        return () -> new SchematicList(new LinkedList<>());
    }

    public static SchematicList forDir(Path dir, String glob) {
        List<SchematicEntry> entries = new LinkedList<>();

        try {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(glob);
            Files.list(dir).filter(p -> matcher.matches(p.getFileName())).map(SchematicEntry::new).forEach(entries::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new SchematicList(entries);
    }

    private static TypeSerializer<SchematicList> SERIALIZER = new TypeSerializer<SchematicList>() {
        @Override
        public SchematicList deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            SchematicList list = new SchematicList();
            list.addAll(value.getList(SchematicEntry.TOKEN));
            return list;
        }

        @Override
        public void serialize(TypeToken<?> type, SchematicList obj, ConfigurationNode value) throws ObjectMappingException {
            value.setValue(obj.list);
        }
    };
}
