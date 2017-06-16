package me.dags.copy.clipboard;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Formatter;
import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerData;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.Inventory2D;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeMapper;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class ReMapper implements BlockVolumeMapper {

    private final Multimap<BlockType, StateMapper> mappers;

    private ReMapper(Builder builder) {
        this.mappers = ArrayListMultimap.create(builder.mappers);
    }

    public BlockState map(BlockState state) {
        Collection<StateMapper> mappers = this.mappers.get(state.getType());
        for (StateMapper mapper : mappers) {
            if (mapper.matches(state)) {
                state = mapper.apply(state);
                break;
            }
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

        private final Multimap<BlockType, StateMapper> mappers = ArrayListMultimap.create();

        public Builder block(BlockState from, BlockState to, BlockTrait<?>... traits) {
            BlockType matchType = from.getType();
            ImmutableMap.Builder<BlockTrait, Object> matchTraits = ImmutableMap.builder();

            for (BlockTrait<?> trait : traits) {
                Optional<?> v = from.getTraitValue(trait);
                if (v.isPresent() && to.getTraitValue(trait).isPresent()) {
                    matchTraits.put(trait, v.get());
                }
            }

            this.mappers.put(matchType, new StateMapper(to, matchType, matchTraits.build()));

            return this;
        }

        ReMapper build() {
            return new ReMapper(this);
        }
    }

    private static class StateMapper {

        private final BlockState toState;
        private final BlockType fromType;
        private final Map<BlockTrait, ?> fromTraits;

        private StateMapper(BlockState toState, BlockType fromType, Map<BlockTrait, Object> fromTraits) {
            this.toState = toState;
            this.fromType = fromType;
            this.fromTraits = fromTraits;
        }

        private boolean matches(BlockState state) {
            if (state.getType() != fromType) {
                return false;
            }

            for (Map.Entry<BlockTrait, ?> entry : fromTraits.entrySet()) {
                Optional<?> value = state.getTraitValue(entry.getKey());
                if (!value.isPresent() || value.get().equals(entry.getValue())) {
                    return false;
                }
            }

            return true;
        }

        private BlockState apply(BlockState in) {
            BlockState out = toState;
            for (Map.Entry<BlockTrait<?>, ?> entry : in.getTraitMap().entrySet()) {
                BlockTrait<?> trait = entry.getKey();
                if (fromTraits.containsKey(entry.getKey())) {
                    out = out.withTrait(trait, entry.getValue()).orElse(out);
                }
            }
            return out;
        }
    }

    public static void showMenu(Player player, final BlockTrait<?>... traits) {
        final WeakReference<Player> playerRef = new WeakReference<>(player);

        Inventory.Builder builder = Inventory.builder();
        builder.of(InventoryArchetypes.CHEST);
        builder.property("title", new InventoryTitle(Text.of("Replacements")));
        builder.listener(InteractInventoryEvent.Close.class, close -> {
            Player p = playerRef.get();
            if (p == null) {
                return;
            }

            Container container = close.getTargetInventory();
            GridInventory inventory = container.query(Inventory2D.class);

            ReMapper.Builder mapper = ReMapper.builder();
            Formatter fmt = Fmt.info("Replacements:");

            for (int y = 0; y + 1 < inventory.getRows(); y += 2) {
                for (int x = 0; x < inventory.getColumns(); x++) {
                    Optional<BlockState> from = inventory.peek(x, y).flatMap(s -> s.get(Keys.ITEM_BLOCKSTATE));
                    Optional<BlockState> to = inventory.peek(x, y + 1).flatMap(s -> s.get(Keys.ITEM_BLOCKSTATE));
                    if (from.isPresent() && to.isPresent()) {
                        mapper.block(from.get(), to.get(), traits);
                        fmt.line().stress(from.get().getName()).info(" -> ").stress(to.get().getName());
                    }
                }
            }

            PlayerData data = CopyPasta.getInstance().getData(p);
            data.ensureOptions().setMapper(mapper.build());
            fmt.tell(p);
        });

        Inventory inventory = builder.build(CopyPasta.getInstance());
        player.openInventory(inventory, Cause.source(CopyPasta.getInstance()).build());
    }
}
