package me.dags.copy.command.parser;

import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.value.Flip;

import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */
public class FlipParser implements OptionValueParser {

    @Override
    public Object parse(Input input, Context context, Option<?> option) throws CommandException {
        Brush brush = brush(context, Flip.OPTION);
        Flip flip = brush.getOption(Flip.OPTION);

        return null;
    }

    @Override
    public Collection<String> suggest(Input input, Context context, Option<?> option) {
        return null;
    }
}
