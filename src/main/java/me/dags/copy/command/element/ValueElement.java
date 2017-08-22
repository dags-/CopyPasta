package me.dags.copy.command.element;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.ElementFactory;
import me.dags.commandbus.element.ElementProvider;
import me.dags.commandbus.element.function.ValueParser;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class ValueElement extends BaseElement {

    private final ElementFactory factory = CommandBus.elements().build();

    private ValueElement(String key) {
        super(key);
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        Option option = context.getOne(Option.class.getCanonicalName());
        if (option == null) {
            throw new CommandException("No Option present");
        }

        ValueParser<?> parser = factory.getParser(option.getType());
        Object object = parser.parse(input);

        if (object == null) {
            throw new CommandException("Unable to parse value for Option %s", option.getName());
        }

        context.add(getKey(), Value.of(object));
    }

    @Override
    Collection<String> getOptions(Context context) {
        Option option = context.getOne(Option.class.getCanonicalName());
        if (option == null) {
            return Collections.emptyList();
        }

        return factory.getOptions(option.getType()).get().collect(Collectors.toList());
    }

    public static ElementProvider provider() {
        return (s, i, options, filter, valueParser) -> new ValueElement(s);
    }
}
