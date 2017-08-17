package me.dags.copy.clipboard;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Formatter;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerData;
import me.dags.copy.block.TraitUtils;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeMapper;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dags <dags@dags.me>
 */
public class StateMapper implements BlockVolumeMapper {

    static final StateMapper EMPTY = new StateMapper() {
        @Override
        public BlockState map(BlockState state) {
            return state;
        }
    };

    private final Multimap<BlockType, Mapper> mappers;

    private StateMapper() {
        this.mappers = null;
    }

    private StateMapper(Builder builder) {
        this.mappers = ArrayListMultimap.create(builder.mappers);
    }

    public BlockState map(BlockState state) {
        Collection<Mapper> mappers = this.mappers.get(state.getType());
        for (Mapper mapper : mappers) {
            state = mapper.apply(state);
        }
        return state;
    }

    @Override
    public BlockState map(UnmodifiableBlockVolume volume, int x, int y, int z) {
        return map(volume.getBlock(x, y, z));
    }

    private static Builder builder() {
        return new Builder();
    }

    private static class Builder {

        private final Multimap<BlockType, Mapper> mappers = ArrayListMultimap.create();

        public Builder map(BlockState from, BlockState to, String... traits) {
            BlockType fromType = from.getType();
            ImmutableMap.Builder<String, Object> matchTraits = ImmutableMap.builder();

            for (String trait : traits) {
                Object value = TraitUtils.getTraitValue(from, trait);
                if (value != null && to.getTrait(trait).isPresent()) {
                    matchTraits.put(trait, value);
                }
            }

            this.mappers.put(fromType, new Mapper(to, fromType, matchTraits.build()));

            return this;
        }

        StateMapper build() {
            return new StateMapper(this);
        }
    }

    private static class Mapper {

        private final BlockType fromState;
        private final Map<String, ?> matchTraits;

        private final BlockState toState;
        private final Map<BlockTrait<?>, ?> toTraits;

        private Mapper(BlockState toState, BlockType fromType, Map<String, Object> matchTraits) {
            this.toState = toState;
            this.fromState = fromType;
            this.matchTraits = matchTraits;
            this.toTraits = toState.getTraitMap();
        }

        private BlockState apply(BlockState in) {
            if (fromState != in.getType()) {
                return in;
            }

            for (Map.Entry<String, ?> match : matchTraits.entrySet()) {
                Object val = TraitUtils.getTraitValue(in, match.getKey());
                if (!TraitUtils.equals(val, match.getValue())) {
                    return in;
                }
            }

            BlockState out = toState;
            for (Map.Entry<BlockTrait<?>, ?> e : toTraits.entrySet()) {
                String trait = e.getKey().getName();

                // Trait already set in 'toState' .: do not change
                if (matchTraits.containsKey(trait)) {
                    continue;
                }

                // If trait exists in both states, carry over the value from the existing map
                Object value = TraitUtils.getTraitValue(in, trait);
                out = TraitUtils.withTrait(out, e.getKey(), value);
            }

            return out;
        }
    }

    public static void showMenu(Player player, final String... traits) {
        final WeakReference<Player> playerRef = new WeakReference<>(player);
        final AtomicReference<Inventory> reference = new AtomicReference<>();

        Inventory.Builder builder = Inventory.builder();
        builder.of(InventoryArchetypes.CHEST);
        builder.property("title", new InventoryTitle(Text.of("Replacements")));
        builder.listener(InteractInventoryEvent.Close.class, close -> {
            Player p = playerRef.get();
            if (p == null) {
                return;
            }

            // close.getTargetInventory() returns the player's inventory, not the custom one...
            Inventory inventory = reference.get();
            StateMapper.Builder mapper = StateMapper.builder();
            Formatter fmt = Fmt.info("Replacements:");

            // not sure if GridInventory etc is implemented
            for (int x = 0; x < 9; x++) {
                Optional<BlockState> from = inventory.query(new SlotPos(x, 0)).peek().flatMap(s -> s.get(Keys.ITEM_BLOCKSTATE));
                Optional<BlockState> to = inventory.query(new SlotPos(x, 1)).peek().flatMap(s -> s.get(Keys.ITEM_BLOCKSTATE));
                if (from.isPresent() && to.isPresent()) {
                    mapper.map(from.get(), to.get(), traits);
                    fmt.line().stress(from.get().getType()).info(" -> ").stress(to.get().getType());
                }
            }

            PlayerData data = CopyPasta.getInstance().getData(p);
            data.ensureOptions().setStateMapper(mapper.build());
            fmt.tell(p);
        });

        reference.set(builder.build(CopyPasta.getInstance()));
        player.openInventory(reference.get(), Cause.source(CopyPasta.getInstance()).build());
    }
}
