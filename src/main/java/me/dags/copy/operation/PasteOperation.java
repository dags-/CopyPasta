package me.dags.copy.operation;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.CopyPasta;
import me.dags.copy.brush.History;
import me.dags.copy.event.BrushEvent;
import me.dags.copy.event.BrushPlaceEvent;
import me.dags.copy.event.LocatableBlockChange;
import me.dags.copy.fmt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class PasteOperation implements Operation {

    private final boolean air;
    private final UUID owner;
    private final Cause cause;
    private final History history;
    private final Vector3i position;
    private final BlockVolume source;
    private final WeakReference<World> world;

    private final List<LocatableBlockChange> transactions = new LinkedList<>();

    private boolean cancelled = false;

    public PasteOperation(Cause cause, WeakReference<World> world, UUID uuid, BlockVolume source, Vector3i position, History history, boolean air) {
        this.air = air;
        this.cause = cause;
        this.owner = uuid;
        this.world = world;
        this.source = source;
        this.history = history;
        this.position = position;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void calculate() {
        final World world = this.world.get();

        if (world == null) {
            cancelled = true;
            return;
        }

        source.getBlockWorker(cause).iterate((v, x, y, z) -> {
            BlockState state = v.getBlock(x, y, z);
            if (state.getType() == BlockTypes.AIR && !air) {
                return;
            }

            x += position.getX();
            y += position.getY();
            z += position.getZ();

            if (!world.containsBlock(x, y, z)) {
                return;
            }

            Location<World> location = world.getLocation(x, y, z);
            if (location.getBlock() != state) {
                transactions.add(new LocatableBlockChange(location, state));
            }
        });
    }

    @Override
    public void test() {
        World world = this.world.get();
        if (world == null) {
            cancelled = true;
            return;
        }

        BrushEvent place = new BrushPlaceEvent(transactions, world, cause);
        Sponge.getEventManager().post(place);

        if (place.isCancelled()) {
            cancelled = true;
        }
    }

    @Override
    public void apply() {
        final World world = this.world.get();
        final Optional<Player> player = Sponge.getServer().getPlayer(owner);

        if (world == null || !player.isPresent()) {
            cancelled = true;
            return;
        }

        List<BlockSnapshot> record = history.nextRecord();
        for (LocatableBlockChange transaction : transactions) {
            if (transaction.isValid()) {
                Location<World> location = transaction.getLocation();
                record.add(location.createSnapshot());
                world.setBlock(location.getBlockPosition(), transaction.getEndState(), BlockChangeFlag.NONE, cause);
                world.setCreator(location.getBlockPosition(), owner);
                world.setNotifier(location.getBlockPosition(), owner);
            }
        }

        fmt.stress("Paste complete").tell(CopyPasta.NOTICE_TYPE, player.get());
    }

    @Override
    public void dispose() {
        CopyPasta.getInstance().getData(owner).ifPresent(data -> data.setOperating(false));
    }
}
