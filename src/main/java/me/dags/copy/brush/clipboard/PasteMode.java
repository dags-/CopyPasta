package me.dags.copy.brush.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import me.dags.copy.operation.modifier.Translate;
import me.dags.copy.util.Serializable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

/**
 * @author dags <dags@dags.me>
 */
public enum PasteMode implements Serializable<PasteMode> {
    NORMAL("normal"),
    SURFACE("surface"),
    OVERLAY("overlay"),
    ;

    private final String name;

    PasteMode(String name) {
        this.name = name;
    }

    public Translate getModifier(Vector3i position, Vector3i offset) {
        if (this == SURFACE) {
            return Translate.foundation(position, offset);
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
    public TypeToken<PasteMode> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<PasteMode> getSerializer() {
        return SERIALIZER;
    }

    public static PasteMode from(String name) {
        switch (name.toLowerCase()) {
            case "surface":
                return SURFACE;
            case "overlay":
                return OVERLAY;
            default:
                return NORMAL;
        }
    }

    private static final TypeToken<PasteMode> TOKEN = TypeToken.of(PasteMode.class);
    private static final TypeSerializer<PasteMode> SERIALIZER = new Serializer();

    private static class Serializer implements TypeSerializer<PasteMode> {
        @Override
        public PasteMode deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            return from(value.getString());
        }

        @Override
        public void serialize(TypeToken<?> type, PasteMode obj, ConfigurationNode value) throws ObjectMappingException {
            value.setValue(obj.toString());
        }
    }
}
