package me.dags.copy.command.parser;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.function.Filter;
import me.dags.copy.block.Trait;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

/**
 * @author dags <dags@dags.me>
 */
public class TraitParser implements OptionValueParser {

    @Override
    public Object parse(Input input, Context context, Option<?> option) throws CommandException {
        Optional<Brush> optional = getBrush(context, Trait.TRAIT_OPTION);
        if (optional.isPresent()) {
            Brush brush = optional.get();
            if (brush.supports(Trait.MATERIAL_OPTION)) {
                String search = input.next();
                BlockType type = brush.getOption(Trait.MATERIAL_OPTION);
                Optional<BlockTrait<?>> trait = type.getTrait(search);

                if (trait.isPresent()) {
                    return new Trait(trait.get().getName());
                }

                throw new CommandException("Trait '%s' is not valid for block '%s'", search, type.getName());
            }
            throw new CommandException("Brush '%s' does not support option '%s'", brush.getType(), Trait.MATERIAL_OPTION);
        }
        throw new CommandException("No brush present");
    }

    @Override
    public Collection<String> suggest(Input input, Context context, Option<?> option) {
        Optional<Brush> optional = getBrush(context, Trait.TRAIT_OPTION);
        try {
            if (optional.isPresent()) {
                Brush brush = optional.get();
                if (brush.supports(Trait.MATERIAL_OPTION)) {
                    String search = "";
                    if (input.hasNext()) {
                        search = input.next().toLowerCase();
                    }

                    BlockType type = brush.getOption(Trait.MATERIAL_OPTION);
                    List<String> suggestions = new LinkedList<>();
                    for (BlockTrait trait : type.getTraits()) {
                        if (trait.getName().equalsIgnoreCase(search)) {
                            return Collections.emptyList();
                        }

                        if (Filter.STARTS_WITH.test(trait.getName(), search)) {
                            suggestions.add(trait.getName());
                        }
                    }
                    return suggestions;
                }
            }
        } catch (CommandException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
