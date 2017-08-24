package me.dags.copy.command.element;

import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.ElementProvider;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import me.dags.copy.registry.brush.BrushType;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class OptionElement extends BaseElement {

    private OptionElement(String key) {
        super(key);
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        BrushType type = getBrush(context);
        if (type == null) {
            throw new CommandException("No BrushType present");
        }

        String next = input.next();
        Optional<Option<?>> option = type.getOption(next);

        if (!option.isPresent()) {
            throw new CommandException("Option '%s' is not valid for brush '%s'", next, type);
        }

        context.add(getKey(), option.get());
        context.add(Option.class.getCanonicalName(), option.get());
    }

    @Override
    Collection<String> getOptions(Context context) {
        BrushType type = getBrush(context);
        if (type == null) {
            return Collections.emptyList();
        }
        return type.getOptions().stream().map(Option::getId).collect(Collectors.toList());
    }

    private BrushType getBrush(Context context) {
        BrushType type = context.getOne(BrushType.class.getCanonicalName());
        if (type == null) {
            Optional<Player> source = context.getSource(Player.class);
            if (source.isPresent()) {
                Player player = source.get();
                Optional<Brush> brush = PlayerManager.getInstance().get(player).flatMap(d -> d.getBrush(player));
                if (brush.isPresent()) {
                    return brush.get().getType();
                }
            }
        }
        return type;
    }

    public static ElementProvider provider() {
        return (s, i, options, filter, valueParser) -> new OptionElement(s);
    }
}