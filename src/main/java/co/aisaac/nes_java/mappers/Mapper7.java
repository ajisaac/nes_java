package co.aisaac.nes_java.mappers;

import co.aisaac.nes_java.Cartridge;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import static co.aisaac.nes_java.memory.PPUMemory.MirrorSingle0;
import static co.aisaac.nes_java.memory.PPUMemory.MirrorSingle1;

// Mapper7 class as defined in mapper7.go
public class Mapper7 extends Mapper {
    public co.aisaac.nes_java.Cartridge Cartridge;
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
    public void Save(ObjectOutputStream encoder) throws IOException {
        encoder.writeInt(this.prgBank);
    }

    // Load method to decode and assign the prgBank value using ObjectInputStream
    public void Load(ObjectInputStream decoder) throws IOException {
        this.prgBank = decoder.readInt();
    }

    // Empty Step method as in the original Golang code
    public void Step() {
    }

    @Override
    public byte Read(int address) {
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

    public void Write(int address, byte value) {
        if (address < 0x2000) {
            this.Cartridge.CHR[address] = value;
        } else if (address >= 0x8000) {
            this.prgBank = value & 7;
            if ((value & 0x10) == 0x00) {
                this.Cartridge.mirror = MirrorSingle0;
            } else if ((value & 0x10) == 0x10) {
                this.Cartridge.mirror = MirrorSingle1;
            }
        } else if (address >= 0x6000) {
            int index = address - 0x6000;
            this.Cartridge.SRAM[index] = value;
        } else {
            throw new RuntimeException(String.format("unhandled mapper7 write at address: 0x%04X", address));
        }
    }

}
