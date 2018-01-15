package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import me.dags.copy.operation.phase.Modifier;

/**
 * @author dags <dags@dags.me>
 */
public enum PasteMode {
    NORMAL,
    FOUNDATION,
    OVERLAY,
    ;

    public Modifier getModifier(Vector3i position, Vector3i offset) {
        if (this == FOUNDATION) {
            return Modifier.foundation(position, offset);
        }
        if (this == OVERLAY) {
            return Modifier.overlay(position, offset);
        }
        return Modifier.NONE;
    }
}
