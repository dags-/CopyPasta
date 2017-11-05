package me.dags.copy.brush;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.brush.option.Options;
import me.dags.copy.util.fmt;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public abstract class AbstractBrush implements Brush {

    private final Options options;
    private final History history;

    protected AbstractBrush() {
        this(0);
    }

    protected AbstractBrush(int size) {
        this.options = new Options();
        this.history = new History(size);
    }

    @Override
    public void primary(Player player, Vector3i pos, Action action) {
        if (PlayerManager.getInstance().must(player).isOperating()) {
            fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }
        undo(player, getHistory());
    }

    @Override
    public void secondary(Player player, Vector3i pos, Action action) {
        if (PlayerManager.getInstance().must(player).isOperating()) {
            fmt.error("An operation is already in progress").tell(CopyPasta.NOTICE_TYPE, player);
            return;
        }
        apply(player, pos, getHistory());
    }

    @Override
    public History getHistory() {
        return history;
    }

    @Override
    public Options getOptions() {
        return options;
    }
}
