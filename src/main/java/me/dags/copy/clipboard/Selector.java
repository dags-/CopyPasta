package me.dags.copy.clipboard;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.format.FMT;
import me.dags.copy.CopyPasta;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public class Selector {

    private static final int defaultSize = 20000;
    private static final int extendedSize = 100000;

    private int range = 5;
    private Vector3i pos1 = Vector3i.ZERO;
    private Vector3i pos2 = Vector3i.ZERO;

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void reset(Player player) {
        pos1 = Vector3i.ZERO;
        pos2 = Vector3i.ZERO;
        FMT.subdued("Reset clipboard").tell(player);
        CopyPasta.getInstance().getData(player).setClipboard(null);
    }

    public void pos(Player player, Vector3i pos) {
        if (pos1 == Vector3i.ZERO) {
            pos1 = pos;
            FMT.info("Set pos1 ").stress(pos).tell(player);
        } else if (pos2 == Vector3i.ZERO) {
            pos2 = pos;
            int size = getSize(pos1, pos2);
            FMT.info("Set pos2 ").stress(pos).info(" (%s blocks)", size).tell(player);
        } else if (player.get(Keys.IS_SNEAKING).orElse(false)) {
            int size = getSize(pos1, pos2);
            int limit = getLimit(player);
            if (size <= limit) {
                Vector3i min = pos1.min(pos2);
                Vector3i max = pos1.max(pos2);

                Clipboard clipboard = Clipboard.of(player, min, max, pos);
                CopyPasta.getInstance().getData(player).setClipboard(clipboard);
                FMT.info("Copied ").stress(size).info(" blocks").tell(player);
            } else {
                FMT.error("Selection size is too large: ").stress(size).info(" / ").stress(limit).tell(player);
            }
        } else {
            reset(player);
        }
    }

    private int getSize(Vector3i pos1, Vector3i pos2) {
        Vector3i min = pos1.min(pos2);
        Vector3i max = pos1.max(pos2);
        int lx = max.getX() - min.getX() + 1;
        int ly = max.getY() - min.getY() + 1;
        int lz = max.getZ() - min.getZ() + 1;
        return lx * ly * lz;
    }

    private int getLimit(Player player) {
        return player.hasPermission("toolkit.wand.select.limit.expanded") ? extendedSize : defaultSize;
    }
}
