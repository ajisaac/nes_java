package nes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

// https://github.com/asfdfdfd/fceux/blob/master/src/boards/225.cpp
// https://wiki.nesdev.com/w/index.php/INES_Mapper_225

// Definition for Mirror constants
class Mirror {
    public static final int MirrorHorizontal = 0;
    public static final int MirrorVertical = 1;
}

// Cartridge class with necessary fields
class Cartridge {
    public byte[] PRG;
    public byte[] CHR;
    public byte[] SRAM;
    public int Mirror; // Represents mirroring mode

    public Cartridge(byte[] PRG, byte[] CHR, byte[] SRAM) {
        this.PRG = PRG;
        this.CHR = CHR;
        this.SRAM = SRAM;
    }
}

// Mapper interface defining required methods
interface Mapper {
    void Save(ObjectOutputStream encoder) throws IOException;
    void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException;
    void Step();
    byte Read(int address);
    void Write(int address, byte value);
}

// Mapper225 class exactly translated from Golang
public class Mapper225 implements Mapper {
    // Embedded Cartridge
    public Cartridge Cartridge;
    public int chrBank;
    public int prgBank1;
    public int prgBank2;

    // Constructor
    public Mapper225(Cartridge cartridge, int chrBank, int prgBank1, int prgBank2) {
        this.Cartridge = cartridge;
        this.chrBank = chrBank;
        this.prgBank1 = prgBank1;
        this.prgBank2 = prgBank2;
    }

    // NewMapper225 factory method equivalent to the Golang NewMapper225 function
    public static Mapper NewMapper225(Cartridge cartridge) {
        int prgBanks = cartridge.PRG.length / 0x4000;
        return new Mapper225(cartridge, 0, 0, prgBanks - 1);
    }

    // Save method equivalent to the Golang Save function using ObjectOutputStream
    public void Save(ObjectOutputStream encoder) throws IOException {
        encoder.writeInt(this.chrBank);
        encoder.writeInt(this.prgBank1);
        encoder.writeInt(this.prgBank2);
    }

    // Load method equivalent to the Golang Load function using ObjectInputStream
    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        this.chrBank = decoder.readInt();
        this.prgBank1 = decoder.readInt();
        this.prgBank2 = decoder.readInt();
    }

    // Step method equivalent to the Golang Step function (empty implementation)
    public void Step() {
    }

    // Read method equivalent to the Golang Read function
    public byte Read(int address) {
        // Using Logger for error logging
        Logger logger = Logger.getLogger(Mapper225.class.getName());
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
            logger.log(Level.SEVERE, String.format("unhandled Mapper225 read at address: 0x%04X", address));
            throw new RuntimeException(String.format("unhandled Mapper225 read at address: 0x%04X", address));
        }
    }

    // Write method equivalent to the Golang Write function
    public void Write(int address, byte value) {
        if (address < 0x8000) {
            return;
        }

        int A = address;
        int bank = (A >> 14) & 1;
        this.chrBank = (A & 0x3f) | (bank << 6);
        int prg = ((A >> 6) & 0x3f) | (bank << 6);
        int mode = (A >> 12) & 1;
        if (mode == 1) {
            this.prgBank1 = prg;
            this.prgBank2 = prg;
        } else {
            this.prgBank1 = prg;
            this.prgBank2 = prg + 1;
        }
        int mirr = (A >> 13) & 1;
        if (mirr == 1) {
            this.Cartridge.Mirror = Mirror.MirrorHorizontal;
        } else {
            this.Cartridge.Mirror = Mirror.MirrorVertical;
        }

        // fmt.Println(address, mirr, mode, prg)
    }
}
