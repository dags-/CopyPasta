package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import java.util.Optional;
import me.dags.commandbus.fmt.Formatter;
import me.dags.copy.CopyPasta;
import me.dags.copy.brush.AbstractBrush;
import me.dags.copy.brush.Action;
import me.dags.copy.brush.History;
import me.dags.copy.brush.option.Options;
import me.dags.copy.util.fmt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.permission.PermissionService;

/**
 * @author dags <dags@dags.me>
 */
public class SelectorBrush extends AbstractBrush {

    private static final int LIMIT = 50000;
    private static final BlockState POS1 = Sponge.getRegistry()
            .getType(BlockState.class, "minecraft:stained_glass[color=orange]")
            .orElse(BlockTypes.EMERALD_BLOCK.getDefaultState());

    private static final BlockState POS2 = Sponge.getRegistry()
            .getType(BlockState.class, "minecraft:stained_glass[color=blue]")
            .orElse(BlockTypes.LAPIS_BLOCK.getDefaultState());

    private final ClipboardBrush clipboardBrush;

    private Vector3i pos1 = Vector3i.ZERO;
    private Vector3i pos2 = Vector3i.ZERO;

    SelectorBrush(ClipboardBrush clipboardBrush) {
        super(0);
        this.clipboardBrush = clipboardBrush;
    }

    @Override
    public Options getOptions() {
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
            resetPos1Marker(player);
            pos1 = pos;
            tellPos(player, "pos1", pos);
            CopyPasta.getInstance().submitSync(() -> player.sendBlockChange(pos1, POS1));
        }
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        if (action == Action.SECONDARY) {
            if (pos1 == Vector3i.ZERO) {
                fmt.error("Pos1 has not been set").tell(player);
                return;
            }
            if (pos2 == Vector3i.ZERO) {
                fmt.error("Pos2 has not been set").tell(player);
                return;
            }

            int size = getSize(pos1, pos2);
            int limit = getLimit(player);

            if (size <= limit) {
                resetPos1Marker(player);
                resetPos2Marker(player);
                Vector3i min = pos1.min(pos2);
                Vector3i max = pos1.max(pos2);
                clipboardBrush.commitSelection(player, min, max, pos, size);
            } else {
                fmt.error("Selection size is too large: ").stress(size).info(" / ").stress(limit).tell(player);
            }
        } else {
            resetPos2Marker(player);
            pos2 = pos;
            tellPos(player, "pos2", pos);
            CopyPasta.getInstance().submitSync(() -> player.sendBlockChange(pos2, POS2));
        }
    }

    @Override
    public void apply(Player player, Vector3i pos, History history) {

    }

    @Override
    public void undo(Player player, History history) {

    }

    private void reset(Player player) {
        resetPos1Marker(player);
        resetPos2Marker(player);
        pos1 = Vector3i.ZERO;
        pos2 = Vector3i.ZERO;
        fmt.info("Reset points").tell(player);
    }

    private void resetPos1Marker(Player player) {
        if (pos1 != Vector3i.ZERO) {
            player.resetBlockChange(pos1);
        }
    }

    private void resetPos2Marker(Player player) {
        if (pos2 != Vector3i.ZERO) {
            player.resetBlockChange(pos2);
        }
    }

    private void tellPos(Player player, String posName, Vector3i pos) {
        Formatter f = fmt.info("Set %s ", posName).stress(pos);

        if (pos1 != Vector3i.ZERO && pos2 != Vector3i.ZERO) {
            f.info(" (%s blocks)", getSize(pos1, pos2));
        }

        f.tell(player);
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
        }

        return LIMIT;
    }
}
