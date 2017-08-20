package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Formatter;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Action;
import me.dags.copy.registry.option.BrushOptions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.permission.PermissionService;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class SelectorBrush extends AbstractBrush {

    private static final int LIMIT = 50000;

    private final ClipboardBrush clipboardBrush;
    private Vector3i pos1 = Vector3i.ZERO;
    private Vector3i pos2 = Vector3i.ZERO;

    SelectorBrush(ClipboardBrush clipboardBrush) {
        this.clipboardBrush = clipboardBrush;
    }

    public void reset(Player player) {
        pos1 = Vector3i.ZERO;
        pos2 = Vector3i.ZERO;
        Fmt.info("Reset points").tell(player);
    }

    @Override
    public BrushOptions getOptions() {
        return clipboardBrush.getOptions();
    }

    @Override
    public String getPermission() {
        return clipboardBrush.getPermission();
    }

    @Override
    public void primary(Player player, Vector3i pos, Action action) {
        if (action == Action.SECONDARY) {
            reset(player);
        } else {
            pos1 = pos;
            tellPos(player, "pos1", pos);
        }
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        if (action == Action.SECONDARY) {
            if (pos1 == Vector3i.ZERO) {
                Fmt.error("Pos1 has not been set").tell(player);
                return;
            }
            if (pos2 == Vector3i.ZERO) {
                Fmt.error("Pos2 has not been set").tell(player);
                return;
            }

            int size = getSize(pos1, pos2);
            int limit = getLimit(player);

            if (size <= limit) {
                Vector3i min = pos1.min(pos2);
                Vector3i max = pos1.max(pos2);
                Clipboard clipboard = Clipboard.of(player, min, max, pos);
                clipboardBrush.setClipboard(clipboard);
                Fmt.info("Copied ").stress(size).info(" blocks").tell(player);
            } else {
                Fmt.error("Selection size is too large: ").stress(size).info(" / ").stress(limit).tell(player);
            }

        } else {
            pos2 = pos;
            tellPos(player, "pos2", pos);
        }
    }

    private void tellPos(Player player, String posName, Vector3i pos) {
        Formatter fmt = Fmt.info("Set %s ", posName).stress(pos);

        if (pos1 != Vector3i.ZERO && pos2 != Vector3i.ZERO) {
            fmt.info(" (%s blocks)", getSize(pos1, pos2));
        }

        fmt.tell(player);
    }

    private static int getSize(Vector3i pos1, Vector3i pos2) {
        Vector3i min = pos1.min(pos2);
        Vector3i max = pos1.max(pos2);
        int lx = max.getX() - min.getX() + 1;
        int ly = max.getY() - min.getY() + 1;
        int lz = max.getZ() - min.getZ() + 1;
        return lx * ly * lz;
    }

    private static int getLimit(Player player) {
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
}
