package me.dags.copy.brush.stencil;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Action;
import me.dags.copy.brush.History;
import me.dags.copy.brush.option.Option;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public class StencilBrush extends AbstractBrush {

    public static final Option<Stencil> STENCIL = Option.of("stencil", Stencil.EMPTY);
    public static final Option<BlockState> MATERIAL = Option.of("material", BlockTypes.SAND.getDefaultState());

    @Override
    public String getPermission() {
        return "brush.stencil";
    }

    @Override
    public void primary(Player player, Vector3i pos, Action action) {

    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {

    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        Stencil stencil = getOption(STENCIL);
        BlockState state = getOption(MATERIAL);

        if (!stencil.isPresent()) {
            return;
        }


    }

    @Override
    public void undo(Player player, History history) {

    }
}
