package co.aisaac.nes_java.memory;

import co.aisaac.nes_java.Console;

// PPU Memory Map
public class PPUMemory implements Memory {
    public Console console;

    public PPUMemory(Console console) {
        this.console = console;
    }

    @Override
    public int /*byte*/ read(int address) {
        address = address % 0x4000;
        if (address < 0x2000) {
            return this.console.mapper.read(address);
        } else if (address < 0x3F00) {
            int /*byte*/ mode = this.console.cartridge.mirror;
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
    public void write(int address, int /*byte*/ value) {
        address = address % 0x4000;
        if (address < 0x2000) {
            this.console.mapper.write(address, value);
        } else if (address < 0x3F00) {
            int /*byte*/ mode = this.console.cartridge.mirror;
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
    public static final int MirrorVertical = 1;
    public static final int MirrorSingle0 = 2;
    public static final int MirrorSingle1 = 3;
    public static final int MirrorFour = 4;

    // MirrorLookup table
    public static final int[][] MirrorLookup = {
            {0, 0, 1, 1},
            {0, 1, 0, 1},
            {0, 0, 0, 0},
            {1, 1, 1, 1},
            {0, 1, 2, 3}
    };

    // MirrorAddress function
    public static int MirrorAddress(int /*byte*/ mode, int address) {
        address = (address - 0x2000) % 0x1000;
        int table = address / 0x0400;
        int offset = address % 0x0400;
        return 0x2000 + MirrorLookup[mode][table] * 0x0400 + offset;
    }
}
