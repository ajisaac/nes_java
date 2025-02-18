package co.aisaac.nes_java.filter;

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
