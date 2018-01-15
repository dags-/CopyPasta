package me.dags.copy.block.volume;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class BufferBuilder {

    private final List<Snapshot> list;
    private final Vector3i position;
    private final UUID owner;

    public BufferBuilder(UUID uuid, Vector3i position, int size) {
        this.owner = uuid;
        this.position = position;
        this.list = new ArrayList<>(size);
    }

    public void addRelative(BlockState state, int x, int y, int z) {
        addRelative(state, this.position.add(x, y, z));
    }

    public void addRelative(BlockState state, Vector3i position) {
        if (position.getY() >= 0 && position.getY() < 256) {
            list.add(new Snapshot(state, position, owner));
        }
    }

    public void addAbsolute(BlockState state, int x, int y, int z) {
        addAbsolute(state, new Vector3i(x, y, z));
    }

    public void addAbsolute(BlockState state, Vector3i position) {
        if (position.getY() >= 0 && position.getY() < 256) {
            list.add(new Snapshot(state, position, owner));
        }
    }

    public BufferView getView() {
        return new BufferView(list);
    }
}
