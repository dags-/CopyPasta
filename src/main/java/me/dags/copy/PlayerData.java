package me.dags.copy;

import com.google.common.base.Stopwatch;
import me.dags.copy.clipboard.Clipboard;
import me.dags.copy.clipboard.ClipboardOptions;
import me.dags.copy.clipboard.Selector;
import org.spongepowered.api.item.ItemType;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class PlayerData {

    private ItemType wand = null;
    private Selector selector = null;
    private Clipboard clipboard = null;
    private ClipboardOptions options = null;

    private boolean operating = false;
    private Stopwatch limiter = Stopwatch.createStarted();

    public Optional<ItemType> getWand() {
        return Optional.ofNullable(wand);
    }

    public Optional<Selector> getSelector() {
        return Optional.ofNullable(selector);
    }

    public Optional<Clipboard> getClipboard() {
        return Optional.ofNullable(clipboard);
    }

    public Optional<ClipboardOptions> getOptions() {
        return Optional.ofNullable(options);
    }

    public Selector ensureSelector() {
        if (selector == null) {
            selector = new Selector();
        }
        return selector;
    }

    public ClipboardOptions ensureOptions() {
        if (options == null) {
            options = new ClipboardOptions();
        }
        return options;
    }

    public boolean isRateLimited() {
        long time = limiter.elapsed(TimeUnit.MILLISECONDS);

        if (time > 250) {
            limiter.reset().start();
            return false;
        }

        return true;
    }

    public boolean isOperating() {
        return operating;
    }

    public void setOperating(boolean operating) {
        this.operating = false;
    }

    public void setWand(ItemType wand) {
        this.wand = wand;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public void setClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    public void setOptions(ClipboardOptions options) {
        this.options = options;
    }

    public void clear() {
        wand = null;
        selector = null;
        clipboard = null;
        options = null;
    }
}
