package me.dags.copy.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.copy.CopyPasta;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.value.Stencil;
import me.dags.copy.brush.stencil.StencilBrush;
import me.dags.copy.util.fmt;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public class StencilCommands {

    @Command("stencil load <stencil>")
    @Permission("copypasta.command.stencil.load")
    @Description("Load a stencil from file and add to the stencil wand")
    public void load(@Src Player player, String name) {
        Optional<StencilBrush> brush = BrushCommands.getBrush(player, StencilBrush.class);
        if (brush.isPresent()) {
            Optional<Stencil> stencil = read(name);
            if (stencil.isPresent()) {
                brush.get().setOption(StencilBrush.STENCIL, stencil.get());
                fmt.info("Successfully loaded stencil ").stress(name).tell(player);
            }
        }
    }

    @Command("stencil save <stencil>")
    @Permission("copypasta.command.stencil.save")
    @Description("Save the stencil attached to your wand to file")
    public void save(@Src Player player, String name) {
        Optional<StencilBrush> brush = BrushCommands.getBrush(player, StencilBrush.class);
        if (brush.isPresent()) {
            Stencil stencil = brush.get().getOption(StencilBrush.STENCIL);
            if (stencil.isPresent()) {
                write(stencil, name);
                fmt.info("Successfully saved stencil ").stress(name).tell(player);
            } else {
                fmt.error("Your brush does not have a stencil attached to it").tell(player);
            }
        }
    }

    @Command("stencil <url> <samples> <threshold>")
    @Permission("copypasta.command.stencil.create")
    @Description("Create a line stencil from an image at the given url")
    public void stencil(@Src Player player, String url, int samples, float threshold) {
        Optional<Brush> brush = BrushCommands.getBrush(player);
        if (brush.isPresent()) {
            if (brush.get().getType().getType() != StencilBrush.class) {
                fmt.error("You must be using a Stencil Brush").tell(player);
                return;
            }

            Supplier<Optional<Stencil>> supplier = () -> Stencil.fromUrl(url, samples, threshold);
            Consumer<Optional<Stencil>> consumer = stencil -> {
                if (stencil.isPresent()) {
                    brush.get().setOption(StencilBrush.STENCIL, stencil.get());
                    fmt.info("Successfully set your stencil").tell(player);
                } else {
                    fmt.error("Unable to create a stencil from the url %s", url).tell(player);
                }
            };

            fmt.info("Creating stencil...").tell(player);
            CopyPasta.getInstance().submitAsync(supplier, consumer);
        }
    }

    private static Optional<Stencil> read(String name) {
        Path in = CopyPasta.getInstance().getConfigDir().resolve("stencils").resolve(name + ".stencil");
        if (!Files.exists(in)) {
            return Optional.empty();
        }

        try (InputStream inputStream = Files.newInputStream(in)) {
            return Optional.of(Stencil.read(inputStream));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static void write(Stencil stencil, String name) {
        Path out = CopyPasta.getInstance().getConfigDir().resolve("stencils").resolve(name + ".stencil");
        try {
            Files.createDirectories(out.getParent());
            try (OutputStream outputStream = Files.newOutputStream(out)) {
                Stencil.write(stencil, outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
