package me.dags.copy.command.element;

import java.util.Optional;
import me.dags.commandbus.CommandBus;
import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.element.ChainElement;
import me.dags.commandbus.element.ElementFactory;
import me.dags.commandbus.element.ElementProvider;
import me.dags.commandbus.element.function.Filter;
import me.dags.commandbus.element.function.Options;
import me.dags.commandbus.element.function.ValueParser;
import me.dags.copy.CopyPasta;
import me.dags.copy.block.state.State;
import me.dags.copy.brush.BrushType;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.Value;
import me.dags.copy.brush.option.value.Palette;
import me.dags.copy.registry.brush.BrushRegistry;
import me.dags.copy.registry.schematic.SchematicEntry;
import me.dags.copy.registry.schematic.SchematicRegistry;
import org.spongepowered.api.block.BlockState;

/**
 * @author dags <dags@dags.me>
 */
public class BrushElements {

    private static final ElementFactory builtin = CommandBus.elements().build();
    public static final ElementFactory FACTORY = getElements();

    public static CommandBus getCommandBus(CopyPasta plugin) {
        return CommandBus.builder()
                .elements(FACTORY)
                .owner(plugin)
                .build();
    }

    private static ElementFactory getElements() {
        ElementFactory.Builder builder = CommandBus.elements();
        stencilPalette(builder);
        schem(builder);

        brush(builder);
        option(builder);
        value(builder);

        builder.provider(State.Mapper.class, StateMapperElement::new);

        return builder.build();
    }

    private static void schem(ElementFactory.Builder builder) {
        builder.filter(SchematicEntry.class, Filter.STARTS_WITH);
        builder.options(SchematicEntry.class, () -> SchematicRegistry.getInstance().getDefaultRepo().getOptions());
        builder.parser(SchematicEntry.class, s -> {
            Optional<SchematicEntry> schem = SchematicRegistry.getInstance().getDefaultRepo().getById(s);
            if (!schem.isPresent()) {
                throw new CommandException("Could not find schematic for '%s'", s);
            }
            return schem.get();
        });
    }

    private static void brush(ElementFactory.Builder builder) {
        builder.options(BrushType.class, BrushRegistry.getInstance()::getAliases);
        builder.filter(BrushType.class, Filter.STARTS_WITH);
        builder.parser(BrushType.class, s -> {
            Optional<BrushType> type = BrushRegistry.getInstance().getById(s);
            if (!type.isPresent()) {
                throw new CommandException("Invalid BrushType '%s'", s);
            }
            return type.get();
        });
    }

    // needs to be added before ValueElement
    private static void stencilPalette(ElementFactory.Builder builder) {
        final ValueParser<?> stateParser = builtin.getParser(BlockState.class);
        final ValueParser<?> weightParser = builtin.getParser(double.class);
        final Options stateOptions = builtin.getOptions(BlockState.class);
        final Filter stateFilter = builtin.getFilter(BlockState.class);

        ElementProvider provider = (key, priority, options, filter, parser) -> {
            ChainElement.Builder<Palette, Palette> chainBuilder = ChainElement.<Palette, Palette>builder()
                    .key(key)
                    .dependency(Palette.class)
                    .filter(stateFilter)
                    .options(palette -> stateOptions.get())
                    .mapper((input, palette) -> {
                        BlockState state = (BlockState) stateParser.parse(input);
                        Double weight = 1D;
                        if (input.hasNext()) {
                            weight = (Double) weightParser.parse(input);
                        }
                        return palette.add(state, weight);
                    });

            return new StencilPaletteElement(key, chainBuilder);
        };

        builder.provider(Palette.class, provider);
    }

    private static void option(ElementFactory.Builder builder) {
        ElementProvider provider = (key, priority, options, filter, parser) -> {
            ChainElement.Builder<BrushType, Option> chainBuilder = ChainElement.builder();
            chainBuilder.key(key)
                    .filter(filter)
                    .dependency(BrushType.class)
                    .options(type -> type.getOptions().stream().map(Option::getId))
                    .mapper((input, type) -> {
                        String next = input.next();
                        Optional<Option<?>> option = type.getOption(next);
                        if (!option.isPresent()) {
                            throw new CommandException("Invalid Option '%s' for Brush '%s'", next, type);
                        }
                        return option.get();
                    });
            return new OptionElement(chainBuilder);
        };

        builder.provider(Option.class, provider);
    }

    private static void value(ElementFactory.Builder builder) {
        ElementFactory factory = builder.build();

        ElementProvider provider = (key, priority, options, filter, parser) -> {
            ChainElement.Builder<Option, Value<?>> chainBuilder = ChainElement.<Option, Value<?>>builder()
                    .key(key)
                    .filter(Filter.CONTAINS)
                    .dependency(Option.class)
                    .options(option -> factory.getOptions(option.getType()).get())
                    .mapper((input, option) -> {
                        ValueParser<?> p = factory.getParser(option.getType());
                        Object value = p.parse(input);
                        if (value == null) {
                            throw new CommandException("Unable to parse value");
                        }

                        if (!option.validate(value)) {
                            throw new CommandException(
                                    "The value '%s' (%s) is not valid for option '%s' (%s)",
                                    value,
                                    value.getClass().getSimpleName(),
                                    option,
                                    option.getUsage()
                            );
                        }
                        return Value.of(value);
                    });
            return new OptionValueElement(chainBuilder);
        };

        builder.provider(Value.class, provider);
    }
}
