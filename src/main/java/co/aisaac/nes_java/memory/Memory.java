package co.aisaac.nes_java.memory;

// Interface Memory with Read and Write methods
public interface Memory {
    int /*byte*/ read(int address);

    void write(int address, int value /*byte*/);
}

