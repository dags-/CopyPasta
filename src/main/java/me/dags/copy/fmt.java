package me.dags.copy;

import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Format;
import me.dags.commandbus.fmt.Formatter;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;

/**
 * @author dags <dags@dags.me>
 */
public class fmt {

    private static final Format DEFAULT = Format.builder()
            .info(TextFormat.of(TextColors.GOLD))
            .stress(TextFormat.of(TextColors.DARK_AQUA))
            .build();

    public static Format get() {
        return Fmt.get("copy", DEFAULT);
    }

    public static Formatter info(Object s, Object... args) {
        return get().info(s, args);
    }

    public static Formatter stress(Object s, Object... args) {
        return get().stress(s, args);
    }

    public static Formatter error(Object s, Object... args) {
        return get().error(s, args);
    }

    public static Formatter warn(Object s, Object... args) {
        return get().warn(s, args);
    }

    public static Formatter sub(Object o, Object... args) {
        return get().subdued(o, args);
    }
}
