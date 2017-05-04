package me.dags.copy.block;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;

/**
 * @author dags <dags@dags.me>
 */
public enum Facing implements TraitValue, Rotatable<Facing>, Flippable<Facing> {
    north,
    east,
    south,
    west,
    up,
    down,
    horizontal,
    ;

    static {
        north.angle = 0;
        north.axis = Axis.z;
        north.opposite = south;

        east.angle = 90;
        east.axis = Axis.x;
        east.opposite = west;

        south.angle = 180;
        south.axis = Axis.z;
        south.opposite = north;

        west.angle = 270;
        west.axis = Axis.x;
        west.opposite = east;

        up.angle = -1;
        up.axis = Axis.y;
        up.opposite = down;

        down.angle = -1;
        down.axis = Axis.y;
        down.opposite = up;

        horizontal.angle = -1;
        horizontal.axis = null;
        horizontal.opposite = horizontal;
    }

    private int angle;
    private Axis axis;
    private Facing opposite;

    public int getAngle() {
        return angle;
    }

    public Axis getAxis() {
        return axis;
    }

    public Facing getOpposite() {
        return opposite;
    }

    @Override
    public Facing flip(Axis axis) {
        if (getAxis() == axis) {
            return getOpposite();
        }
        return null;
    }

    @Override
    public Facing rotate(Axis axis, int angle) {
        if (getAxis() != axis) {
            if (getAxis() == Axis.y && angle % 360 == 180) {
                return getOpposite();
            } else {
                int to = clampAngle(getAngle() + angle);
                return fromAngle(to);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name();
    }

    public static Facing fromDirection(Direction direction) {
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

    public static Facing fromName(String name) {
        switch (name) {
            case "north":
                return north;
            case "east":
                return east;
            case "south":
                return south;
            case "west":
                return west;
            case "up":
                return up;
            case "down":
                return down;
            default:
                return null;
        }
    }

    public static Facing fromAngle(int angle) {
        switch (angle) {
            case 0:
                return north;
            case 90:
                return east;
            case 180:
                return south;
            case 270:
                return west;
            default:
                return null;
        }
    }

    public static Facing facing(Player player) {
        Facing vertical = verticalFacing(player);
        return vertical != horizontal ? vertical : horizontalFacing(player);
    }

    public static Facing horizontalFacing(Player player) {
        Vector3d rotation = getRotation(player);
        Direction direction = Direction.getClosestHorizontal(rotation, Direction.Division.CARDINAL);
        Facing facing = fromDirection(direction);
        return facing != null ? facing : north;
    }

    public static Facing verticalFacing(Player player) {
        Vector3d rotation = player.getRotation();
        if (rotation.getX() > 45) {
            return up;
        }
        if (rotation.getX() < -45) {
            return down;
        }
        return horizontal;
    }

    public static int clampAngle(int angle) {
        if (angle < -359 || angle > 719) {
            angle = angle % 360;
        }
        return angle < 0 ? 360 + angle : angle > 359 ? angle - 360 : angle;
    }

    private static Vector3d getRotation(Player player) {
        Vector3d rotation = player.getRotation();
        return Quaterniond.fromAxesAnglesDeg(0, -rotation.getY(), rotation.getZ()).getDirection();
    }
}
