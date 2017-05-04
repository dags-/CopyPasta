package me.dags.copy.operation;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public final class LocatableBlockChange {

    private final Location<World> location;
    private final BlockState endState;

    private boolean valid = true;

    public LocatableBlockChange(Location<World> location, BlockState state) {
        this.location = location;
        this.endState = state;
    }

    public boolean isValid() {
        return valid;
    }

    public BlockState getEndState() {
        return endState;
    }

    public Location<World> getLocation() {
        return location;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
