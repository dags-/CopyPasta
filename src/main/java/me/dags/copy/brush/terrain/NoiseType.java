package me.dags.copy.brush.terrain;

import com.flowpowered.noise.module.Module;
import com.flowpowered.noise.module.source.Billow;
import com.flowpowered.noise.module.source.Perlin;
import com.flowpowered.noise.module.source.RidgedMulti;
import com.google.common.reflect.TypeToken;
import me.dags.copy.util.Serializable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

/**
 * @author dags <dags@dags.me>
 */
public enum NoiseType implements Serializable<NoiseType> {
    RIDGE("ridge"),
    PERLIN("perlin"),
    BILLOW("billow"),
    NONE("none"),;

    private static final TypeToken<NoiseType> TOKEN = TypeToken.of(NoiseType.class);
    private static final TypeSerializer<NoiseType> SERIALIZER = new Serializer();
    private final String name;

    NoiseType(String name) {
        this.name = name;
    }

    @Override
    public TypeToken<NoiseType> getToken() {
        return TOKEN;
    }

    @Override
    public TypeSerializer<NoiseType> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String toString() {
        return name;
    }

    public static NoiseType from(String name) {
        switch (name.toLowerCase()) {
            case "ridge":
                return RIDGE;
            case "perlin":
                return PERLIN;
            case "billow":
                return BILLOW;
            default:
                return NONE;
        }
    }

    public Module getModule(int seed, int scale, int octaves) {
        if (this == RIDGE) {
            RidgedMulti module = new RidgedMulti();
            module.setSeed(seed);
            module.setFrequency(1D / scale);
            module.setOctaveCount(octaves);
            return module;
        }
        if (this == PERLIN) {
            Perlin module = new Perlin();
            module.setSeed(seed);
            module.setFrequency(1D / scale);
            module.setOctaveCount(octaves);
            return module;
        }
        Billow module = new Billow();
        module.setSeed(seed);
        module.setFrequency(1D / scale);
        module.setOctaveCount(octaves);
        return module;
    }

    private static class Serializer implements TypeSerializer<NoiseType> {
        @Override
        public NoiseType deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            return from(value.getString());
        }

        @Override
        public void serialize(TypeToken<?> type, NoiseType obj, ConfigurationNode value) throws ObjectMappingException {
            value.setValue(obj.toString());
        }
    }
}
