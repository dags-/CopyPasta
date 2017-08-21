package me.dags.copy.brush.schematic;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.fmt.Fmt;
import me.dags.copy.CopyPasta;
import me.dags.copy.block.state.State;
import me.dags.copy.brush.Action;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.clipboard.Clipboard;
import me.dags.copy.brush.clipboard.ClipboardBrush;
import me.dags.copy.registry.option.Option;
import me.dags.copy.registry.schematic.CachedSchematic;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"schematic", "schem"})
public class SchematicBrush extends ClipboardBrush {

    public static final Option SCHEMATICS = Option.of("schematics");
    public static final Option MODE = Option.of("mode");
    public static final Option NAME = Option.of("name");
    public static final Option DIR = Option.of("dir");

    private static final Random RANDOM = new Random();

    private final Clipboard clipboard = Clipboard.empty();

    public SchematicBrush() {
        setOption(ClipboardBrush.FLIPX, false);
        setOption(ClipboardBrush.FLIPY, false);
        setOption(ClipboardBrush.FLIPZ, false);
        setOption(ClipboardBrush.AUTO_FLIP, false);
        setOption(ClipboardBrush.AUTO_ROTATE, false);
        setOption(ClipboardBrush.RANDOM_FLIPH, true);
        setOption(ClipboardBrush.RANDOM_FLIPV, false);
        setOption(ClipboardBrush.RANDOM_ROTATE, true);
        setOption(ClipboardBrush.SOLID_FOUNDATION, true);
        setOption(ClipboardBrush.RANGE, 50);
        setOption(ClipboardBrush.MAPPERS, new LinkedList<State.Mapper>());
    }

    @Override
    public void commitSelection(Player player, Vector3i min, Vector3i max, Vector3i origin, int size) {
        ArchetypeVolume volume = player.getWorld().createArchetypeVolume(min, max, origin);

        Schematic schematic = Schematic.builder()
                .paletteType(BlockPaletteTypes.LOCAL)
                .volume(volume)
                .metaValue(Schematic.METADATA_AUTHOR, player.getName())
                .build();

        Path output = getFilePath();

        try {
            Files.createDirectories(output.getParent());
            try (OutputStream outputStream = Files.newOutputStream(getFilePath())) {
                DataContainer container = DataTranslators.SCHEMATIC.translate(schematic);
                DataFormats.NBT.writeTo(outputStream, container);
            }
            Fmt.info("Saved schematic to %s", output).tell(player);
        } catch (IOException e) {
            e.printStackTrace();
            Fmt.warn("An error occurred whilst saving schematic to %s", output).tell(player);
        }
    }

    @Override
    public String getPermission() {
        return "brush.schematic";
    }

    @Override
    public void primary(Player player, Vector3i pos, Action action) {
        if (getOption(MODE, Mode.SAVE) == Mode.SAVE) {
            super.setClipboard(null);
        } else {
            super.setClipboard(clipboard);
        }

        super.primary(player, pos, action);
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        if (getOption(MODE, Mode.SAVE) == Mode.SAVE) {
            super.setClipboard(null);
        } else {
            List<SchematicEntry> schematics = getOption(SCHEMATICS, Collections.emptyList());
            Optional<CachedSchematic> schematic = chooseNext(schematics);
            if (schematic.isPresent()) {
                clipboard.setSource(schematic.get().getVolume(), schematic.get().getOrigin());
                super.setClipboard(clipboard);
            }
        }

        super.secondary(player, pos, action);
    }

    private Path getFilePath() {
        String name = getOption(NAME, "schem");
        String dir = getOption(DIR, "");

        Path root = CopyPasta.getInstance().getConfigDir().resolve("schematics").resolve(dir);
        Path path;

        int counter = 0;
        String fileName = String.format("%s-%03d.schematic", name, counter);
        while (Files.exists(path = root.resolve(fileName))) {
            fileName = String.format("%s-%03d.schematic", name, ++counter);
        }

        return path;
    }

    private Optional<CachedSchematic> chooseNext(List<SchematicEntry> schematics) {
        for (int i = 0; i < 5; i++) {
            int index = RANDOM.nextInt(schematics.size());
            Optional<CachedSchematic> schematic = schematics.get(index).getSchematic();
            if (schematic.isPresent()) {
                return schematic;
            }
        }
        return Optional.empty();
    }

    private enum Mode {
        PASTE,
        SAVE,
        ;
    }
}
