package me.dags.copy.block;

import me.dags.copy.block.property.Axis;
import me.dags.copy.block.state.State;

/**
 * @author dags <dags@dags.me>
 */
public class Mappers {

    private static final State.Mapper ROT90 = State.rotate(Axis.y, 90);
    private static final State.Mapper ROT180 = State.rotate(Axis.y, 180);
    private static final State.Mapper ROT270 = State.rotate(Axis.y, 270);
    private static final State.Mapper FLIPX = State.flip(Axis.x);
    private static final State.Mapper FLIPY = State.flip(Axis.y);
    private static final State.Mapper FLIPZ = State.flip(Axis.z);

    public static void init() {
        // instantiates mappers
    }

    public static State.Mapper getRotationY(int angle) {
        if (angle == 90) {
            return ROT90;
        }
        if (angle == 180) {
            return ROT180;
        }
        if (angle == 270) {
            return ROT270;
        }
        return State.emptyMapper();
    }

    public static State.Mapper getFlipX() {
        return FLIPX;
    }

    public static State.Mapper getFlipY() {
        return FLIPY;
    }

    public static State.Mapper getFlipZ() {
        return FLIPZ;
    }
}
