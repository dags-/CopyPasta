package me.dags.copy.command.element;

import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.ElementProvider;
import me.dags.copy.registry.brush.BrushRegistry;
import me.dags.copy.registry.brush.BrushType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class BrushElement extends BaseElement {

    private BrushElement(String key) {
        super(key);
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        String next = input.next();
        Optional<BrushType> type = BrushRegistry.getInstance().getById(next);

        if (!type.isPresent()) {
            throw new CommandException("Input '%s' is not a valid BrushType", next);
        }

        context.add(getKey(), type.get());
        context.add(BrushType.class.getCanonicalName(), type.get());
    }

    @Override
    Collection<String> getOptions(Context context) {
        List<String> options = new LinkedList<>();
        BrushRegistry.getInstance().getAll().stream().map(BrushType::getAliases).forEach(options::addAll);
        return options;
    }

    public static ElementProvider provider() {
        return (s, i, options, filter, valueParser) -> new BrushElement(s);
    }
}
