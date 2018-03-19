package me.dags.copy.command.element;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.ValueElement;
import me.dags.commandbus.element.function.Filter;
import me.dags.commandbus.element.function.Options;
import me.dags.commandbus.element.function.ValueParser;
import me.dags.copy.block.state.State;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

/**
 * @author dags <dags@dags.me>
 */
public class StateMapperElement extends ValueElement {

    private final String key;

    StateMapperElement(String key, int priority, Options options, Filter filter, ValueParser<?> parser) {
        super(key, priority, options, filter, parser);
        this.key = key;
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        String match = input.next();
        String matchMat = getMaterial(match);
        if (!matchMaterial(matchMat).isPresent()) {
            throw new CommandException("Invalid material %s", matchMat);
        }

        if (!input.hasNext()) {
            throw new CommandException("A replace material has not been provided");
        }

        String replace = input.next();
        String replaceMat = getMaterial(replace);
        if (!matchMaterial(replaceMat).isPresent()) {
            throw new CommandException("Invalid material %s", replaceMat);
        }

        if (replace.indexOf('[') != -1 && replace.indexOf(']') == -1) {
            throw new CommandException("Invalid rule, missing closing brace: ']'");
        }

        State.Mapper mapper = State.mapper(match, replace);
        context.add(key, mapper);
    }

    @Override
    public void suggest(Input input, Context context, List<String> suggestions) {
        if(!input.hasNext()) {
            return;
        }

        try {
            String next = input.last().next();
            String material = getMaterial(next);
            Optional<BlockType> type = matchMaterial(material);

            if (type.isPresent()) {
                suggestProperty(type.get(), next, suggestions);
                return;
            }

            Sponge.getRegistry().getAllOf(BlockType.class)
                    .stream()
                    .map(BlockType::getId)
                    .filter(name -> name.contains(material))
                    .sorted(Comparator.comparingInt(String::length))
                    .forEach(suggestions::add);

        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    private String getMaterial(String input) {
        int index = input.indexOf('[');
        int split = index > 0 ? index : input.length();
        return input.substring(0, split);
    }

    private void suggestProperty(BlockType type, String input, List<String> suggestions) {
        if (input.endsWith("]")) {
            return;
        }

        int keyStart = Math.max(input.lastIndexOf('['), input.lastIndexOf(',')) + 1;
        int keyEnd = input.indexOf('=', keyStart);
        if (keyStart == 0) {
            return;
        }

        if (keyEnd == -1) {
            keyEnd = input.length();
        }

        String key = input.substring(keyStart, keyEnd);
        Optional<BlockTrait<?>> trait = type.getTrait(key);
        if (!trait.isPresent()) {
            type.getTraits().stream()
                    .map(BlockTrait::getName)
                    .filter(name -> name.startsWith(key))
                    .map(name -> input.substring(0, keyStart) + name)
                    .forEach(suggestions::add);
            return;
        }

        int valStart = keyEnd + 1;
        if (valStart < input.length()) {
            String value = input.substring(valStart);
            trait.get().getPossibleValues().stream()
                    .map(Object::toString)
                    .filter(name -> name.startsWith(value))
                    .map(name -> input.substring(0, valStart) + name)
                    .forEach(suggestions::add);
        }
    }

    private Optional<BlockType> matchMaterial(String material) {
        return Sponge.getRegistry().getType(BlockType.class, material);
    }
}
