package me.dags.copy.operation;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.format.FMT;
import me.dags.copy.CopyPasta;
import me.dags.copy.clipboard.History;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.ChangeBlockEvent;
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

    private final UUID owner;
    private final Cause cause;
    private final History history;
    private final Vector3i position;
    private final BlockVolume source;
    private final WeakReference<World> world;

    private final List<LocatableBlockChange> transactions = new LinkedList<>();

    private boolean cancelled = false;

    public PasteOperation(Cause cause, WeakReference<World> world, UUID uuid, BlockVolume source, Vector3i position, History history) {
        this.cause = cause;
        this.owner = uuid;
        this.world = world;
        this.history = history;
        this.position = position;
        this.source = source;
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
            if (state.getType() == BlockTypes.AIR) {
                return;
            }

            x += position.getX();
            y += position.getY();
            z += position.getZ();

            if (!world.containsBlock(x, y, z)) {
                return;
            }

            Location<World> location = world.getLocation(x, y, z);
            transactions.add(new LocatableBlockChange(location, state));
        });
    }

    @Override
    public void test() {
        World world = this.world.get();
        if (world == null) {
            cancelled = true;
            return;
        }

        ChangeBlockEvent.Place place = new TestPlaceEvent(transactions, world, cause);
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
                location.setBlock(transaction.getEndState(), BlockChangeFlag.NONE, cause);
            }
        }

        FMT.stress("Paste complete").tell(CopyPasta.CHAT_TYPE, player.get());
    }

    @Override
    public void dispose() {
        CopyPasta.getInstance().getData(owner).ifPresent(data -> data.setOperating(false));
    }
}
