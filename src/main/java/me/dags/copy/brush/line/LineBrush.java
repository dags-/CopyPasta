package me.dags.copy.brush.line;

import com.flowpowered.math.vector.Vector3i;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import me.dags.commandbus.fmt.Fmt;
import me.dags.copy.CopyPasta;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Action;
import me.dags.copy.brush.Aliases;
import me.dags.copy.brush.History;
import me.dags.copy.brush.line.iterator.LineIterator;
import me.dags.copy.brush.option.Option;
import me.dags.copy.brush.option.value.Palette;
import me.dags.copy.brush.option.value.Translation;
import me.dags.copy.operation.IterateOperation;
import me.dags.copy.operation.modifier.Translate;
import me.dags.copy.registry.brush.BrushSupplier;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
@Aliases("line")
public class LineBrush extends AbstractBrush {

    public static final Option<Palette> PALETTE = Palette.OPTION;
    public static final Option<Integer> SIDES = Option.of("sides", 3);
    public static final Option<Boolean> CLOSED = Option.of("close", false);
    public static final Option<Line> LINE = Option.of("line", Line.LINE);
    public static final Option<Vector3i> OFFSET = Option.of("offset", Vector3i.ZERO);
    public static final Option<Translation> TRANSLATION = Option.of("translate", Translation.NONE);

    private List<Vector3i> points = new LinkedList<>();

    private LineBrush() {
        super(5);
    }

    @Override
    public void primary(Player player, Vector3i pos, Action action) {
        if (action == Action.PRIMARY) {
            super.primary(player, pos, action);
        } else {
            Fmt.subdued("Cleared points").tell(player);
            points = new LinkedList<>();
        }
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        if (action == Action.PRIMARY) {
            Fmt.info("Added point #%s: ", points.size()).stress(pos).tell(player);
            points.add(pos);
        } else {
            super.secondary(player, pos, action);
        }
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        Line line = getOption(LINE);
        Integer sides = getOption(SIDES);
        boolean closed = getOption(CLOSED);
        LineIterator iterator = line.newIterator(points, sides, closed);

        if (iterator == LineIterator.EMPTY) {
            Fmt.error("Wrong number of points: %s, required: %s", points.size(), line.points).tell(player);
            return;
        }

        UUID owner = player.getUniqueId();
        World world = player.getWorld();
        Palette palette = getOption(PALETTE);
        Translate translate = getOption(TRANSLATION).getModifier(pos, getOption(OFFSET));
        IterateOperation operation = new IterateOperation(world, owner, iterator, translate, palette, history);
        CopyPasta.getInstance().getOperationManager().queueOperation(operation);
    }

    public static BrushSupplier supplier() {
        return player -> new LineBrush();
    }
}
