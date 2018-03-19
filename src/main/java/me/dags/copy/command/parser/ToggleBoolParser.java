package me.dags.copy.command.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.function.ValueParser;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;

/**
 * @author dags <dags@dags.me>
 */
public class ToggleBoolParser implements OptionValueParser {

    @Override
    public Object parse(Input input, Context context, Option<?> option) throws CommandException {
        if (!input.hasNext()) {
            Optional<Brush> brush = getBrush(context, option);
            if (brush.isPresent()) {
                Object value = brush.get().getOption(option);
                return !(boolean) value;
            }
        }
        return ValueParser.bool().parse(input);
    }

    @Override
    public Collection<String> suggest(Input input, Context context, Option<?> option) {
        return Arrays.asList("true", "false");
    }
}
