package co.aisaac.nes_java;

import java.lang.Math;

public interface Filter {
    float Step(float x);
}

// First order filters are defined by the following parameters.
// y[n] = B0*x[n] + B1*x[n-1] - A1*y[n-1]
class FirstOrderFilter implements Filter {
    float B0;
    float B1;
    float A1;
    float prevX;
    float prevY;

    FirstOrderFilter(float B0, float B1, float A1) {
        this.B0 = B0;
        this.B1 = B1;
        this.A1 = A1;
        this.prevX = 0;
        this.prevY = 0;
    }

    public float Step(float x) {
        float y = this.B0 * x + this.B1 * this.prevX - this.A1 * this.prevY;
        this.prevY = y;
        this.prevX = x;
        return y;
    }
}

// Utility class for creating filters.
class FilterFactory {
    // sampleRate: samples per second
    // cutoffFreq: oscillations per second
    public static Filter LowPassFilter(float sampleRate, float cutoffFreq) {
        float c = sampleRate / ((float)Math.PI) / cutoffFreq;
        float a0i = 1 / (1 + c);
        return new FirstOrderFilter(a0i, a0i, (1 - c) * a0i);
    }

    public static Filter HighPassFilter(float sampleRate, float cutoffFreq) {
        float c = sampleRate / ((float)Math.PI) / cutoffFreq;
        float a0i = 1 / (1 + c);
        return new FirstOrderFilter(c * a0i, -c * a0i, (1 - c) * a0i);
    }
}

// FilterChain is a chain of filters.
class FilterChain implements Filter {
    Filter[] filters;

    FilterChain(Filter[] filters) {
        this.filters = filters;
    }

    public float Step(float x) {
        if (this.filters != null) {
            for (int i = 0; i < this.filters.length; i++) {
                x = this.filters[i].Step(x);
            }
        }
        return x;
    }
}
