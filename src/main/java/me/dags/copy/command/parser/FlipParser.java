package me.dags.copy.command.parser;

import com.google.common.collect.ImmutableList;
import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.function.Filter;
import me.dags.copy.block.property.Axis;
import me.dags.copy.block.property.Facing;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.value.Flip;
import org.spongepowered.api.entity.living.player.Player;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class FlipParser implements OptionValueParser {

    private final Collection<String> options = ImmutableList.<String>builder()
            .add("north").add("south").add("east").add("west").add("up").add("down")
            .add("x").add("y").add("z")
            .add("auto").add("random")
            .build();

    @Override
    public Object parse(Input input, Context context, Option<?> option) throws CommandException {
        Brush brush = brush(context, Flip.OPTION);
        Flip flip = brush.getOption(Flip.OPTION);
        if (input.hasNext()) {
            String next = input.next();
            parse(next, flip);
        } else {
            Optional<Player> source = context.getSource(Player.class);
            if (!source.isPresent()) {
                throw new CommandException("No player present");
            }

            Player player = source.get();
            Axis axis = Facing.getFacing(player).getAxis();
            switch (axis) {
                case x:
                    flip.setX(!flip.flipX());
                    break;
                case y:
                    flip.setX(!flip.flipY());
                    break;
                case z:
                    flip.setX(!flip.flipZ());
                    break;
            }
        }
        return flip;
    }

    @Override
    public Collection<String> suggest(Input input, Context context, Option<?> option) {
        Optional<Brush> optional = getBrush(context, Flip.OPTION);
        if (optional.isPresent()) {
            Brush brush = optional.get();
            if (brush.supports(Flip.OPTION)) {
                try {
                    String search = "";
                    if (input.hasNext()) {
                        search = input.next().toLowerCase();
                    }

                    List<String> suggestions = new LinkedList<>();
                    for (String arg : options) {
                        if (arg.equalsIgnoreCase(search)) {
                            return Collections.emptyList();
                        }

                        if (Filter.STARTS_WITH.test(arg, search)) {
                            suggestions.add(arg);
                        }
                    }
                    return suggestions;
                } catch (CommandException e) {
                    e.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }

    private static void parse(String input, Flip flip) throws CommandException {
        switch (input) {
            case "r":
            case "rand":
            case "random":
                flip.setRandom(!flip.random());
                return;
            case "a":
            case "auto":
                flip.setAuto(!flip.auto());
                return;
            case "n":
            case "s":
            case "z":
            case "north":
            case "south":
                flip.setZ(!flip.flipZ());
                return;
            case "e":
            case "w":
            case "x":
            case "east":
            case "west":
                flip.setX(!flip.flipX());
                return;
            case "u":
            case "d":
            case "y":
            case "up":
            case "down":
                flip.setY(!flip.flipY());
                return;
            default:
                throw new CommandException("Invalid input " + input);
        }
    }
}
