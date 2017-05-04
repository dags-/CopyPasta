package me.dags.copy.block;

/**
 * @author dags <dags@dags.me>
 */
public enum Half implements TraitValue, Flippable<Half> {
    top,
    bottom,
    upper,
    lower,
    ;

    static {
        top.opposite = bottom;
        bottom.opposite = top;
        upper.opposite = lower;
        lower.opposite = upper;
    }

    private Half opposite;

    public Half getOpposite() {
        return opposite;
    }

    @Override
    public Half flip(Axis axis) {
        if (axis == Axis.y) {
            return getOpposite();
        }
        return null;
    }

    public static Half fromName(String name) {
        switch (name) {
            case "top":
                return top;
            case "bottom":
                return bottom;
            case "upper":
                return upper;
            case "lower":
                return lower;
            default:
                return null;
        }
    }
}
