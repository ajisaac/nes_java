package co.aisaac.nes_java.filter;

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
