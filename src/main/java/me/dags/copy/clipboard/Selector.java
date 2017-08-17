package me.dags.copy.clipboard;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.fmt.Fmt;
import me.dags.copy.CopyPasta;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.permission.PermissionService;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Selector {

    private static final int LIMIT = 50000;

    private int range = 4;
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
        Fmt.subdued("Reset clipboard").tell(player);
        CopyPasta.getInstance().ensureData(player).setClipboard(null);
    }

    public void pos(Player player, Vector3i pos) {
        if (pos1 == Vector3i.ZERO) {
            pos1 = pos;
            Fmt.info("Set pos1 ").stress(pos).tell(player);
        } else if (pos2 == Vector3i.ZERO) {
            pos2 = pos;
            int size = getSize(pos1, pos2);
            Fmt.info("Set pos2 ").stress(pos).info(" (%s blocks)", size).tell(player);
        } else if (player.get(Keys.IS_SNEAKING).orElse(false)) {
            int size = getSize(pos1, pos2);
            int limit = getLimit(player);
            if (size <= limit) {
                Vector3i min = pos1.min(pos2);
                Vector3i max = pos1.max(pos2);

                Clipboard clipboard = Clipboard.of(player, min, max, pos);
                CopyPasta.getInstance().ensureData(player).setClipboard(clipboard);
                Fmt.info("Copied ").stress(size).info(" blocks").tell(player);
            } else {
                Fmt.error("Selection size is too large: ").stress(size).info(" / ").stress(limit).tell(player);
            }
        } else {
            reset(player);
        }
    }

    private int getLimit(Player player) {
        Optional<?> permissionService = Sponge.getServiceManager().getRegistration(PermissionService.class)
                .map(ProviderRegistration::getPlugin)
                .map(PluginContainer::getId)
                .filter(name -> !name.equals("sponge"));

        if (permissionService.isPresent()) {
            Optional<String> size = player.getOption("copypasta.limit");
            if (size.isPresent()) {
                try {
                    return Integer.parseInt(size.get());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return 0;
        }

        return LIMIT;
    }

    private int getSize(Vector3i pos1, Vector3i pos2) {
        Vector3i min = pos1.min(pos2);
        Vector3i max = pos1.max(pos2);
        int lx = max.getX() - min.getX() + 1;
        int ly = max.getY() - min.getY() + 1;
        int lz = max.getZ() - min.getZ() + 1;
        return lx * ly * lz;
    }
}
