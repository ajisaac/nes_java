package co.aisaac.nes_java;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Memory interface equivalent to the Go memory interface
interface Memory {
    byte Read(int address);
    void Write(int address, byte value);
}

// PPUMemory class implements the Memory interface.
// It is constructed with a Console reference and provides 16 KB of memory.
class PPUMemory implements Memory {
    private byte[] mem = new byte[0x4000]; // 16K memory

    public PPUMemory(Console console) {
        // No additional implementation required; console is accepted per original signature.
    }

    public byte Read(int address) {
        return mem[address & 0x3FFF];
    }

    public void Write(int address, byte value) {
        mem[address & 0x3FFF] = value;
    }
}

// CPU class provides minimal functionality needed by the PPU.
// It includes a triggerNMI method, a stall counter, cycle counter and memory for reading.
class CPU {
    public int Cycles = 0;
    public int stall = 0;
    private byte[] ram = new byte[65536];

    public void triggerNMI() {
        // In a complete implementation, this would trigger a Non-Maskable Interrupt.
    }

    public byte Read(int address) {
        return ram[address & 0xFFFF];
    }

    public void Write(int address, byte value) {
        ram[address & 0xFFFF] = value;
    }
}

// Console class holds a reference to the CPU.
class Console {
    public CPU CPU;

    public Console() {
        CPU = new CPU();
    }
}

// The PPU class is a complete, line-by-line translation of the Go PPU implementation.
public class PPU {
    // Memory interface
    private Memory Memory;
    public Console console; // reference to parent object

    public int Cycle;    // 0-340
    public int ScanLine; // 0-261, 0-239=visible, 240=post, 241-260=vblank, 261=pre
    public long Frame;   // frame counter

    // storage variables
    public byte[] paletteData = new byte[32];
    public byte[] nameTableData = new byte[2048];
    public byte[] oamData = new byte[256];
    public BufferedImage front;
    public BufferedImage back;

    // PPU registers
    public int v; // current vram address (15 bit)
    public int t; // temporary vram address (15 bit)
    public byte x;   // fine x scroll (3 bit)
    public byte w;   // write toggle (1 bit)
    public byte f;   // even/odd frame flag (1 bit)

    public byte register;

    // NMI flags
    public boolean nmiOccurred;
    public boolean nmiOutput;
    public boolean nmiPrevious;
    public byte nmiDelay;

    // background temporary variables
    public byte nameTableByte;
    public byte attributeTableByte;
    public byte lowTileByte;
    public byte highTileByte;
    public long tileData;

    // sprite temporary variables
    public int spriteCount;
    public int[] spritePatterns = new int[8];
    public byte[] spritePositions = new byte[8];
    public byte[] spritePriorities = new byte[8];
    public byte[] spriteIndexes = new byte[8];

    // $2000 PPUCTRL
    public byte flagNameTable;       // 0: $2000; 1: $2400; 2: $2800; 3: $2C00
    public byte flagIncrement;       // 0: add 1; 1: add 32
    public byte flagSpriteTable;     // 0: $0000; 1: $1000; ignored in 8x16 mode
    public byte flagBackgroundTable; // 0: $0000; 1: $1000
    public byte flagSpriteSize;      // 0: 8x8; 1: 8x16
    public byte flagMasterSlave;     // 0: read EXT; 1: write EXT

    // $2001 PPUMASK
    public byte flagGrayscale;          // 0: color; 1: grayscale
    public byte flagShowLeftBackground; // 0: hide; 1: show
    public byte flagShowLeftSprites;    // 0: hide; 1: show
    public byte flagShowBackground;     // 0: hide; 1: show
    public byte flagShowSprites;        // 0: hide; 1: show
    public byte flagRedTint;            // 0: normal; 1: emphasized
    public byte flagGreenTint;          // 0: normal; 1: emphasized
    public byte flagBlueTint;           // 0: normal; 1: emphasized

    // $2002 PPUSTATUS
    public byte flagSpriteZeroHit;
    public byte flagSpriteOverflow;

    // $2003 OAMADDR
    public byte oamAddress;

    // $2007 PPUDATA
    public byte bufferedData; // for buffered reads

    // Palette is declared as a static array with 64 entries.
    public static Color[] Palette = new Color[64];
    static {
        for (int i = 0; i < 64; i++) {
            Palette[i] = new Color(0, 0, 0); // default to black
        }
    }

    // Constructor equivalent to NewPPU in Go.
    public PPU(Console console) {
        this.Memory = new PPUMemory(console);
        this.console = console;
        this.front = new BufferedImage(256, 240, BufferedImage.TYPE_INT_ARGB);
        this.back = new BufferedImage(256, 240, BufferedImage.TYPE_INT_ARGB);
        this.Reset();
    }

    // Save method writes each field to the ObjectOutputStream.
    public void Save(ObjectOutputStream encoder) throws IOException {
        encoder.writeInt(this.Cycle);
        encoder.writeInt(this.ScanLine);
        encoder.writeLong(this.Frame);
        encoder.write(this.paletteData);
        encoder.write(this.nameTableData);
        encoder.write(this.oamData);
        encoder.writeInt(this.v);
        encoder.writeInt(this.t);
        encoder.writeByte(this.x);
        encoder.writeByte(this.w);
        encoder.writeByte(this.f);
        encoder.writeByte(this.register);
        encoder.writeBoolean(this.nmiOccurred);
        encoder.writeBoolean(this.nmiOutput);
        encoder.writeBoolean(this.nmiPrevious);
        encoder.writeByte(this.nmiDelay);
        encoder.writeByte(this.nameTableByte);
        encoder.writeByte(this.attributeTableByte);
        encoder.writeByte(this.lowTileByte);
        encoder.writeByte(this.highTileByte);
        encoder.writeLong(this.tileData);
        encoder.writeInt(this.spriteCount);
        for (int i = 0; i < this.spritePatterns.length; i++) {
            encoder.writeInt(this.spritePatterns[i]);
        }
        encoder.write(this.spritePositions);
        encoder.write(this.spritePriorities);
        encoder.write(this.spriteIndexes);
        encoder.writeByte(this.flagNameTable);
        encoder.writeByte(this.flagIncrement);
        encoder.writeByte(this.flagSpriteTable);
        encoder.writeByte(this.flagBackgroundTable);
        encoder.writeByte(this.flagSpriteSize);
        encoder.writeByte(this.flagMasterSlave);
        encoder.writeByte(this.flagGrayscale);
        encoder.writeByte(this.flagShowLeftBackground);
        encoder.writeByte(this.flagShowLeftSprites);
        encoder.writeByte(this.flagShowBackground);
        encoder.writeByte(this.flagShowSprites);
        encoder.writeByte(this.flagRedTint);
        encoder.writeByte(this.flagGreenTint);
        encoder.writeByte(this.flagBlueTint);
        encoder.writeByte(this.flagSpriteZeroHit);
        encoder.writeByte(this.flagSpriteOverflow);
        encoder.writeByte(this.oamAddress);
        encoder.writeByte(this.bufferedData);
    }

    // Load method reads each field from the ObjectInputStream.
    public void Load(ObjectInputStream decoder) throws IOException {
        this.Cycle = decoder.readInt();
        this.ScanLine = decoder.readInt();
        this.Frame = decoder.readLong();
        decoder.readFully(this.paletteData);
        decoder.readFully(this.nameTableData);
        decoder.readFully(this.oamData);
        this.v = decoder.readInt();
        this.t = decoder.readInt();
        this.x = decoder.readByte();
        this.w = decoder.readByte();
        this.f = decoder.readByte();
        this.register = decoder.readByte();
        this.nmiOccurred = decoder.readBoolean();
        this.nmiOutput = decoder.readBoolean();
        this.nmiPrevious = decoder.readBoolean();
        this.nmiDelay = decoder.readByte();
        this.nameTableByte = decoder.readByte();
        this.attributeTableByte = decoder.readByte();
        this.lowTileByte = decoder.readByte();
        this.highTileByte = decoder.readByte();
        this.tileData = decoder.readLong();
        this.spriteCount = decoder.readInt();
        for (int i = 0; i < this.spritePatterns.length; i++) {
            this.spritePatterns[i] = decoder.readInt();
        }
        decoder.readFully(this.spritePositions);
        decoder.readFully(this.spritePriorities);
        decoder.readFully(this.spriteIndexes);
        this.flagNameTable = decoder.readByte();
        this.flagIncrement = decoder.readByte();
        this.flagSpriteTable = decoder.readByte();
        this.flagBackgroundTable = decoder.readByte();
        this.flagSpriteSize = decoder.readByte();
        this.flagMasterSlave = decoder.readByte();
        this.flagGrayscale = decoder.readByte();
        this.flagShowLeftBackground = decoder.readByte();
        this.flagShowLeftSprites = decoder.readByte();
        this.flagShowBackground = decoder.readByte();
        this.flagShowSprites = decoder.readByte();
        this.flagRedTint = decoder.readByte();
        this.flagGreenTint = decoder.readByte();
        this.flagBlueTint = decoder.readByte();
        this.flagSpriteZeroHit = decoder.readByte();
        this.flagSpriteOverflow = decoder.readByte();
        this.oamAddress = decoder.readByte();
        this.bufferedData = decoder.readByte();
    }

    // Reset initializes the PPU to its power-on state.
    public void Reset() {
        this.Cycle = 340;
        this.ScanLine = 240;
        this.Frame = 0;
        this.writeControl((byte) 0);
        this.writeMask((byte) 0);
        this.writeOAMAddress((byte) 0);
    }

    public byte readPalette(int address) {
        if (address >= 16 && address % 4 == 0) {
            address -= 16;
        }
        return this.paletteData[address];
    }

    public void writePalette(int address, byte value) {
        if (address >= 16 && address % 4 == 0) {
            address -= 16;
        }
        this.paletteData[address] = value;
    }

    public byte readRegister(int address) {
        switch (address) {
            case 0x2002:
                return this.readStatus();
            case 0x2004:
                return this.readOAMData();
            case 0x2007:
                return this.readData();
        }
        return 0;
    }

    public void writeRegister(int address, byte value) {
        this.register = value;
        switch (address) {
            case 0x2000:
                this.writeControl(value);
                break;
            case 0x2001:
                this.writeMask(value);
                break;
            case 0x2003:
                this.writeOAMAddress(value);
                break;
            case 0x2004:
                this.writeOAMData(value);
                break;
            case 0x2005:
                this.writeScroll(value);
                break;
            case 0x2006:
                this.writeAddress(value);
                break;
            case 0x2007:
                this.writeData(value);
                break;
            case 0x4014:
                this.writeDMA(value);
                break;
        }
    }

    // $2000: PPUCTRL
    public void writeControl(byte value) {
        this.flagNameTable = (byte) ((value >> 0) & 3);
        this.flagIncrement = (byte) ((value >> 2) & 1);
        this.flagSpriteTable = (byte) ((value >> 3) & 1);
        this.flagBackgroundTable = (byte) ((value >> 4) & 1);
        this.flagSpriteSize = (byte) ((value >> 5) & 1);
        this.flagMasterSlave = (byte) ((value >> 6) & 1);
        this.nmiOutput = ((value >> 7) & 1) == 1;
        this.nmiChange();
        // t: ....BA.. ........ = d: ......BA
        this.t = (this.t & 0xF3FF) | (((value & 0x03)) << 10);
    }

    // $2001: PPUMASK
    public void writeMask(byte value) {
        this.flagGrayscale = (byte) ((value >> 0) & 1);
        this.flagShowLeftBackground = (byte) ((value >> 1) & 1);
        this.flagShowLeftSprites = (byte) ((value >> 2) & 1);
        this.flagShowBackground = (byte) ((value >> 3) & 1);
        this.flagShowSprites = (byte) ((value >> 4) & 1);
        this.flagRedTint = (byte) ((value >> 5) & 1);
        this.flagGreenTint = (byte) ((value >> 6) & 1);
        this.flagBlueTint = (byte) ((value >> 7) & 1);
    }

    // $2002: PPUSTATUS
    public byte readStatus() {
        int result = (this.register & 0x1F);
        result |= (this.flagSpriteOverflow & 0xFF) << 5;
        result |= (this.flagSpriteZeroHit & 0xFF) << 6;
        if (this.nmiOccurred) {
            result |= 1 << 7;
        }
        this.nmiOccurred = false;
        this.nmiChange();
        // w:                   = 0
        this.w = 0;
        return (byte) result;
    }

    // $2003: OAMADDR
    public void writeOAMAddress(byte value) {
        this.oamAddress = value;
    }

    // $2004: OAMDATA (read)
    public byte readOAMData() {
        byte data = this.oamData[this.oamAddress & 0xFF];
        if ((this.oamAddress & 0x03) == 0x02) {
            data = (byte) (data & 0xE3);
        }
        return data;
    }

    // $2004: OAMDATA (write)
    public void writeOAMData(byte value) {
        this.oamData[this.oamAddress & 0xFF] = value;
        this.oamAddress++;
    }

    // $2005: PPUSCROLL
    public void writeScroll(byte value) {
        if (this.w == 0) {
            // t: ........ ...HGFED = d: HGFED...
            // x:               CBA = d: .....CBA
            // w:                   = 1
            this.t = (this.t & 0xFFE0) | ((value & 0xFF) >> 3);
            this.x = (byte) (value & 0x07);
            this.w = 1;
        } else {
            // t: .CBA..HG FED..... = d: HGFEDCBA
            // w:                   = 0
            this.t = (this.t & 0x8FFF) | (((value & 0x07)) << 12);
            this.t = (this.t & 0xFC1F) | (((value & 0xF8)) << 2);
            this.w = 0;
        }
    }

    // $2006: PPUADDR
    public void writeAddress(byte value) {
        if (this.w == 0) {
            // t: ..FEDCBA ........ = d: ..FEDCBA
            // t: .X...... ........ = 0
            // w:                   = 1
            this.t = (this.t & 0x80FF) | (((value & 0x3F)) << 8);
            this.w = 1;
        } else {
            // t: ........ HGFEDCBA = d: HGFEDCBA
            // v                    = t
            // w:                   = 0
            this.t = (this.t & 0xFF00) | (value & 0xFF);
            this.v = this.t;
            this.w = 0;
        }
    }

    // $2007: PPUDATA (read)
    public byte readData() {
        int addr = this.v & 0xFFFF;
        byte value = this.Read(addr);
        // emulate buffered reads
        if ((addr % 0x4000) < 0x3F00) {
            byte buffered = this.bufferedData;
            this.bufferedData = value;
            value = buffered;
        } else {
            this.bufferedData = this.Read(addr - 0x1000);
        }
        // increment address
        if (this.flagIncrement == 0) {
            this.v += 1;
        } else {
            this.v += 32;
        }
        return value;
    }

    // $2007: PPUDATA (write)
    public void writeData(byte value) {
        this.Write(this.v, value);
        if (this.flagIncrement == 0) {
            this.v += 1;
        } else {
            this.v += 32;
        }
    }

    // $4014: OAMDMA
    public void writeDMA(byte value) {
        CPU cpu = this.console.CPU;
        int address = (value & 0xFF) << 8;
        for (int i = 0; i < 256; i++) {
            this.oamData[this.oamAddress & 0xFF] = cpu.Read(address);
            this.oamAddress++;
            address++;
        }
        cpu.stall += 513;
        if (cpu.Cycles % 2 == 1) {
            cpu.stall++;
        }
    }

    // NTSC Timing Helper Functions

    public void incrementX() {
        // increment hori(v)
        // if coarse X == 31
        if ((this.v & 0x001F) == 31) {
            // coarse X = 0
            this.v &= 0xFFE0;
            // switch horizontal nametable
            this.v ^= 0x0400;
        } else {
            // increment coarse X
            this.v++;
        }
    }

    public void incrementY() {
        // increment vert(v)
        // if fine Y < 7
        if ((this.v & 0x7000) != 0x7000) {
            // increment fine Y
            this.v += 0x1000;
        } else {
            // fine Y = 0
            this.v &= 0x8FFF;
            // let y = coarse Y
            int y = (this.v & 0x03E0) >> 5;
            if (y == 29) {
                // coarse Y = 0
                y = 0;
                // switch vertical nametable
                this.v ^= 0x0800;
            } else if (y == 31) {
                // coarse Y = 0, nametable not switched
                y = 0;
            } else {
                // increment coarse Y
                y++;
            }
            // put coarse Y back into v
            this.v = (this.v & 0xFC1F) | (y << 5);
        }
    }

    public void copyX() {
        // hori(v) = hori(t)
        // v: .....F.. ...EDCBA = t: .....F.. ...EDCBA
        this.v = (this.v & 0xFBE0) | (this.t & 0x041F);
    }

    public void copyY() {
        // vert(v) = vert(t)
        // v: .IHGF.ED CBA..... = t: .IHGF.ED CBA.....
        this.v = (this.v & 0x841F) | (this.t & 0x7BE0);
    }

    public void nmiChange() {
        boolean nmi = this.nmiOutput && this.nmiOccurred;
        if (nmi && !this.nmiPrevious) {
            // TODO: this fixes some games but the delay shouldn't have to be so
            // long, so the timings are off somewhere
            this.nmiDelay = 15;
        }
        this.nmiPrevious = nmi;
    }

    public void setVerticalBlank() {
        BufferedImage temp = this.front;
        this.front = this.back;
        this.back = temp;
        this.nmiOccurred = true;
        this.nmiChange();
    }

    public void clearVerticalBlank() {
        this.nmiOccurred = false;
        this.nmiChange();
    }

    public void fetchNameTableByte() {
        int address = 0x2000 | (this.v & 0x0FFF);
        this.nameTableByte = this.Read(address);
    }

    public void fetchAttributeTableByte() {
        int address = 0x23C0 | (this.v & 0x0C00) | ((this.v >> 4) & 0x38) | ((this.v >> 2) & 0x07);
        int shift = ((this.v >> 4) & 4) | (this.v & 2);
        this.attributeTableByte = (byte) (((this.Read(address) >> shift) & 3) << 2);
    }

    public void fetchLowTileByte() {
        int fineY = (this.v >> 12) & 7;
        int table = this.flagBackgroundTable;
        int tile = this.nameTableByte & 0xFF;
        int address = 0x1000 * table + tile * 16 + fineY;
        this.lowTileByte = this.Read(address);
    }

    public void fetchHighTileByte() {
        int fineY = (this.v >> 12) & 7;
        int table = this.flagBackgroundTable;
        int tile = this.nameTableByte & 0xFF;
        int address = 0x1000 * table + tile * 16 + fineY;
        this.highTileByte = this.Read(address + 8);
    }

    public void storeTileData() {
        int data = 0;
        for (int i = 0; i < 8; i++) {
            int a = this.attributeTableByte & 0xFF;
            int p1 = (this.lowTileByte & 0x80) >> 7;
            int p2 = (this.highTileByte & 0x80) >> 6;
            this.lowTileByte = (byte) ((this.lowTileByte & 0xFF) << 1);
            this.highTileByte = (byte) ((this.highTileByte & 0xFF) << 1);
            data <<= 4;
            data |= (a | p1 | p2);
        }
        this.tileData |= (long) data;
    }

    public int fetchTileData() {
        return (int) (this.tileData >> 32);
    }

    public byte backgroundPixel() {
        if (this.flagShowBackground == 0) {
            return 0;
        }
        int data = this.fetchTileData() >> ((7 - this.x) * 4);
        return (byte) (data & 0x0F);
    }

    public int[] spritePixel() {
        if (this.flagShowSprites == 0) {
            return new int[] { 0, 0 };
        }
        for (int i = 0; i < this.spriteCount; i++) {
            int offset = (this.Cycle - 1) - (this.spritePositions[i] & 0xFF);
            if (offset < 0 || offset > 7) {
                continue;
            }
            offset = 7 - offset;
            int color = (this.spritePatterns[i] >> (offset * 4)) & 0x0F;
            if (color % 4 == 0) {
                continue;
            }
            return new int[] { i, color };
        }
        return new int[] { 0, 0 };
    }

    public void renderPixel() {
        int x = this.Cycle - 1;
        int y = this.ScanLine;
        byte background = this.backgroundPixel();
        int[] spriteResult = this.spritePixel();
        int i = spriteResult[0];
        byte sprite = (byte) spriteResult[1];
        if (x < 8 && this.flagShowLeftBackground == 0) {
            background = 0;
        }
        if (x < 8 && this.flagShowLeftSprites == 0) {
            sprite = 0;
        }
        boolean b = (background % 4) != 0;
        boolean s = (sprite % 4) != 0;
        byte color;
        if (!b && !s) {
            color = 0;
        } else if (!b && s) {
            color = (byte) (sprite | 0x10);
        } else if (b && !s) {
            color = background;
        } else {
            if (this.spriteIndexes[i] == 0 && x < 255) {
                this.flagSpriteZeroHit = 1;
            }
            if (this.spritePriorities[i] == 0) {
                color = (byte) (sprite | 0x10);
            } else {
                color = background;
            }
        }
        int paletteIndex = (this.readPalette((int) color) & 0xFF) % 64;
        Color c = Palette[paletteIndex];
        this.back.setRGB(x, y, c.getRGB());
    }

    public int fetchSpritePattern(int i, int row) {
        int tile = this.oamData[i * 4 + 1] & 0xFF;
        int attributes = this.oamData[i * 4 + 2] & 0xFF;
        int address;
        if (this.flagSpriteSize == 0) {
            if ((attributes & 0x80) == 0x80) {
                row = 7 - row;
            }
            int table = this.flagSpriteTable;
            address = 0x1000 * table + tile * 16 + row;
        } else {
            if ((attributes & 0x80) == 0x80) {
                row = 15 - row;
            }
            int table = tile & 1;
            tile = tile & 0xFE;
            if (row > 7) {
                tile++;
                row -= 8;
            }
            address = 0x1000 * table + tile * 16 + row;
        }
        int a = (attributes & 3) << 2;
        int lowTileByte = this.Read(address) & 0xFF;
        int highTileByte = this.Read(address + 8) & 0xFF;
        int data = 0;
        for (int j = 0; j < 8; j++) {
            int p1, p2;
            if ((attributes & 0x40) == 0x40) {
                p1 = (lowTileByte & 1) << 0;
                p2 = (highTileByte & 1) << 1;
                lowTileByte >>= 1;
                highTileByte >>= 1;
            } else {
                p1 = (lowTileByte & 0x80) >> 7;
                p2 = (highTileByte & 0x80) >> 6;
                lowTileByte <<= 1;
                highTileByte <<= 1;
            }
            data <<= 4;
            data |= (a | p1 | p2);
        }
        return data;
    }

    public void evaluateSprites() {
        int h;
        if (this.flagSpriteSize == 0) {
            h = 8;
        } else {
            h = 16;
        }
        int count = 0;
        for (int i = 0; i < 64; i++) {
            int y = this.oamData[i * 4 + 0] & 0xFF;
            int a = this.oamData[i * 4 + 2] & 0xFF;
            int x = this.oamData[i * 4 + 3] & 0xFF;
            int row = this.ScanLine - y;
            if (row < 0 || row >= h) {
                continue;
            }
            if (count < 8) {
                this.spritePatterns[count] = this.fetchSpritePattern(i, row);
                this.spritePositions[count] = this.oamData[i * 4 + 3];
                this.spritePriorities[count] = (byte) ((a >> 5) & 1);
                this.spriteIndexes[count] = (byte) i;
            }
            count++;
        }
        if (count > 8) {
            count = 8;
            this.flagSpriteOverflow = 1;
        }
        this.spriteCount = count;
    }

    // tick updates Cycle, ScanLine and Frame counters.
    public void tick() {
        if (this.nmiDelay > 0) {
            this.nmiDelay--;
            if (this.nmiDelay == 0 && this.nmiOutput && this.nmiOccurred) {
                this.console.CPU.triggerNMI();
            }
        }

        if (this.flagShowBackground != 0 || this.flagShowSprites != 0) {
            if (this.f == 1 && this.ScanLine == 261 && this.Cycle == 339) {
                this.Cycle = 0;
                this.ScanLine = 0;
                this.Frame++;
                this.f = (byte) (this.f ^ 1);
                return;
            }
        }
        this.Cycle++;
        if (this.Cycle > 340) {
            this.Cycle = 0;
            this.ScanLine++;
            if (this.ScanLine > 261) {
                this.ScanLine = 0;
                this.Frame++;
                this.f = (byte) (this.f ^ 1);
            }
        }
    }

    // Step executes a single PPU cycle.
    public void Step() {
        this.tick();

        boolean renderingEnabled = this.flagShowBackground != 0 || this.flagShowSprites != 0;
        boolean preLine = this.ScanLine == 261;
        boolean visibleLine = this.ScanLine < 240;
        // postLine := this.ScanLine == 240 (not used)
        boolean renderLine = preLine || visibleLine;
        boolean preFetchCycle = this.Cycle >= 321 && this.Cycle <= 336;
        boolean visibleCycle = this.Cycle >= 1 && this.Cycle <= 256;
        boolean fetchCycle = preFetchCycle || visibleCycle;

        // background logic
        if (renderingEnabled) {
            if (visibleLine && visibleCycle) {
                this.renderPixel();
            }
            if (renderLine && fetchCycle) {
                this.tileData <<= 4;
                switch (this.Cycle % 8) {
                    case 1:
                        this.fetchNameTableByte();
                        break;
                    case 3:
                        this.fetchAttributeTableByte();
                        break;
                    case 5:
                        this.fetchLowTileByte();
                        break;
                    case 7:
                        this.fetchHighTileByte();
                        break;
                    case 0:
                        this.storeTileData();
                        break;
                }
            }
            if (preLine && this.Cycle >= 280 && this.Cycle <= 304) {
                this.copyY();
            }
            if (renderLine) {
                if (fetchCycle && (this.Cycle % 8) == 0) {
                    this.incrementX();
                }
                if (this.Cycle == 256) {
                    this.incrementY();
                }
                if (this.Cycle == 257) {
                    this.copyX();
                }
            }
        }

        // sprite logic
        if (renderingEnabled) {
            if (this.Cycle == 257) {
                if (visibleLine) {
                    this.evaluateSprites();
                } else {
                    this.spriteCount = 0;
                }
            }
        }

        // vblank logic
        if (this.ScanLine == 241 && this.Cycle == 1) {
            this.setVerticalBlank();
        }
        if (preLine && this.Cycle == 1) {
            this.clearVerticalBlank();
            this.flagSpriteZeroHit = 0;
            this.flagSpriteOverflow = 0;
        }
    }

    // Helper methods for Memory read and write using the Memory interface.
    public byte Read(int address) {
        return this.Memory.Read(address);
    }

    public void Write(int address, byte value) {
        this.Memory.Write(address, value);
    }
}
