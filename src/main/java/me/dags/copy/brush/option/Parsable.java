package me.dags.copy.brush.option;

import java.util.List;
import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Input;
import me.dags.copy.command.element.BrushElements;
import me.dags.copy.util.Utils;

/**
 * @author dags <dags@dags.me>
 */
public interface Parsable extends OptionHolder {

    List<Option<?>> getParseOptions();

    default void parse(String code) {
        List<Option<?>> options = getParseOptions();
        Input input = Utils.tokenize(code);
        for (Option<?> option : options) {
            Object value = option.getDefault().get();
            if (input.hasNext()) {
                try {
                    Object val = BrushElements.FACTORY.getParser(option.getType()).parse(input);
                    if (option.validate(val)) {
                        value = val;
                    }
                } catch (CommandException ignored) {
                }
            }
            setOption(option, value);
        }
    }
}
