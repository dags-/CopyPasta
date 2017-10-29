package me.dags.copy.registry.schematic;

import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.world.schematic.Schematic;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class SchematicEntry {

    private final DataTranslator<Schematic> format;
    private final String repo;
    private final Path path;

    private SchematicEntry(String repo, Path path, DataTranslator<Schematic> format) {
        this.repo = repo;
        this.path = path;
        this.format = format;
    }

    public DataTranslator<Schematic> getFormat() {
        return format;
    }

    public String getRepo() {
        return repo;
    }

    public Path getName() {
        return path.getFileName();
    }

    public Path getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getName().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchematicEntry that = (SchematicEntry) o;

        return path != null ? path.equals(that.path) : that.path == null;

    }

    public Optional<CachedSchematic> getSchematic() {
        return SchematicRegistry.getInstance().getSchematic(this);
    }

    public static Optional<SchematicEntry> of(String repository, String path) {
        return SchematicRegistry.getInstance().getRepository(repository).map(repo -> of(repo, path));
    }

    public static SchematicEntry of(Repository repository, String path) {
        return of(repository, Paths.get(path));
    }

    public static SchematicEntry of(Repository repository, Path path) {
        return new SchematicEntry(repository.getName(), repository.getAbsolute(path), repository.getFormat());
    }
}
