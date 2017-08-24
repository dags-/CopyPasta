package me.dags.copy.command.element;

import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.Element;
import me.dags.commandbus.element.function.Filter;

import java.util.Collection;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
abstract class BaseElement implements Element {

    private final String key;

    BaseElement(String key) {
        this.key = key;
    }

    String getKey() {
        return key;
    }

    @Override
    public void suggest(Input input, Context context, List<String> suggestions) {
        if(!input.hasNext()) {
            Collection<String> options = getOptions(context);
            suggestions.addAll(options);
        } else {
            try {
                String e = input.next();
                Collection<String> options = getOptions(context);
                if(options.isEmpty()) {
                    return;
                }

                if (options.stream().anyMatch(s -> s.equalsIgnoreCase(e))) {
                    return;
                }

                String upper = e.toUpperCase();
                for (String option : options) {
                    if (getFilter().test(option, upper)) {
                        suggestions.add(option);
                    }
                }
            } catch (CommandException e) {
                e.printStackTrace();
            }
        }
    }

    Filter getFilter() {
        return Filter.STARTS_WITH;
    }

    abstract Collection<String> getOptions(Context context);
}
