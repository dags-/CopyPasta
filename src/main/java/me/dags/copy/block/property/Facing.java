package me.dags.copy.block.property;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;

/**
 * @author dags <dags@dags.me>
 */
public enum Facing implements Property<Facing> {

    north(Axis.z),
    east(Axis.x),
    south(Axis.z),
    west(Axis.x),
    up(Axis.y),
    down(Axis.y),
    none(Axis.x),;

    public static final Facing[] VALUES = {north, east, south, west, up, down};
    public static final Facing[] ROTX = {up, north, down, south};
    public static final Facing[] ROTY = {north, east, south, west};
    public static final Facing[] ROTZ = {up, east, down, west};

    static {
        for (Facing facing : values()) {
            facing.init();
        }
    }

    private int x, y, z;
    private final Axis axis;
    private final String property = "facing=" + this.toString();

    Facing(Axis axis) {
        this.axis = axis;
    }

    private void init() {
        x = indexOf(ROTX);
        y = indexOf(ROTY);
        z = indexOf(ROTZ);
    }

    private int getRotation(Axis axis) {
        switch (axis) {
            case x:
                return x;
            case y:
                return y;
            case z:
                return z;
            default:
                return -1;
        }
    }

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public Facing rotate(Axis axis, int angle) {
        if (axis != this.axis) {
            int rotation = (angle / 90);
            switch (axis) {
                case x:
                    return ROTX[(x + rotation) % 4];
                case y:
                    return ROTY[(y + rotation) % 4];
                case z:
                    return ROTZ[(z + rotation) % 4];
            }
        }
        return this;
    }

    @Override
    public Facing flip(Axis axis) {
        if (axis == this.axis) {
            switch (axis) {
                case x:
                    return this == east ? west : east;
                case y:
                    return this == up ? down : up;
                case z:
                    return this == north ? south : north;
            }
        }
        return this;
    }

    public Axis getAxis() {
        return axis;
    }

    public int angle(Facing other, Axis axis) {
        int i = getRotation(axis);
        int j = other.getRotation(axis);

        if (i == -1 || j == -1) {
            return 0;
        }

        int angle = (j * 90) - (i * 90);
        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    private Facing or(Facing other) {
        return this == none ? other : this;
    }

    private int indexOf(Facing[] facings) {
        for (int i = 0; i < facings.length; i++) {
            if (this == facings[i]) {
                return i;
            }
        }
        return -1;
    }

    public static Facing getFacing(Player player) {
        return getVertical(player).or(getHorizontal(player));
    }

    public static Facing getHorizontal(Player player) {
        Vector3d rotation = getRotation(player);
        Direction direction = Direction.getClosestHorizontal(rotation, Direction.Division.CARDINAL);
        Facing facing = fromDirection(direction);
        return facing != null ? facing : north;
    }

    public static Facing getVertical(Player player) {
        Vector3d rotation = player.getRotation();
        if (rotation.getX() > 45) {
            return up;
        }
        if (rotation.getX() < -45) {
            return down;
        }
        return none;
    }

    private static Vector3d getRotation(Player player) {
        Vector3d rotation = player.getRotation();
        return Quaterniond.fromAxesAnglesDeg(0, -rotation.getY(), rotation.getZ()).getDirection();
    }

    private static Facing fromDirection(Direction direction) {
        switch (direction) {
            case NORTH:
                return north;
            case EAST:
                return east;
            case SOUTH:
                return south;
            case WEST:
                return west;
            case UP:
                return up;
            case DOWN:
                return down;
            default:
                return null;
        }
    }
}
