package me.dags.copy.brush.cloud;

import com.flowpowered.noise.Noise;
import com.flowpowered.noise.NoiseQuality;
import com.flowpowered.noise.Utils;

/**
 * @author dags <dags@dags.me>
 */
public class ValueNoise {

    private static final double LACUNARITY = 2.0D;
    private static final double PERSISTENCE = 0.5D;
    private static final NoiseQuality NOISE_QUALITY = NoiseQuality.STANDARD;

    public static double getValue(double x, double y, double z, int seed, double frequency, int octaveCount) {
        double x1 = x * frequency;
        double y1 = y * frequency;
        double z1 = z * frequency;
        return getValue(x1, y1, z1, seed, octaveCount);
    }

    public static double getValue(double x, double y, double z, int seed, int octaveCount) {
        double value = 0.0D;
        double curPersistence = 1.0D;

        for(int curOctave = 0; curOctave < octaveCount; ++curOctave) {
            double nx = Utils.makeInt32Range(x);
            double ny = Utils.makeInt32Range(y);
            double nz = Utils.makeInt32Range(z);
            int currentSeed = seed + curOctave;
            double signal = Noise.valueCoherentNoise3D(nx, ny, nz, currentSeed, NOISE_QUALITY);
            value += signal * curPersistence;
            x *= LACUNARITY;
            y *= LACUNARITY;
            z *= LACUNARITY;
            curPersistence *= PERSISTENCE;
        }

        return value;
    }

    public static double maxValue(int octaveCount) {
        return (Math.pow(PERSISTENCE, (double) octaveCount) - 1.0D) / (PERSISTENCE - 1.0D);
    }
}
