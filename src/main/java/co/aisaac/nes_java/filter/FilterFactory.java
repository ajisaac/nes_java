package co.aisaac.nes_java.filter;

// Utility class for creating filters.
class FilterFactory {
    // sampleRate: samples per second
    // cutoffFreq: oscillations per second
    public static Filter LowPassFilter(float sampleRate, float cutoffFreq) {
        float c = sampleRate / ((float) Math.PI) / cutoffFreq;
        float a0i = 1 / (1 + c);
        return new FirstOrderFilter(a0i, a0i, (1 - c) * a0i);
    }

    public static Filter HighPassFilter(float sampleRate, float cutoffFreq) {
        float c = sampleRate / ((float) Math.PI) / cutoffFreq;
        float a0i = 1 / (1 + c);
        return new FirstOrderFilter(c * a0i, -c * a0i, (1 - c) * a0i);
    }
}
