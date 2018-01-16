package me.dags.copy.command.element;

import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.ChainElement;
import me.dags.copy.block.Trait;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Value;
import me.dags.copy.command.parser.OptionValueParser;
import me.dags.copy.command.parser.ToggleBoolParser;
import me.dags.copy.command.parser.TraitParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class OptionValueElement extends ChainElement<Option, Value<?>> {

    private final String key;
    private final String dependency = Option.class.getCanonicalName();
    private final Map<Class<?>, OptionValueParser> parsers = getParsers();

    public OptionValueElement(String key, ChainElement.Builder<Option, Value<?>> builder) {
        super(builder);
        this.key = key;
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        Option<?> option = context.getLast(dependency);
        OptionValueParser parser = parsers.get(option.getType());
        if (parser != null) {
            Object value = parser.parse(input, context, option);
            context.add(key, new Value<>(value));
        } else {
            super.parse(input, context);
        }
    }

    @Override
    public void suggest(Input input, Context context, List<String> suggestions) {
        Option<?> option = context.getLast(dependency);
        OptionValueParser parser = parsers.get(option.getType());
        if (parser != null) {
            suggestions.addAll(parser.suggest(input, context, option));
        } else {
            super.suggest(input, context, suggestions);
        }
    }

    private static Map<Class<?>, OptionValueParser> getParsers() {
        return new HashMap<Class<?>, OptionValueParser>(){{
            put(Trait.class, new TraitParser());
            put(Boolean.class, new ToggleBoolParser());
        }};
    }
}
