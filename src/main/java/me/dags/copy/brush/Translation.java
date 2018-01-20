package me.dags.copy.brush;

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
public enum Translation implements Serializable<Translation> {
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
    public TypeToken<Translation> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<Translation> getSerializer() {
        return SERIALIZER;
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

    private static final TypeToken<Translation> TOKEN = TypeToken.of(Translation.class);
    private static final TypeSerializer<Translation> SERIALIZER = new Serializer();

    private static class Serializer implements TypeSerializer<Translation> {
        @Override
        public Translation deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            return from(value.getString());
        }

        @Override
        public void serialize(TypeToken<?> type, Translation obj, ConfigurationNode value) throws ObjectMappingException {
            value.setValue(obj.toString());
        }
    }
}
