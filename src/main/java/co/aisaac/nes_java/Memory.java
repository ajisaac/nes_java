package co.aisaac.nes_java;

import java.util.*;

// Interface Memory with Read and Write methods
public interface Memory {
    byte Read(int address);
    void Write(int address, byte value);
}

// CPU Memory Map
public class cpuMemory implements Memory {
    public Console console;

    public cpuMemory(Console console) {
        this.console = console;
    }

    @Override
    public byte Read(int address) {
        if (address < 0x2000) {
            return this.console.RAM[address % 0x0800];
        } else if (address < 0x4000) {
            return this.console.PPU.readRegister(0x2000 + address % 8);
        } else if (address == 0x4014) {
            return this.console.PPU.readRegister(address);
        } else if (address == 0x4015) {
            return this.console.APU.readRegister(address);
        } else if (address == 0x4016) {
            return this.console.Controller1.Read();
        } else if (address == 0x4017) {
            return this.console.Controller2.Read();
        } else if (address < 0x6000) {
            // TODO: I/O registers
        } else if (address >= 0x6000) {
            return this.console.Mapper.Read(address);
        } else {
            System.err.printf("unhandled cpu memory read at address: 0x%04X%n", address);
            System.exit(1);
        }
        return 0;
    }

    @Override
    public void Write(int address, byte value) {
        if (address < 0x2000) {
            this.console.RAM[address % 0x0800] = value;
        } else if (address < 0x4000) {
            this.console.PPU.writeRegister(0x2000 + address % 8, value);
        } else if (address < 0x4014) {
            this.console.APU.writeRegister(address, value);
        } else if (address == 0x4014) {
            this.console.PPU.writeRegister(address, value);
        } else if (address == 0x4015) {
            this.console.APU.writeRegister(address, value);
        } else if (address == 0x4016) {
            this.console.Controller1.Write(value);
            this.console.Controller2.Write(value);
        } else if (address == 0x4017) {
            this.console.APU.writeRegister(address, value);
        } else if (address < 0x6000) {
            // TODO: I/O registers
        } else if (address >= 0x6000) {
            this.console.Mapper.Write(address, value);
        } else {
            System.err.printf("unhandled cpu memory write at address: 0x%04X%n", address);
            System.exit(1);
        }
    }
}

// PPU Memory Map
public class ppuMemory implements Memory {
    public Console console;

    public ppuMemory(Console console) {
        this.console = console;
    }

    @Override
    public byte Read(int address) {
        address = address % 0x4000;
        if (address < 0x2000) {
            return this.console.Mapper.Read(address);
        } else if (address < 0x3F00) {
            byte mode = this.console.Cartridge.Mirror;
            int mirrorAddr = MirrorAddress(mode, address) % 2048;
            return this.console.PPU.nameTableData[mirrorAddr];
        } else if (address < 0x4000) {
            return this.console.PPU.readPalette(address % 32);
        } else {
            System.err.printf("unhandled ppu memory read at address: 0x%04X%n", address);
            System.exit(1);
        }
        return 0;
    }

    @Override
    public void Write(int address, byte value) {
        address = address % 0x4000;
        if (address < 0x2000) {
            this.console.Mapper.Write(address, value);
        } else if (address < 0x3F00) {
            byte mode = this.console.Cartridge.Mirror;
            int mirrorAddr = MirrorAddress(mode, address) % 2048;
            this.console.PPU.nameTableData[mirrorAddr] = value;
        } else if (address < 0x4000) {
            this.console.PPU.writePalette(address % 32, value);
        } else {
            System.err.printf("unhandled ppu memory write at address: 0x%04X%n", address);
            System.exit(1);
        }
    }

    // Mirroring Modes Constants
    public static final int MirrorHorizontal = 0;
    public static final int MirrorVertical   = 1;
    public static final int MirrorSingle0    = 2;
    public static final int MirrorSingle1    = 3;
    public static final int MirrorFour       = 4;

    // MirrorLookup table
    public static final int[][] MirrorLookup = {
            {0, 0, 1, 1},
            {0, 1, 0, 1},
            {0, 0, 0, 0},
            {1, 1, 1, 1},
            {0, 1, 2, 3}
    };

    // MirrorAddress function
    public static int MirrorAddress(byte mode, int address) {
        address = (address - 0x2000) % 0x1000;
        int table = address / 0x0400;
        int offset = address % 0x0400;
        return 0x2000 + MirrorLookup[mode][table] * 0x0400 + offset;
    }
}

// Console and related classes
public class Console {
    public byte[] RAM;
    public PPU PPU;
    public APU APU;
    public Controller Controller1;
    public Controller Controller2;
    public Mapper Mapper;
    public Cartridge Cartridge;

    public Console() {
        // Initialize RAM with size 0x0800 (2KB)
        this.RAM = new byte[0x0800];
        this.PPU = new PPU();
        this.APU = new APU();
        this.Controller1 = new Controller();
        this.Controller2 = new Controller();
        this.Mapper = new Mapper();
        this.Cartridge = new Cartridge();
    }
}

class PPU {
    public byte[] nameTableData;

    public PPU() {
        // Allocate 2048 bytes for nameTableData as used in indexing with %2048.
        this.nameTableData = new byte[2048];
    }

    public byte readRegister(int address) {
        // Complete implementation returning dummy value
        return 0;
    }

    public void writeRegister(int address, byte value) {
        // Complete implementation (no operation)
    }

    public byte readPalette(int address) {
        // Complete implementation returning dummy value
        return 0;
    }

    public void writePalette(int address, byte value) {
        // Complete implementation (no operation)
    }
}

class APU {
    public byte readRegister(int address) {
        // Complete implementation returning dummy value
        return 0;
    }

    public void writeRegister(int address, byte value) {
        // Complete implementation (no operation)
    }
}

class Controller {
    public byte Read() {
        // Complete implementation returning dummy value
        return 0;
    }

    public void Write(byte value) {
        // Complete implementation (no operation)
    }
}

class Mapper {
    public byte Read(int address) {
        // Complete implementation returning dummy value
        return 0;
    }

    public void Write(int address, byte value) {
        // Complete implementation (no operation)
    }
}

class Cartridge {
    public byte Mirror;

    public Cartridge() {
        // Default mirror mode (for example, MirrorHorizontal)
        this.Mirror = (byte) ppuMemory.MirrorHorizontal;
    }
}

// Factory methods for creating memory objects
class MemoryFactory {
    public static Memory NewCPUMemory(Console console) {
        return new cpuMemory(console);
    }

    public static Memory NewPPUMemory(Console console) {
        return new ppuMemory(console);
    }
}
