package me.dags.copy.command.parser;

import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public interface OptionValueParser {

    Object parse(Input input, Context context, Option<?> option) throws CommandException;

    Collection<String> suggest(Input input, Context context, Option<?> option);

    default Optional<Brush> getBrush(Context context, Option<?> option) {
        Optional<Player> source = context.getSource(Player.class);
        if (source.isPresent()) {
            Player player = source.get();
            return PlayerManager.getInstance().get(player)
                    .flatMap(data -> data.getBrush(player))
                    .filter(brush -> brush.supports(option));
        }
        return Optional.empty();
    }
}