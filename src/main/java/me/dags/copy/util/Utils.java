package me.dags.copy.util;

import me.dags.commandbus.command.Input;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author dags <dags@dags.me>
 */
public class Utils {

    public static Input tokenize(String code) {
        char[] chars = code.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ';') {
                chars[i] = ' ';
            }
        }
        return new Input(new String(chars));
    }

    public static Path getDir(Path parent, String... child) {
        Path p = parent;
        for (String c : child) {
            p = p.resolve(c);
        }
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static Path ensure(Path root, String... path) {
        Path p0 = Paths.get(root.toAbsolutePath().toString(), path);
        try {
            Files.createDirectories(p0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p0;
    }

    public static ConfigurationNode getRootNode(HoconConfigurationLoader loader) {
        try {
            return loader.load();
        } catch (IOException e) {
            ConfigurationNode node = loader.createEmptyNode();
            writeNode(loader, node);
            return node;
        }
    }

    public static void writeNode(HoconConfigurationLoader loader, ConfigurationNode node) {
        try {
            loader.save(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void convertLegacySchematic(Path dir, boolean recurse) {
        try {
            Files.list(dir).forEach(path -> {
                if (Files.isDirectory(path)) {
                    if (recurse) {
                        convertLegacySchematic(dir, recurse);
                    }
                } else {
                    convertLegacySchematic(path);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void convertLegacySchematic(Path input) {
        try {
            final Schematic converted;

            try (InputStream inputStream = Files.newInputStream(input)) {
                DataContainer in = DataFormats.NBT.readFrom(inputStream);
                Schematic schematic = DataTranslators.LEGACY_SCHEMATIC.translate(in);
                converted = Schematic.builder().from(schematic).paletteType(BlockPaletteTypes.LOCAL).build();
            }

            Path output = input.getParent().resolve("converted").resolve(input.getFileName());
            Files.createDirectories(output.getParent());

            try (OutputStream outputStream = Files.newOutputStream(output)) {
                DataContainer out = DataTranslators.SCHEMATIC.translate(converted);
                DataFormats.NBT.writeTo(outputStream, out);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
