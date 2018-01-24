package me.dags.copy.registry.brush;

import me.dags.copy.PlayerManager;
import me.dags.copy.brush.Brush;
import me.dags.copy.brush.option.Option;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class BrushType {

    static final BrushType NONE = new BrushType();

    private final String name;
    private final String permission;
    private final List<Option<?>> options;
    private final BrushSupplier supplier;
    private final Class<? extends Brush> type;

    private BrushType() {
        name = "none";
        permission = "copypasta.wand.none";
        type =  Brush.class;
        supplier = p -> null;
        options = Collections.emptyList();
    }

    private BrushType(String name, Class<? extends Brush> type, BrushSupplier supplier, List<Option<?>> options) {
        this.name = name;
        this.permission = "copypasta.wand." + name.toLowerCase();
        this.options = options;
        this.type = type;
        this.supplier = supplier;
    }

    public Optional<Brush> create(Player player) {
        return create(player, true);
    }

    public Optional<Brush> create(Player player, boolean load) {
        if (player.hasPermission(permission)) {
            Brush brush = supplier.create(player);
            if (brush != null) {
                if (load) {
                    PlayerManager.getInstance().must(player).load(brush);
                }
                return Optional.of(brush);
            }
        }
        return Optional.empty();
    }

    public Class<? extends Brush> getType() {
        return type;
    }

    public Optional<Option<?>> getOption(String id) {
        for (Option<?> option : options) {
            if (option.getId().equals(id)) {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    public Collection<Option<?>> getOptions() {
        return options;
    }

    public String getId() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other != null && (other == this || other instanceof BrushType && ((BrushType) other).getId().equals(getId()));
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public static <T extends Brush> BrushType of(String name, Class<T> type, BrushSupplier supplier, List<Option<?>> options) {
        return new BrushType(name, type, supplier, options);
    }
}
