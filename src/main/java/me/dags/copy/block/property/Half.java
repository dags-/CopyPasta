package me.dags.copy.block.property;

/**
 * @author dags <dags@dags.me>
 */
public enum Half implements Property<Half> {

    top,
    bottom,;

    private final String property = "half=" + this.toString();

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public Half rotate(Axis axis, int angle) {
        return this;
    }

    @Override
    public Half flip(Axis axis) {
        return axis == Axis.y ? (this == top ? bottom : top) : this;
    }
}
