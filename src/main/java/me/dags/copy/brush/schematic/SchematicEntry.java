package me.dags.copy.brush.schematic;

import me.dags.copy.registry.schematic.CachedSchematic;
import me.dags.copy.registry.schematic.SchematicRegistry;

import java.nio.file.Path;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class SchematicEntry {

    private final Path path;

    public SchematicEntry(Path path) {
        this.path = path;
    }

    public Optional<CachedSchematic> getSchematic() {
        return SchematicRegistry.getInstance().get(path);
    }
}
