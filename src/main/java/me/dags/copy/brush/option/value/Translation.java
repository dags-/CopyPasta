package me.dags.copy.brush.option.value;

import com.flowpowered.math.vector.Vector3i;
import me.dags.config.Node;
import me.dags.copy.operation.modifier.Translate;

/**
 * @author dags <dags@dags.me>
 */
public enum Translation implements Node.Value<Translation> {
    NONE("none"),
    SURFACE("surface"),
    OVERLAY("overlay"),
    ;

    private final String name;

    Translation(String name) {
        this.name = name;
    }

    public Translate getModifier(Vector3i position, Vector3i offset) {
        if (this == SURFACE) {
            return Translate.surface(position, offset);
        }
        if (this == OVERLAY) {
            return Translate.overlay(position, offset);
        }
        return Translate.NONE;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Translation fromNode(Node node) {
        return from(node.get(""));
    }

    @Override
    public void toNode(Node node) {
        node.set(this.name);
    }

    public static Translation from(String name) {
        switch (name.toLowerCase()) {
            case "surface":
                return SURFACE;
            case "overlay":
                return OVERLAY;
            default:
                return NONE;
        }
    }
}
