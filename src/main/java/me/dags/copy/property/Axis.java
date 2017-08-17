package me.dags.copy.property;

/**
 * @author dags <dags@dags.me>
 */
public enum Axis implements Property<Axis> {
    x,
    y,
    z,;

    private final String property = "axis=" + this.toString();

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public Axis rotate(Axis axis, int angle) {
        if (axis != this) {
            int rotations = angle / 90;
            int mod = rotations % 2;
            if (mod != 0) {
                switch (axis) {
                    case x:
                        return rotate(y, z);
                    case y:
                        return rotate(x, z);
                    case z:
                        return rotate(x, y);
                    default:
                        break;
                }
            }
        }
        return this;
    }

    @Override
    public Axis flip(Axis axis) {
        return this;
    }

    private Axis rotate(Axis one, Axis two) {
        return this == one ? two : one;
    }
}
