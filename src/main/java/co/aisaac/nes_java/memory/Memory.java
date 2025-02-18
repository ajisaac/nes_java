package co.aisaac.nes_java.memory;

// Interface Memory with Read and Write methods
public interface Memory {
    byte Read(int address);
    void Write(int address, byte value);
}

