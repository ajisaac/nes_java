package co.aisaac.nes_java;

import java.awt.Color;
import java.awt.image.BufferedImage;

import co.aisaac.nes_java.cpu.CPU;
import co.aisaac.nes_java.memory.Memory;
import co.aisaac.nes_java.memory.PPUMemory;

public class PPU {
    // Memory interface
    private Memory memory;
    public Console console; // reference to parent object

    public int Cycle;    // 0-340
    public int ScanLine; // 0-261, 0-239=visible, 240=post, 241-260=vblank, 261=pre
    public long Frame;   // frame counter

    // storage variables
    public int /*byte*/[] paletteData = new int /*byte*/[32];
    public int /*byte*/[] nameTableData = new int /*byte*/[2048];
    public int /*byte*/[] oamData = new int /*byte*/[256];
    public BufferedImage front;
    public BufferedImage back;

    // PPU registers
    public int v; // current vram address (15 bit)
    public int t; // temporary vram address (15 bit)
    public int /*byte*/ x;   // fine x scroll (3 bit)
    public int /*byte*/ w;   // write toggle (1 bit)
    public int /*byte*/ f;   // even/odd frame flag (1 bit)

    public int /*byte*/ register;

    // NMI flags
    public boolean nmiOccurred;
    public boolean nmiOutput;
    public boolean nmiPrevious;
    public int /*byte*/ nmiDelay;

    // background temporary variables
    public int /*byte*/ nameTableByte;
    public int /*byte*/ attributeTableByte;
    public int /*byte*/ lowTileByte;
    public int /*byte*/ highTileByte;
    public long tileData;

    // sprite temporary variables
    public int spriteCount;
    public int[] spritePatterns = new int[8];
    public int /*byte*/[] spritePositions = new int /*byte*/[8];
    public int /*byte*/[] spritePriorities = new int /*byte*/[8];
    public int /*byte*/[] spriteIndexes = new int /*byte*/[8];

    // $2000 PPUCTRL
    public int /*byte*/ flagNameTable;       // 0: $2000; 1: $2400; 2: $2800; 3: $2C00
    public int /*byte*/ flagIncrement;       // 0: add 1; 1: add 32
    public int /*byte*/ flagSpriteTable;     // 0: $0000; 1: $1000; ignored in 8x16 mode
    public int /*byte*/ flagBackgroundTable; // 0: $0000; 1: $1000
    public int /*byte*/ flagSpriteSize;      // 0: 8x8; 1: 8x16
    public int /*byte*/ flagMasterSlave;     // 0: read EXT; 1: write EXT

    // $2001 PPUMASK
    public int /*byte*/ flagGrayscale;          // 0: color; 1: grayscale
    public int /*byte*/ flagShowLeftBackground; // 0: hide; 1: show
    public int /*byte*/ flagShowLeftSprites;    // 0: hide; 1: show
    public int /*byte*/ flagShowBackground;     // 0: hide; 1: show
    public int /*byte*/ flagShowSprites;        // 0: hide; 1: show
    public int /*byte*/ flagRedTint;            // 0: normal; 1: emphasized
    public int /*byte*/ flagGreenTint;          // 0: normal; 1: emphasized
    public int /*byte*/ flagBlueTint;           // 0: normal; 1: emphasized

    // $2002 PPUSTATUS
    public int /*byte*/ flagSpriteZeroHit;
    public int /*byte*/ flagSpriteOverflow;

    // $2003 OAMADDR
    public int /*byte*/ oamAddress;

    // $2007 PPUDATA
    public int /*byte*/ bufferedData; // for buffered reads

    // Palette is declared as a static array with 64 entries.
    public static Color[] Palette = new Color[64];
    static {
        for (int i = 0; i < 64; i++) {
            Palette[i] = new Color(0, 0, 0); // default to black
        }
    }

    // Constructor equivalent to NewPPU in Go.
    public PPU(Console console) {
        this.console = console;
        this.memory = new PPUMemory(console);
        this.front = new BufferedImage(256, 240, BufferedImage.TYPE_INT_ARGB);
        this.back = new BufferedImage(256, 240, BufferedImage.TYPE_INT_ARGB);
        this.Reset();
    }

    // Reset initializes the PPU to its power-on state.
    public void Reset() {
        this.Cycle = 340;
        this.ScanLine = 240;
        this.Frame = 0;
        this.writeControl((int /*byte*/) 0);
        this.writeMask((int /*byte*/) 0);
        this.writeOAMAddress((int /*byte*/) 0);
    }

    public int /*byte*/ readPalette(int address) {
        if (address >= 16 && address % 4 == 0) {
            address -= 16;
        }
        return this.paletteData[address];
    }

    public void writePalette(int address, int /*byte*/ value) {
        if (address >= 16 && address % 4 == 0) {
            address -= 16;
        }
        this.paletteData[address] = value;
    }

    public int /*byte*/ readRegister(int address) {
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

    public void writeRegister(int address, int /*byte*/ value) {
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
    void writeControl(int /*byte*/ value) {
        this.flagNameTable = (int /*byte*/) ((value >> 0) & 3);
        this.flagIncrement = (int /*byte*/) ((value >> 2) & 1);
        this.flagSpriteTable = (int /*byte*/) ((value >> 3) & 1);
        this.flagBackgroundTable = (int /*byte*/) ((value >> 4) & 1);
        this.flagSpriteSize = (int /*byte*/) ((value >> 5) & 1);
        this.flagMasterSlave = (int /*byte*/) ((value >> 6) & 1);
        this.nmiOutput = ((value >> 7) & 1) == 1;
        this.nmiChange();
        // t: ....BA.. ........ = d: ......BA
        this.t = (this.t & 0xF3FF) | (((value & 0x03)) << 10);
    }

    // $2001: PPUMASK
    void writeMask(int /*byte*/ value) {
        this.flagGrayscale = (int /*byte*/) ((value >> 0) & 1);
        this.flagShowLeftBackground = (int /*byte*/) ((value >> 1) & 1);
        this.flagShowLeftSprites = (int /*byte*/) ((value >> 2) & 1);
        this.flagShowBackground = (int /*byte*/) ((value >> 3) & 1);
        this.flagShowSprites = (int /*byte*/) ((value >> 4) & 1);
        this.flagRedTint = (int /*byte*/) ((value >> 5) & 1);
        this.flagGreenTint = (int /*byte*/) ((value >> 6) & 1);
        this.flagBlueTint = (int /*byte*/) ((value >> 7) & 1);
    }

    // $2002: PPUSTATUS
    int /*byte*/ readStatus() {
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
        return (int /*byte*/) result;
    }

    // $2003: OAMADDR
    void writeOAMAddress(int /*byte*/ value) {
        this.oamAddress = value;
    }

    // $2004: OAMDATA (read)
    int /*byte*/ readOAMData() {
        int /*byte*/ data = this.oamData[this.oamAddress & 0xFF];
        if ((this.oamAddress & 0x03) == 0x02) {
            data = (int /*byte*/) (data & 0xE3);
        }
        return data;
    }

    // $2004: OAMDATA (write)
    void writeOAMData(int /*byte*/ value) {
        this.oamData[this.oamAddress & 0xFF] = value;
        this.oamAddress++;
    }

    // $2005: PPUSCROLL
    void writeScroll(int /*byte*/ value) {
        if (this.w == 0) {
            // t: ........ ...HGFED = d: HGFED...
            // x:               CBA = d: .....CBA
            // w:                   = 1
            this.t = (this.t & 0xFFE0) | ((value & 0xFF) >> 3);
            this.x = (int /*byte*/) (value & 0x07);
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
    void writeAddress(int /*byte*/ value) {
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
    int /*byte*/ readData() {
        int addr = this.v & 0xFFFF;
        int /*byte*/ value = this.read(addr);
        // emulate buffered reads
        if ((addr % 0x4000) < 0x3F00) {
            int /*byte*/ buffered = this.bufferedData;
            this.bufferedData = value;
            value = buffered;
        } else {
            this.bufferedData = this.read(addr - 0x1000);
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
    void writeData(int /*byte*/ value) {
        this.write(this.v, value);
        if (this.flagIncrement == 0) {
            this.v += 1;
        } else {
            this.v += 32;
        }
    }

    // $4014: OAMDMA
    void writeDMA(int /*byte*/ value) {
        CPU cpu = this.console.CPU;
        int address = (value & 0xFF) << 8;
        for (int i = 0; i < 256; i++) {
            this.oamData[this.oamAddress & 0xFF] = (int /*byte*/) cpu.Read(address);
            this.oamAddress++;
            address++;
        }
        cpu.stall += 513;
        if (cpu.Cycles % 2 == 1) {
            cpu.stall++;
        }
    }

    // NTSC Timing Helper Functions

    void incrementX() {
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

    void incrementY() {
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

    void copyX() {
        // hori(v) = hori(t)
        // v: .....F.. ...EDCBA = t: .....F.. ...EDCBA
        this.v = (this.v & 0xFBE0) | (this.t & 0x041F);
    }

    void copyY() {
        // vert(v) = vert(t)
        // v: .IHGF.ED CBA..... = t: .IHGF.ED CBA.....
        this.v = (this.v & 0x841F) | (this.t & 0x7BE0);
    }

    void nmiChange() {
        boolean nmi = this.nmiOutput && this.nmiOccurred;
        if (nmi && !this.nmiPrevious) {
            // TODO: this fixes some games but the delay shouldn't have to be so
            // long, so the timings are off somewhere
            this.nmiDelay = 15;
        }
        this.nmiPrevious = nmi;
    }

    void setVerticalBlank() {
        BufferedImage temp = this.front;
        this.front = this.back;
        this.back = temp;
        this.nmiOccurred = true;
        this.nmiChange();
    }

    void clearVerticalBlank() {
        this.nmiOccurred = false;
        this.nmiChange();
    }

    void fetchNameTableByte() {
        int address = 0x2000 | (this.v & 0x0FFF);
        this.nameTableByte = this.read(address);
    }

    void fetchAttributeTableByte() {
        int address = 0x23C0 | (this.v & 0x0C00) | ((this.v >> 4) & 0x38) | ((this.v >> 2) & 0x07);
        int shift = ((this.v >> 4) & 4) | (this.v & 2);
        this.attributeTableByte = (int /*byte*/) (((this.read(address) >> shift) & 3) << 2);
    }

    void fetchLowTileByte() {
        int fineY = (this.v >> 12) & 7;
        int table = this.flagBackgroundTable;
        int tile = this.nameTableByte & 0xFF;
        int address = 0x1000 * table + tile * 16 + fineY;
        this.lowTileByte = this.read(address);
    }

    void fetchHighTileByte() {
        int fineY = (this.v >> 12) & 7;
        int table = this.flagBackgroundTable;
        int tile = this.nameTableByte & 0xFF;
        int address = 0x1000 * table + tile * 16 + fineY;
        this.highTileByte = this.read(address + 8);
    }

    void storeTileData() {
        int data = 0;
        for (int i = 0; i < 8; i++) {
            int a = this.attributeTableByte & 0xFF;
            int p1 = (this.lowTileByte & 0x80) >> 7;
            int p2 = (this.highTileByte & 0x80) >> 6;
            this.lowTileByte = (int /*byte*/) ((this.lowTileByte & 0xFF) << 1);
            this.highTileByte = (int /*byte*/) ((this.highTileByte & 0xFF) << 1);
            data <<= 4;
            data |= (a | p1 | p2);
        }
        this.tileData |= (long) data;
    }

    int fetchTileData() {
        return (int) (this.tileData >> 32);
    }

    int /*byte*/ backgroundPixel() {
        if (this.flagShowBackground == 0) {
            return 0;
        }
        int data = this.fetchTileData() >> ((7 - this.x) * 4);
        return (int /*byte*/) (data & 0x0F);
    }

    int[] spritePixel() {
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

    void renderPixel() {
        int x = this.Cycle - 1;
        int y = this.ScanLine;
        int /*byte*/ background = this.backgroundPixel();
        int[] spriteResult = this.spritePixel();
        int i = spriteResult[0];
        int /*byte*/ sprite = (int /*byte*/) spriteResult[1];
        if (x < 8 && this.flagShowLeftBackground == 0) {
            background = 0;
        }
        if (x < 8 && this.flagShowLeftSprites == 0) {
            sprite = 0;
        }
        boolean b = (background % 4) != 0;
        boolean s = (sprite % 4) != 0;
        int /*byte*/ color;
        if (!b && !s) {
            color = 0;
        } else if (!b && s) {
            color = (int /*byte*/) (sprite | 0x10);
        } else if (b && !s) {
            color = background;
        } else {
            if (this.spriteIndexes[i] == 0 && x < 255) {
                this.flagSpriteZeroHit = 1;
            }
            if (this.spritePriorities[i] == 0) {
                color = (int /*byte*/) (sprite | 0x10);
            } else {
                color = background;
            }
        }
        int paletteIndex = (this.readPalette((int) color) & 0xFF) % 64;
        Color c = Palette[paletteIndex];
        this.back.setRGB(x, y, c.getRGB());
    }

    int fetchSpritePattern(int i, int row) {
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
        int lowTileByte = this.read(address) & 0xFF;
        int highTileByte = this.read(address + 8) & 0xFF;
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

    void evaluateSprites() {
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
                this.spritePriorities[count] = (int /*byte*/) ((a >> 5) & 1);
                this.spriteIndexes[count] = (int /*byte*/) i;
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
    void tick() {
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
                this.f = (int /*byte*/) (this.f ^ 1);
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
                this.f = (int /*byte*/) (this.f ^ 1);
            }
        }
    }

    // Step executes a single PPU cycle.
    public void step() {
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
    int /*byte*/ read(int address) {
        return this.memory.read(address);
    }

    void write(int address, int /*byte*/ value) {
        this.memory.write(address, value);
    }
}
