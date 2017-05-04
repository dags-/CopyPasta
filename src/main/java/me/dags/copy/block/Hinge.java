package me.dags.copy.block;

/**
 * @author dags <dags@dags.me>
 */
public enum Hinge implements TraitValue, Flippable<Hinge> {
    left,
    right,
    ;

    static {
        left.opposite = right;
        right.opposite = left;
    }

    private Hinge opposite;

    public Hinge getOpposite() {
        return opposite;
    }

    @Override
    public Hinge flip(Axis axis) {
        if (axis != Axis.y) {
            return getOpposite();
        }
        return null;
    }

    public static Hinge fromName(String name) {
        return name.equals("left") ? left : name.equals("right") ? right : null;
    }
}
