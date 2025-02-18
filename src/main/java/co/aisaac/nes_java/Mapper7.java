package co.aisaac.nes_java;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

// Mirror enum representing the two mirroring modes
enum Mirror {
    Single0,
    Single1
}

// Cartridge class with necessary fields for Mapper7 functionality
class Cartridge {
    public Mirror Mirror;
    public byte[] CHR;
    public byte[] PRG;
    public byte[] SRAM;

    public Cartridge(byte[] CHR, byte[] PRG, byte[] SRAM) {
        this.CHR = CHR;
        this.PRG = PRG;
        this.SRAM = SRAM;
    }
}

// Mapper interface defining all required methods
interface Mapper {
    void save(ObjectOutputStream encoder) throws IOException;
    void load(ObjectInputStream decoder) throws IOException, ClassNotFoundException;
    void step();
    byte read(int address);
    void write(int address, byte value);
}

// Mapper7 class as defined in mapper7.go
public class Mapper7 implements Mapper {
    public Cartridge Cartridge;
    public int prgBank;

    // Constructor corresponding to NewMapper7 in Golang
    public Mapper7(Cartridge cartridge) {
        this.Cartridge = cartridge;
        this.prgBank = 0;
    }

    // Factory method to create a new Mapper7 instance
    public static Mapper NewMapper7(Cartridge cartridge) {
        return new Mapper7(cartridge);
    }

    // Save method to encode the prgBank value using ObjectOutputStream
    public void save(ObjectOutputStream encoder) throws IOException {
        encoder.writeInt(this.prgBank);
    }

    // Load method to decode and assign the prgBank value using ObjectInputStream
    public void load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        this.prgBank = decoder.readInt();
    }

    // Empty Step method as in the original Golang code
    public void step() {
    }

    // Read method corresponding to the Golang implementation
    public byte read(int address) {
        if (address < 0x2000) {
            return this.Cartridge.CHR[address];
        } else if (address >= 0x8000) {
            int index = this.prgBank * 0x8000 + (address - 0x8000);
            return this.Cartridge.PRG[index];
        } else if (address >= 0x6000) {
            int index = address - 0x6000;
            return this.Cartridge.SRAM[index];
        } else {
            throw new RuntimeException(String.format("unhandled mapper7 read at address: 0x%04X", address));
        }
    }

    // Write method corresponding to the Golang implementation
    public void write(int address, byte value) {
        if (address < 0x2000) {
            this.Cartridge.CHR[address] = value;
        } else if (address >= 0x8000) {
            this.prgBank = value & 7;
            if ((value & 0x10) == 0x00) {
                this.Cartridge.Mirror = Mirror.Single0;
            } else if ((value & 0x10) == 0x10) {
                this.Cartridge.Mirror = Mirror.Single1;
            }
        } else if (address >= 0x6000) {
            int index = address - 0x6000;
            this.Cartridge.SRAM[index] = value;
        } else {
            throw new RuntimeException(String.format("unhandled mapper7 write at address: 0x%04X", address));
        }
    }
}
