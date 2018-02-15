package me.dags.copy.command.element;

import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.ChainElement;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.option.value.Palette;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */
public class StencilPaletteElement extends ChainElement<Palette, Palette> {

    private final String key;
    private final String lookup;

    StencilPaletteElement(String key, Builder<Palette, Palette> builder) {
        super(builder);
        this.key = key;
        this.lookup = Palette.class.getCanonicalName();
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        ensurePalette(context);

        StencilPaletteElement palette = context.getLast(lookup);
        if (palette != null && input.hasNext() && input.peek().equalsIgnoreCase("init")) {
            input.next();
            context.add(lookup, Palette.create());
            context.add(key, Palette.create());
            return;
        }

        super.parse(input, context);
    }

    @Override
    public Collection<String> getOptions(Context context) {
        ensurePalette(context);
        return super.getOptions(context);
    }

    private void ensurePalette(Context context) {
        context.getSource(Player.class)
                .flatMap(player -> PlayerManager.getInstance().get(player).flatMap(d -> d.getBrush(player)))
                .filter(brush -> brush.supports(Palette.OPTION))
                .map(brush -> brush.getOption(Palette.OPTION))
                .ifPresent(palette -> context.add(Palette.class.getCanonicalName(), palette));
    }
}
