package me.dags.copy.brush.schematic;

import com.google.common.reflect.TypeToken;
import me.dags.copy.registry.schematic.CachedSchematic;
import me.dags.copy.registry.schematic.SchematicRegistry;
import me.dags.copy.util.Serializable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class SchematicEntry implements Serializable<SchematicEntry> {

    static final TypeToken<SchematicEntry> TOKEN = TypeToken.of(SchematicEntry.class);

    private final Path path;

    public SchematicEntry(Path path) {
        this.path = path;
    }

    public Optional<CachedSchematic> getSchematic() {
        return SchematicRegistry.getInstance().getSchematic(path);
    }

    @Override
    public TypeToken<SchematicEntry> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<SchematicEntry> getSerializer() {
        return SERIALIZER;
    }

    private static final TypeSerializer<SchematicEntry> SERIALIZER = new TypeSerializer<SchematicEntry>() {
        @Override
        public SchematicEntry deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            return new SchematicEntry(Paths.get(value.getString()));
        }

        @Override
        public void serialize(TypeToken<?> type, SchematicEntry obj, ConfigurationNode value) throws ObjectMappingException {
            value.setValue(obj.path.toString());
        }
    };
}
