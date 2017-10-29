package me.dags.copy.brush.schematic;

import com.google.common.reflect.TypeToken;
import me.dags.copy.CopyPasta;
import me.dags.copy.registry.schematic.SchematicEntry;
import me.dags.copy.util.Serializable;
import me.dags.copy.util.WeightedList;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class SchematicList extends WeightedList<SchematicEntry> implements Serializable<SchematicList> {

    private static final TypeToken<SchematicList> TOKEN = TypeToken.of(SchematicList.class);

    @Override
    public TypeToken<SchematicList> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<SchematicList> getSerializer() {
        return new TypeSerializer<SchematicList>() {
            @Override
            public SchematicList deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
                SchematicList list = new SchematicList();
                for (Map.Entry<?, ? extends ConfigurationNode> entry : value.getChildrenMap().entrySet()) {
                    String path = entry.getKey().toString();
                    double weight = value.getNode(path, "weight").getDouble(1D);
                    String repo = value.getNode(path, "repo").getString(CopyPasta.ID);
                    SchematicEntry.of(repo, path).ifPresent(e -> list.add(e, weight));
                }
                return list;
            }

            @Override
            public void serialize(TypeToken<?> type, SchematicList obj, ConfigurationNode value) throws ObjectMappingException {
                obj.iterate((entry, weight) -> {
                    value.getNode(entry.getPath().toString(), "weight").setValue(weight);
                    value.getNode(entry.getPath().toString(), "repo").setValue(entry.getRepo());
                });
            }
        };
    }
}
