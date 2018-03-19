package me.dags.copy.command.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.ChainElement;
import me.dags.copy.block.Trait;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Value;
import me.dags.copy.brush.option.value.Flip;
import me.dags.copy.command.parser.FlipParser;
import me.dags.copy.command.parser.OptionValueParser;
import me.dags.copy.command.parser.ToggleBoolParser;
import me.dags.copy.command.parser.TraitParser;

/**
 * @author dags <dags@dags.me>
 */
public class OptionValueElement extends ChainElement<Option, Value<?>> {

    private final Map<Class<?>, OptionValueParser> parsers = getParsers();

    public OptionValueElement(ChainElement.Builder<Option, Value<?>> builder) {
        super(builder);
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        Option<?> option = context.getLast(getDependency().getCanonicalName());
        OptionValueParser parser = parsers.get(option.getType());
        if (parser != null) {
            Object value = parser.parse(input, context, option);
            context.add(getKey(), new Value<>(value));
        } else {
            super.parse(input, context);
        }
    }

    @Override
    public void suggest(Input input, Context context, List<String> suggestions) {
        Option<?> option = context.getLast(getDependency().getCanonicalName());
        OptionValueParser parser = parsers.get(option.getType());
        if (parser != null) {
            suggestions.addAll(parser.suggest(input, context, option));
        } else {
            super.suggest(input, context, suggestions);
        }
    }

    private static Map<Class<?>, OptionValueParser> getParsers() {
        return new HashMap<Class<?>, OptionValueParser>(){{
            put(Flip.class, new FlipParser());
            put(Trait.class, new TraitParser());
            put(Boolean.class, new ToggleBoolParser());
        }};
    }
}
