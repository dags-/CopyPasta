package me.dags.copy.block;

/**
 * @author dags <dags@dags.me>
 */
public enum Axis implements TraitValue, Rotatable<Axis> {
    x,
    y,
    z,
    ;

    @Override
    public Axis rotate(Axis axis, int angle) {
        if (this == axis) {
            return null;
        }

        if (angle % 180 == 90) {
            return null;
        }

        if (this == x) {
            if (axis == y) {
                return z;
            }
            return y;
        }

        if (this == y) {
            if (axis == x) {
                return z;
            }
            return x;
        }

        if (axis == y) {
            return x;
        }

        return y;
    }

    @Override
    public String toString() {
        return name();
    }

    public static Axis fromName(String name) {
        switch (name) {
            case "x":
                return x;
            case "y":
                return y;
            case "z":
                return z;
            default:
                return null;
        }
    }
}
