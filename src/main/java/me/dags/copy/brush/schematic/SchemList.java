package me.dags.copy.brush.schematic;

import com.google.common.reflect.TypeToken;
import me.dags.copy.util.Serializable;
import me.dags.copy.util.WeightedList;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class SchemList extends WeightedList<SchematicEntry> implements Serializable<SchemList> {

    private static final TypeToken<SchemList> TOKEN = TypeToken.of(SchemList.class);

    private void add(Path path) {
        add(new SchematicEntry(path), 1D);
    }

    @Override
    public TypeToken<SchemList> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<SchemList> getSerializer() {
        return new TypeSerializer<SchemList>() {
            @Override
            public SchemList deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
                SchemList list = new SchemList();
                for (Map.Entry<?, ? extends ConfigurationNode> entry : value.getChildrenMap().entrySet()) {
                    Path path = Paths.get(entry.getKey().toString());
                    list.add(new SchematicEntry(path), entry.getValue().getDouble());
                }
                return list;
            }

            @Override
            public void serialize(TypeToken<?> type, SchemList obj, ConfigurationNode value) throws ObjectMappingException {
                obj.iterate((entry, weight) -> value.getNode(entry.toString()).setValue(weight));
            }
        };
    }
}
