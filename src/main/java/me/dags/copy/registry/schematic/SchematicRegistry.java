package me.dags.copy.registry.schematic;

import com.flowpowered.math.vector.Vector3i;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.world.schematic.Schematic;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class SchematicRegistry implements CacheLoader<Path, CachedSchematic> {

    private static final SchematicRegistry instance = new SchematicRegistry();
    private static final DataQuery ORIGIN = DataQuery.of("offset");

    private final Cache<Path, CachedSchematic> cache = Caffeine.<String, CachedSchematic>newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(this);

    private SchematicRegistry() {

    }

    public Optional<CachedSchematic> get(Path path) {
        return Optional.ofNullable(cache.getIfPresent(path));
    }

    @Override
    public CachedSchematic load(@Nonnull Path path) throws Exception {
        try (InputStream inputStream = Files.newInputStream(path)) {
            DataContainer container = DataFormats.NBT.readFrom(inputStream);
            Vector3i origin = container.getView(ORIGIN).map(DataTranslators.VECTOR_3_I::translate).orElse(Vector3i.ZERO);
            Schematic schematic = DataTranslators.SCHEMATIC.translate(container);
            return CachedSchematic.of(schematic, origin);
        }
    }

    public static SchematicRegistry getInstance() {
        return instance;
    }
}
