package co.aisaac.nes_java;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class Mapper3 extends Mapper {
    public Cartridge Cartridge;
    public int chrBank;
    public int prgBank1;
    public int prgBank2;

    // Constructor for Mapper3 taking in Cartridge and initializing banks.
    public Mapper3(Cartridge cartridge, int chrBank, int prgBank1, int prgBank2) {
        this.Cartridge = cartridge;
        this.chrBank = chrBank;
        this.prgBank1 = prgBank1;
        this.prgBank2 = prgBank2;
    }

    // NewMapper3 corresponds to the Golang NewMapper3 function.
    public static Mapper NewMapper3(Cartridge cartridge) {
        int prgBanks = cartridge.PRG.length / 0x4000;
        return new Mapper3(cartridge, 0, 0, prgBanks - 1);
    }

    // Save method writes the state of the mapper to the encoder.
    public void Save(ObjectOutputStream encoder) throws IOException {
        encoder.writeInt(chrBank);
        encoder.writeInt(prgBank1);
        encoder.writeInt(prgBank2);
    }

    // Load method reads the state of the mapper from the decoder.
    public void Load(ObjectInputStream decoder) throws IOException {
        chrBank = decoder.readInt();
        prgBank1 = decoder.readInt();
        prgBank2 = decoder.readInt();
    }

    // Step method with no operation as defined in the original code.
    public void Step() {
    }

    // Read method returns a byte from the appropriate memory region
    // based on the given address.
    public byte Read(int address) {
        if (address < 0x2000) {
            int index = this.chrBank * 0x2000 + address;
            return this.Cartridge.CHR[index];
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
            throw new RuntimeException(String.format("unhandled mapper3 read at address: 0x%04X", address));
        }
    }

    // Write method writes a byte value to the appropriate memory region
    // based on the given address.
    public void Write(int address, byte value) {
        if (address < 0x2000) {
            int index = this.chrBank * 0x2000 + address;
            this.Cartridge.CHR[index] = value;
        } else if (address >= 0x8000) {
            this.chrBank = value & 3;
        } else if (address >= 0x6000) {
            int index = address - 0x6000;
            this.Cartridge.SRAM[index] = value;
        } else {
            throw new RuntimeException(String.format("unhandled mapper3 write at address: 0x%04X", address));
        }
    }
}
