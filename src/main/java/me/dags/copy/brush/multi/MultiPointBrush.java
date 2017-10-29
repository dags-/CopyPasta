package me.dags.copy.brush.multi;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerData;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.*;
import me.dags.copy.brush.option.Option;
import me.dags.copy.operation.UndoOperation;
import me.dags.copy.registry.brush.BrushSupplier;
import me.dags.copy.util.fmt;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
@Aliases({"multipoint", "multi", "mp"})
public class MultiPointBrush extends AbstractBrush {

    private static final Random RANDOM = new Random();

    public static final Option<Integer> RADIUS = Option.of("radius", 10);
    public static final Option<Float> DENSITY = Option.of("density", 0.5F);
    public static final Option<Integer> SPACING = Option.of("spacing", 2);

    private final Brush delegate;
    private History history = super.getHistory();

    public MultiPointBrush(Brush delegate) {
        super(0);
        this.delegate = delegate;
    }

    @Override
    public History getHistory() {
        return history;
    }

    @Override
    public String getPermission() {
        return delegate.getPermission();
    }

    @Override
    public void primary(Player player, Vector3i pos, Action action) {
        if (action == Action.PRIMARY) {
            undo(player, getHistory());
        }
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        PlayerData data = PlayerManager.getInstance().must(player);

        if (data.isOperating()) {
            fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }

        data.setOperating(true);

        if (action == Action.PRIMARY) {
            List<Vector3i> list = getLocations(pos);
            history = new History(list.size());
            for (Vector3i vec : list) {
                apply(player, vec, history);
            }
        }
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {
        delegate.apply(player, pos, history);
    }

    @Override
    public void undo(Player player, History history) {
        PlayerData data = PlayerManager.getInstance().must(player);

        if (data.isOperating()) {
            fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }

        if (!history.hasNext()) {
            fmt.error("No more history to undo").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }

        while (history.hasNext()) {
            LinkedList<BlockSnapshot> record = history.popRecord();
            UndoOperation operation = new UndoOperation(record, player.getUniqueId(), history);
            CopyPasta.getInstance().getOperationManager().queueOperation(operation);
        }
    }

    private List<Vector3i> getLocations(Vector3i center) {
        List<Vector3i> list = new LinkedList<>();
        float density = getOption(DENSITY);
        int radius = getOption(RADIUS);
        int spacing = getOption(SPACING);
        addPositions(list, center, radius, density, spacing);
        return list;
    }

    private static void addPositions(List<Vector3i> list, Vector3i center, int radius, float density, int spacing) {
        int r2 = radius * radius;
        int s2 = spacing * spacing;
        for (int dz = 0; dz <= radius; dz++) {
            for (int dx = 0; dx <= radius; dx++) {
                if (((dx * dx) + (dz * dz)) <= r2) {
                    addPoint(list, center, dx, dz, density, s2);
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    addPoint(list, center, dx, -dz, density, s2);
                    addPoint(list, center, -dx, -dz, density, s2);
                    addPoint(list, center, -dx, dz, density, s2);
                } else {
                    break;
                }
            }
        }
    }

    private static void addPoint(Collection<Vector3i> collection, Vector3i center, int dx, int dz, float density, int minDist2) {
        if (RANDOM.nextFloat() <= density) {
            int x = center.getX() + dx;
            int z = center.getZ() + dz;
            for (Vector3i pos : collection) {
                int px = x - pos.getX();
                int pz = z - pos.getZ();
                if (((px * px) + pz * pz) < minDist2) {
                    return;
                }
            }
            collection.add(center.add(dx, 0, dz));
        }
    }

    public static BrushSupplier supplier() {
        return player -> {
            PlayerData data = PlayerManager.getInstance().must(player);
            Optional<Brush> brush = data.getBrush(player);
            if (brush.isPresent()) {
                return new MultiPointBrush(brush.get());
            }
            fmt.error("You must be holding an existing brush").tell(player);
            return null;
        };
    }
}
