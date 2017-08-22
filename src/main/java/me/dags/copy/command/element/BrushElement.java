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
        BrushType type = BrushRegistry.getInstance().getById(next).orElseThrow(() -> new CommandException("Invalid BrushType '%s'", next));
        context.add(getKey(), type);
        context.add(BrushType.class.getCanonicalName(), type);
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
