package me.dags.copy.command.element;

import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.ChainElement;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import me.dags.copy.registry.brush.BrushType;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
class OptionElement extends ChainElement<BrushType, Option> {

    OptionElement(Builder<BrushType, Option> builder) {
        super(builder);
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        ensureBrush(context);
        super.parse(input, context);
    }

    @Override
    public Collection<String> getOptions(Context context) {
        ensureBrush(context);
        return super.getOptions(context);
    }

    private void ensureBrush(Context context) {
        BrushType type = context.getOne(BrushType.class.getCanonicalName());
        if (type == null) {
            Optional<Player> source = context.getSource(Player.class);
            if (source.isPresent()) {
                Player player = source.get();
                Optional<Brush> brush = PlayerManager.getInstance().get(player).flatMap(d -> d.getBrush(player));
                if (brush.isPresent()) {
                    context.add(BrushType.class.getCanonicalName(), brush.get().getType());
                }
            }
        }
    }
}
