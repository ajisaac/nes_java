package co.aisaac.nes_java.mappers;

import co.aisaac.nes_java.Cartridge;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import static co.aisaac.nes_java.memory.PPUMemory.MirrorHorizontal;
import static co.aisaac.nes_java.memory.PPUMemory.MirrorSingle0;
import static co.aisaac.nes_java.memory.PPUMemory.MirrorSingle1;
import static co.aisaac.nes_java.memory.PPUMemory.MirrorVertical;

//
// Mapper1 implementation
//
public class Mapper1 extends Mapper {
    // Embedded Cartridge reference
    public co.aisaac.nes_java.Cartridge Cartridge;
    public byte shiftRegister;
    public byte control;
    public byte prgMode;
    public byte chrMode;
    public byte prgBank;
    public byte chrBank0;
    public byte chrBank1;
    public int[] prgOffsets = new int[2];
    public int[] chrOffsets = new int[2];

    private static final Logger LOGGER = Logger.getLogger(Mapper1.class.getName());

    // NewMapper1 creates a new Mapper1 instance
    public static Mapper NewMapper1(Cartridge cartridge) {
        Mapper1 m = new Mapper1();
        m.Cartridge = cartridge;
        m.shiftRegister = 0x10;
        m.prgOffsets[1] = m.prgBankOffset(-1);
        return m;
    }

    // Save method - writes internal state using ObjectOutputStream
    public void save(ObjectOutputStream encoder) throws IOException {
        encoder.writeByte(shiftRegister);
        encoder.writeByte(control);
        encoder.writeByte(prgMode);
        encoder.writeByte(chrMode);
        encoder.writeByte(prgBank);
        encoder.writeByte(chrBank0);
        encoder.writeByte(chrBank1);
        for (int i = 0; i < prgOffsets.length; i++) {
            encoder.writeInt(prgOffsets[i]);
        }
        for (int i = 0; i < chrOffsets.length; i++) {
            encoder.writeInt(chrOffsets[i]);
        }
    }

    // Load method - reads internal state using ObjectInputStream
    public void load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        shiftRegister = decoder.readByte();
        control = decoder.readByte();
        prgMode = decoder.readByte();
        chrMode = decoder.readByte();
        prgBank = decoder.readByte();
        chrBank0 = decoder.readByte();
        chrBank1 = decoder.readByte();
        for (int i = 0; i < prgOffsets.length; i++) {
            prgOffsets[i] = decoder.readInt();
        }
        for (int i = 0; i < chrOffsets.length; i++) {
            chrOffsets[i] = decoder.readInt();
        }
    }

    // Step does nothing
    public void step() {
    }

    // Read method for Mapper1
    public byte read(int address) {
        if (address < 0x2000) {
            int bank = address / 0x1000;
            int offset = address % 0x1000;
            return Cartridge.CHR[chrOffsets[bank] + offset];
        } else if (address >= 0x8000) {
            address = address - 0x8000;
            int bank = address / 0x4000;
            int offset = address % 0x4000;
            return Cartridge.PRG[prgOffsets[bank] + offset];
        } else if (address >= 0x6000) {
            return Cartridge.SRAM[address - 0x6000];
        } else {
            LOGGER.severe(String.format("unhandled mapper1 read at address: 0x%04X", address));
            throw new RuntimeException(String.format("unhandled mapper1 read at address: 0x%04X", address));
        }
    }

    // Write method for Mapper1
    public void write(int address, byte value) {
        if (address < 0x2000) {
            int bank = address / 0x1000;
            int offset = address % 0x1000;
            Cartridge.CHR[chrOffsets[bank] + offset] = value;
        } else if (address >= 0x8000) {
            loadRegister(address, value);
        } else if (address >= 0x6000) {
            Cartridge.SRAM[address - 0x6000] = value;
        } else {
            LOGGER.severe(String.format("unhandled mapper1 write at address: 0x%04X", address));
            throw new RuntimeException(String.format("unhandled mapper1 write at address: 0x%04X", address));
        }
    }

    // loadRegister processes writes to the mapper register
    public void loadRegister(int address, byte value) {
        if ((value & 0x80) == 0x80) {
            shiftRegister = 0x10;
            writeControl((byte)(control | 0x0C));
        } else {
            boolean complete = ((shiftRegister & 1) == 1);
            shiftRegister = (byte)((shiftRegister & 0xFF) >> 1);
            shiftRegister |= (byte)((value & 1) << 4);
            if (complete) {
                writeRegister(address, shiftRegister);
                shiftRegister = 0x10;
            }
        }
    }

    // writeRegister dispatches the write based on the address range
    public void writeRegister(int address, byte value) {
        if (address <= 0x9FFF) {
            writeControl(value);
        } else if (address <= 0xBFFF) {
            writeCHRBank0(value);
        } else if (address <= 0xDFFF) {
            writeCHRBank1(value);
        } else if (address <= 0xFFFF) {
            writePRGBank(value);
        }
    }

    // writeControl (internal, $8000-$9FFF)
    public void writeControl(byte value) {
        control = value;
        chrMode = (byte)((value >> 4) & 1);
        prgMode = (byte)((value >> 2) & 3);
        byte mirror = (byte)(value & 3);
        switch (mirror) {
            case 0:
                Cartridge.Mirror = MirrorSingle0;
                break;
            case 1:
                Cartridge.Mirror = MirrorSingle1;
                break;
            case 2:
                Cartridge.Mirror = MirrorVertical;
                break;
            case 3:
                Cartridge.Mirror = MirrorHorizontal;
                break;
        }
        updateOffsets();
    }

    // CHR bank 0 (internal, $A000-$BFFF)
    public void writeCHRBank0(byte value) {
        chrBank0 = value;
        updateOffsets();
    }

    // CHR bank 1 (internal, $C000-$DFFF)
    public void writeCHRBank1(byte value) {
        chrBank1 = value;
        updateOffsets();
    }

    // PRG bank (internal, $E000-$FFFF)
    public void writePRGBank(byte value) {
        prgBank = (byte)(value & 0x0F);
        updateOffsets();
    }

    // prgBankOffset calculates the PRG bank offset
    public int prgBankOffset(int index) {
        if (index >= 0x80) {
            index -= 0x100;
        }
        int bankCount = Cartridge.PRG.length / 0x4000;
        index = index % bankCount;
        if (index < 0) {
            index += bankCount;
        }
        int offset = index * 0x4000;
        if (offset < 0) {
            offset += Cartridge.PRG.length;
        }
        return offset;
    }

    // chrBankOffset calculates the CHR bank offset
    public int chrBankOffset(int index) {
        if (index >= 0x80) {
            index -= 0x100;
        }
        int bankCount = Cartridge.CHR.length / 0x1000;
        index = index % bankCount;
        if (index < 0) {
            index += bankCount;
        }
        int offset = index * 0x1000;
        if (offset < 0) {
            offset += Cartridge.CHR.length;
        }
        return offset;
    }

    // PRG ROM bank mode (0, 1: switch 32 KB at $8000, ignoring low bit of bank number;
    //                    2: fix first bank at $8000 and switch 16 KB bank at $C000;
    //                    3: fix last bank at $C000 and switch 16 KB bank at $8000)
    // CHR ROM bank mode (0: switch 8 KB at a time; 1: switch two separate 4 KB banks)
    public void updateOffsets() {
        switch (prgMode) {
            case 0:
            case 1:
                prgOffsets[0] = prgBankOffset(prgBank & (byte)0xFE);
                prgOffsets[1] = prgBankOffset(prgBank | (byte)0x01);
                break;
            case 2:
                prgOffsets[0] = 0;
                prgOffsets[1] = prgBankOffset(prgBank);
                break;
            case 3:
                prgOffsets[0] = prgBankOffset(prgBank);
                prgOffsets[1] = prgBankOffset(-1);
                break;
        }
        switch (chrMode) {
            case 0:
                chrOffsets[0] = chrBankOffset(chrBank0 & (byte)0xFE);
                chrOffsets[1] = chrBankOffset(chrBank0 | (byte)0x01);
                break;
            case 1:
                chrOffsets[0] = chrBankOffset(chrBank0);
                chrOffsets[1] = chrBankOffset(chrBank1);
                break;
        }
    }


    @Override
    public void Save(ObjectOutputStream encoder) throws IOException {

    }

    @Override
    public void Load(ObjectInputStream decoder) throws IOException {

    }

    @Override
    public void Step() {

    }

    @Override
    public byte Read(int address) {
        return 0;
    }

    @Override
    public void Write(int address, byte value) {

    }
}
