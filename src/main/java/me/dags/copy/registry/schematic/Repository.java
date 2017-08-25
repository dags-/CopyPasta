package me.dags.copy.registry.schematic;

import me.dags.copy.CopyPasta;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.schematic.Schematic;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class Repository {

    private static final Object lock = new Object();

    private final Path root;
    private final String name;
    private final String extension;
    private final PathMatcher matcher;
    private final DataTranslator<Schematic> format;

    private volatile List<Path> tree = Collections.emptyList();

    public Repository(Path root, String name, String extension, DataTranslator<Schematic> format) {
        this.name = name;
        this.root = root;
        this.extension = extension;
        this.format = format;
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:*." + extension);
        Task.builder().execute(this::scan).interval(15, TimeUnit.MINUTES).submit(CopyPasta.getInstance());
    }

    public DataTranslator<Schematic> getFormat() {
        return format;
    }

    public Path getRelative(Path in) {
        if (in.startsWith(root)) {
            return root.relativize(in);
        }
        return in;
    }

    public Path getAbsolute(Path relative) {
        if (relative.startsWith(root)) {
            return relative;
        }
        return root.resolve(relative);
    }

    public String getName() {
        return name;
    }

    public Supplier<Optional<Path>> save(Schematic schematic, String dir, String name) {
        return () -> {
            Path path = getNext(dir, name);
            try {
                Files.createDirectories(path.getParent());
                try (OutputStream outputStream = Files.newOutputStream(path)) {
                    DataContainer container = DataTranslators.SCHEMATIC.translate(schematic);
                    DataFormats.NBT.writeTo(outputStream, container);
                }

                return Optional.of(path);
            } catch (IOException e) {
                return Optional.empty();
            }
        };
    }

    public Stream<Path> match(String path) {
        List<Path> paths = getTree();
        return paths.stream().filter(p -> p.startsWith(path));
    }

    private Path getNext(String dir, String name) {
        Set<Path> paths = getTree().stream().collect(Collectors.toSet());

        Path base = Paths.get(dir);
        Path path;

        int counter = 0;
        while (paths.contains(path = base.resolve(String.format("%s-%03d.%s", name, counter, extension)))) {
            counter++;
        }

        return path;
    }

    private List<Path> getTree() {
        List<Path> tree;
        synchronized (lock) {
            tree = this.tree;
        }
        return tree;
    }

    private void setTree(List<Path> tree) {
        synchronized (lock) {
            this.tree = tree;
        }
    }

    private void scan() {
        try {
            List<Path> list = Files.walk(root)
                    .filter(p -> matcher.matches(p.getFileName()))
                    .map(root::relativize)
                    .collect(Collectors.toList());
            setTree(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
