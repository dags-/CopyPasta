package me.dags.copy.registry.schematic;

import com.flowpowered.math.vector.Vector3i;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import me.dags.copy.CopyPasta;
import me.dags.copy.block.property.Facing;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.world.schematic.Schematic;

/**
 * @author dags <dags@dags.me>
 */
public class SchematicRegistry implements CacheLoader<SchematicEntry, CachedSchematic> {

    private static final SchematicRegistry instance = new SchematicRegistry();
    private static final DataQuery METADATA = DataQuery.of(".");
    private static final DataQuery ORIGIN = DataQuery.of("offset");
    private static final DataQuery FACEINGH = DataQuery.of(CachedSchematic.FACING_H);
    private static final DataQuery FACEINGV = DataQuery.of(CachedSchematic.FACING_V);

    private final Repository defaultRepo;
    private final Map<String, Repository> repositories = new HashMap<>();
    private final LoadingCache<SchematicEntry, CachedSchematic> cache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(this);

    private SchematicRegistry() {
        Path config = Sponge.getGame().getGameDirectory().resolve("config");
        defaultRepo = repo(config, CopyPasta.ID, "schematics", "schematic", DataTranslators.SCHEMATIC);
        repo(config, "worldedit", "schematics", "schematic", DataTranslators.LEGACY_SCHEMATIC);
    }

    private Repository repo(Path sharedConfig, String name, String path, String extension, DataTranslator<Schematic> format) {
        Path root = sharedConfig.resolve(name).resolve(path);
        Repository repository = new Repository(root, name, extension, format);
        repositories.put(repository.getName(), repository);
        return repository;
    }

    public Repository getDefaultRepo() {
        return defaultRepo;
    }

    public Optional<CachedSchematic> getSchematic(SchematicEntry entry) {
        return Optional.ofNullable(cache.get(entry));
    }

    public Optional<Repository> getRepository(String name) {
        return Optional.ofNullable(repositories.get(name));
    }

    public Stream<String> getRepositories() {
        return repositories.keySet().stream();
    }

    @Override
    public CachedSchematic load(@Nonnull SchematicEntry entry) throws Exception {
        try (InputStream inputStream = Files.newInputStream(entry.getPath())) {
            DataContainer container = DataFormats.NBT.readFrom(inputStream);
            Vector3i origin = container.getView(ORIGIN).map(DataTranslators.VECTOR_3_I::translate).orElse(Vector3i.ZERO);
            Schematic schematic = entry.getFormat().translate(container);

            Optional<DataView> metadata = schematic.getMetadata().getView(METADATA);
            Facing horizontal = metadata.flatMap(d -> d.getString(FACEINGH)).map(Facing::valueOf).orElse(Facing.none);
            Facing vertical = metadata.flatMap(d -> d.getString(FACEINGV)).map(Facing::valueOf).orElse(Facing.none);

            return CachedSchematic.of(schematic, origin, horizontal, vertical);
        }
    }

    public static SchematicRegistry getInstance() {
        return instance;
    }
}
