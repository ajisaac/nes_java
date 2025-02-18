package co.aisaac.nes_java;

import java.io.*;

// Mapper interface definition
public interface Mapper {
    void Save(ObjectOutputStream encoder) throws IOException;
    void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException;
    void Step();
    byte Read(int address);
    void Write(int address, byte value);
}

// Mapper4 class translated from Golang
public class Mapper4 implements Mapper {
    public Cartridge Cartridge;
    public Console console;
    public byte register;
    public byte[] registers = new byte[8];
    public byte prgMode;
    public byte chrMode;
    public int[] prgOffsets = new int[4];
    public int[] chrOffsets = new int[8];
    public byte reload;
    public byte counter;
    public boolean irqEnable;

    // Constructor equivalent to NewMapper4 in Golang
    public Mapper4(Console console, Cartridge cartridge) {
        this.Cartridge = cartridge;
        this.console = console;
        this.prgOffsets[0] = prgBankOffset(0);
        this.prgOffsets[1] = prgBankOffset(1);
        this.prgOffsets[2] = prgBankOffset(-2);
        this.prgOffsets[3] = prgBankOffset(-1);
    }

    public static Mapper NewMapper4(Console console, Cartridge cartridge) {
        return new Mapper4(console, cartridge);
    }

    public void Save(ObjectOutputStream encoder) throws IOException {
        encoder.writeByte(register);
        encoder.write(registers);
        encoder.writeByte(prgMode);
        encoder.writeByte(chrMode);
        for (int offset : prgOffsets) {
            encoder.writeInt(offset);
        }
        for (int offset : chrOffsets) {
            encoder.writeInt(offset);
        }
        encoder.writeByte(reload);
        encoder.writeByte(counter);
        encoder.writeBoolean(irqEnable);
    }

    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        register = decoder.readByte();
        decoder.readFully(registers);
        prgMode = decoder.readByte();
        chrMode = decoder.readByte();
        for (int i = 0; i < prgOffsets.length; i++) {
            prgOffsets[i] = decoder.readInt();
        }
        for (int i = 0; i < chrOffsets.length; i++) {
            chrOffsets[i] = decoder.readInt();
        }
        reload = decoder.readByte();
        counter = decoder.readByte();
        irqEnable = decoder.readBoolean();
    }

    public void Step() {
        PPU ppu = console.PPU;
        if (ppu.Cycle != 280) { // TODO: this *should* be 260
            return;
        }
        if (ppu.ScanLine > 239 && ppu.ScanLine < 261) {
            return;
        }
        if (ppu.flagShowBackground == 0 && ppu.flagShowSprites == 0) {
            return;
        }
        HandleScanLine();
    }

    public void HandleScanLine() {
        if (counter == 0) {
            counter = reload;
        } else {
            counter--;
            if (counter == 0 && irqEnable) {
                console.CPU.triggerIRQ();
            }
        }
    }

    public byte Read(int address) {
        if (address < 0x2000) {
            int bank = address / 0x0400;
            int offset = address % 0x0400;
            return Cartridge.CHR[chrOffsets[bank] + offset];
        } else if (address >= 0x8000) {
            address = address - 0x8000;
            int bank = address / 0x2000;
            int offset = address % 0x2000;
            return Cartridge.PRG[prgOffsets[bank] + offset];
        } else if (address >= 0x6000) {
            return Cartridge.SRAM[address - 0x6000];
        } else {
            throw new RuntimeException(String.format("unhandled mapper4 read at address: 0x%04X", address));
        }
    }

    public void Write(int address, byte value) {
        if (address < 0x2000) {
            int bank = address / 0x0400;
            int offset = address % 0x0400;
            Cartridge.CHR[chrOffsets[bank] + offset] = value;
        } else if (address >= 0x8000) {
            writeRegister(address, value);
        } else if (address >= 0x6000) {
            Cartridge.SRAM[address - 0x6000] = value;
        } else {
            throw new RuntimeException(String.format("unhandled mapper4 write at address: 0x%04X", address));
        }
    }

    public void writeRegister(int address, byte value) {
        if (address <= 0x9FFF && address % 2 == 0) {
            writeBankSelect(value);
        } else if (address <= 0x9FFF && address % 2 == 1) {
            writeBankData(value);
        } else if (address <= 0xBFFF && address % 2 == 0) {
            writeMirror(value);
        } else if (address <= 0xBFFF && address % 2 == 1) {
            writeProtect(value);
        } else if (address <= 0xDFFF && address % 2 == 0) {
            writeIRQLatch(value);
        } else if (address <= 0xDFFF && address % 2 == 1) {
            writeIRQReload(value);
        } else if (address <= 0xFFFF && address % 2 == 0) {
            writeIRQDisable(value);
        } else if (address <= 0xFFFF && address % 2 == 1) {
            writeIRQEnable(value);
        }
    }

    public void writeBankSelect(byte value) {
        prgMode = (byte)((value >> 6) & 1);
        chrMode = (byte)((value >> 7) & 1);
        register = (byte)(value & 7);
        updateOffsets();
    }

    public void writeBankData(byte value) {
        registers[register] = value;
        updateOffsets();
    }

    public void writeMirror(byte value) {
        switch (value & 1) {
            case 0:
                Cartridge.Mirror = Cartridge.MirrorVertical;
                break;
            case 1:
                Cartridge.Mirror = Cartridge.MirrorHorizontal;
                break;
        }
    }

    public void writeProtect(byte value) {
    }

    public void writeIRQLatch(byte value) {
        reload = value;
    }

    public void writeIRQReload(byte value) {
        counter = 0;
    }

    public void writeIRQDisable(byte value) {
        irqEnable = false;
    }

    public void writeIRQEnable(byte value) {
        irqEnable = true;
    }

    public int prgBankOffset(int index) {
        if (index >= 0x80) {
            index -= 0x100;
        }
        int banks = Cartridge.PRG.length / 0x2000;
        index = index % banks;
        if (index < 0) {
            index += banks;
        }
        int offset = index * 0x2000;
        if (offset < 0) {
            offset += Cartridge.PRG.length;
        }
        return offset;
    }

    public int chrBankOffset(int index) {
        if (index >= 0x80) {
            index -= 0x100;
        }
        int banks = Cartridge.CHR.length / 0x0400;
        index = index % banks;
        if (index < 0) {
            index += banks;
        }
        int offset = index * 0x0400;
        if (offset < 0) {
            offset += Cartridge.CHR.length;
        }
        return offset;
    }

    public void updateOffsets() {
        switch (prgMode) {
            case 0:
                prgOffsets[0] = prgBankOffset(registers[6]);
                prgOffsets[1] = prgBankOffset(registers[7]);
                prgOffsets[2] = prgBankOffset(-2);
                prgOffsets[3] = prgBankOffset(-1);
                break;
            case 1:
                prgOffsets[0] = prgBankOffset(-2);
                prgOffsets[1] = prgBankOffset(registers[7]);
                prgOffsets[2] = prgBankOffset(registers[6]);
                prgOffsets[3] = prgBankOffset(-1);
                break;
        }
        switch (chrMode) {
            case 0:
                chrOffsets[0] = chrBankOffset(registers[0] & 0xFE);
                chrOffsets[1] = chrBankOffset(registers[0] | 0x01);
                chrOffsets[2] = chrBankOffset(registers[1] & 0xFE);
                chrOffsets[3] = chrBankOffset(registers[1] | 0x01);
                chrOffsets[4] = chrBankOffset(registers[2]);
                chrOffsets[5] = chrBankOffset(registers[3]);
                chrOffsets[6] = chrBankOffset(registers[4]);
                chrOffsets[7] = chrBankOffset(registers[5]);
                break;
            case 1:
                chrOffsets[0] = chrBankOffset(registers[2]);
                chrOffsets[1] = chrBankOffset(registers[3]);
                chrOffsets[2] = chrBankOffset(registers[4]);
                chrOffsets[3] = chrBankOffset(registers[5]);
                chrOffsets[4] = chrBankOffset(registers[0] & 0xFE);
                chrOffsets[5] = chrBankOffset(registers[0] | 0x01);
                chrOffsets[6] = chrBankOffset(registers[1] & 0xFE);
                chrOffsets[7] = chrBankOffset(registers[1] | 0x01);
                break;
        }
    }
}

// Supporting classes and definitions

class Cartridge {
    public byte[] PRG;
    public byte[] CHR;
    public byte[] SRAM;
    public int Mirror;

    public static final int MirrorVertical = 0;
    public static final int MirrorHorizontal = 1;
}

class Console {
    public PPU PPU;
    public CPU CPU;
}

class PPU {
    public int Cycle;
    public int ScanLine;
    public byte flagShowBackground;
    public byte flagShowSprites;
}

class CPU {
    public void triggerIRQ() {
    }
}
