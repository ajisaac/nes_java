package co.aisaac.nes_java;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Mapper2 class translation from Golang to Java
public class Mapper2 extends Mapper {
    // Corresponds to *Cartridge in Go; preserved as a public field
    public Cartridge Cartridge;
    public int prgBanks;
    public int prgBank1;
    public int prgBank2;

    // Constructor corresponding to NewMapper2 in Go
    public Mapper2(Cartridge cartridge, int prgBanks, int prgBank1, int prgBank2) {
        this.Cartridge = cartridge;
        this.prgBanks = prgBanks;
        this.prgBank1 = prgBank1;
        this.prgBank2 = prgBank2;
    }

    // Factory method equivalent to NewMapper2 in Go
    public static Mapper NewMapper2(Cartridge cartridge) {
        int prgBanks = cartridge.PRG.length / 0x4000;
        int prgBank1 = 0;
        int prgBank2 = prgBanks - 1;
        return new Mapper2(cartridge, prgBanks, prgBank1, prgBank2);
    }

    @Override
    public void Save(ObjectOutputStream encoder) throws IOException {
        // todo
//        encoder.encode(this.prgBanks);
//        encoder.encode(this.prgBank1);
//        encoder.encode(this.prgBank2);
    }

    @Override
    public void Load(ObjectInputStream decoder) throws IOException {
        // todo
//        this.prgBanks = decoder.decodeInt();
//        this.prgBank1 = decoder.decodeInt();
//        this.prgBank2 = decoder.decodeInt();
    }

    @Override
    public void Step() {
    }

    @Override
    public byte Read(int address) {
        if (address < 0x2000) {
            return this.Cartridge.CHR[address];
        } else if (address >= 0xC000) {
            int index = this.prgBank2 * 0x4000 + (address - 0xC000);
            return this.Cartridge.PRG[index];
        } else if (address >= 0x8000) {
            int index = this.prgBank1 * 0x4000 + (address - 0x8000);
            return this.Cartridge.PRG[index];
        } else if (address >= 0x6000) {
            int index = address - 0x6000;
            return this.Cartridge.SRAM[index];
        } else {
            System.err.printf("unhandled mapper2 read at address: 0x%04X%n", address);
            throw new RuntimeException(String.format("unhandled mapper2 read at address: 0x%04X", address));
        }
    }

    @Override
    public void Write(int address, byte value) {
        if (address < 0x2000) {
            this.Cartridge.CHR[address] = value;
        } else if (address >= 0x8000) {
            this.prgBank1 = (int) value % this.prgBanks;
        } else if (address >= 0x6000) {
            int index = address - 0x6000;
            this.Cartridge.SRAM[index] = value;
        } else {
            System.err.printf("unhandled mapper2 write at address: 0x%04X%n", address);
            throw new RuntimeException(String.format("unhandled mapper2 write at address: 0x%04X", address));
        }
    }
}
