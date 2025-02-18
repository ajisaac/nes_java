package co.aisaac.nes_java;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


// Cartridge struct translated from Go.
// PRG: PRG-ROM banks
// CHR: CHR-ROM banks
// SRAM: Save RAM
// Mapper: mapper type
// Mirror: mirroring mode
// Battery: battery present
public class Cartridge {
    public byte[] PRG;     // PRG-ROM banks
    public byte[] CHR;     // CHR-ROM banks
    public byte[] SRAM;    // Save RAM
    public byte Mapper;    // mapper type
    public byte Mirror;    // mirroring mode
    public byte Battery;   // battery present

    public Cartridge(byte[] PRG, byte[] CHR, byte[] SRAM, byte Mapper, byte Mirror, byte Battery) {
        this.PRG = PRG;
        this.CHR = CHR;
        this.SRAM = SRAM;
        this.Mapper = Mapper;
        this.Mirror = Mirror;
        this.Battery = Battery;
    }

    // NewCartridge creates a new Cartridge instance.
    // It allocates SRAM with size 0x2000 and initializes the Cartridge fields.
    public static Cartridge NewCartridge(byte[] prg, byte[] chr, byte mapper, byte mirror, byte battery) {
        byte[] sram = new byte[0x2000];
        return new Cartridge(prg, chr, sram, mapper, mirror, battery);
    }

    // Save serializes selected Cartridge fields using a GobEncoder.
    public void Save(GobEncoder encoder) throws IOException {
        encoder.Encode(this.PRG);
        encoder.Encode(this.CHR);
        encoder.Encode(this.SRAM);
        encoder.Encode(this.Mirror);
    }

    // Load deserializes selected Cartridge fields using a GobDecoder.
    public void Load(GobDecoder decoder) throws IOException, ClassNotFoundException {
        this.PRG = (byte[]) decoder.Decode(byte[].class);
        this.CHR = (byte[]) decoder.Decode(byte[].class);
        this.SRAM = (byte[]) decoder.Decode(byte[].class);
        this.Mirror = (Byte) decoder.Decode(Byte.class);
    }

    // GobEncoder mimics the Go gob.Encoder functionality using Java ObjectOutputStream.
    class GobEncoder {
        private ObjectOutputStream out;

        public GobEncoder(OutputStream os) throws IOException {
            this.out = new ObjectOutputStream(os);
        }

        public void Encode(Object obj) throws IOException {
            out.writeObject(obj);
        }
    }

    // GobDecoder mimics the Go gob.Decoder functionality using Java ObjectInputStream.
    class GobDecoder {
        private ObjectInputStream in;

        public GobDecoder(InputStream is) throws IOException {
            this.in = new ObjectInputStream(is);
        }

        public Object Decode(Class<?> clazz) throws IOException, ClassNotFoundException {
            return in.readObject();
        }
    }

}
/*
 Cartridge loaded from a .nes file
 http://wiki.nesdev.com/w/index.php/INES
 http://nesdev.com/NESDoc.pdf (page 28)

 Header (16 bytes)
 Trainer, if present (0 or 512 bytes)
 PRG ROM data (16384 * x bytes)
 CHR ROM data, if present (8192 * y bytes)
 PlayChoice INST-ROM, if present (0 or 8192 bytes)
 PlayChoice PROM, if present (16 bytes Data, 16 bytes CounterOut) (this is often missing; see PC10 ROM-Images for details)
public class Cartridge {

    // PRG-ROM
    byte[] prg;

    // CHR-ROM
    byte[] chr;

    // save ram?
    byte[] sram;

    // mapper type
    int mapper;

    // mirroring mode?
    int mirror;

    // has battery?
    int battery;

    /**
     * The beginning of every ines file format
     *
    final int MAGIC = 0x1a53454e;

    /**
     * The first 16 bytes
     *
    byte[] header;

    public Cartridge(String file) throws IOException {

        // open file
        Path path = Path.of(file);
        byte[] bytes = Files.readAllBytes(path);

        // read file header
        header = Arrays.copyOfRange(bytes, 0, 16);

        // first 4 bytes
        verifyMagicNumber();

        // num of PRG-ROM in 16kb chunks
        byte numPrg = header[4];

        // num of CHR-ROM in 8kb chunks, 0 means board uses CHR-RAM
        byte numChr = header[5];

        // control bits
        byte control1 = header[6];
        // control bits
        byte control2 = header[7];

        // PRG-RAM size ( x 8KB ) each
        byte numRam = header[8];

        // 9, 10, 11, 12, 13, 14, 15 -> these 7 bytes are unused



        // load trainer if present

        // load PRG ROM

        // LOAD CHR ROM

        // PlayChoice INST-ROM if present

        // PlayChoice PROM, if present


        // read in all the bytes from the file

    }

    // verify magic number, first 4 bytes
    private void verifyMagicNumber() {
        int i = header[0] | header[1] << 8 | header[2] << 16 | header[3] << 24;
        if (i != MAGIC) throw new IllegalStateException("Magic number unexpected: 0x" + HexFormat.of().toHexDigits(i));
    }

    // TODO everything below here is unused at the moment, until we go for version 2 which is the more accurate version
    // FLAGS 6

    // 0 == vertical arrangement / "horizontal mirrored" (CIRAM a10 = PPU A11)
    // 1 == horizontal arrangement / "veritcal mirrored" (CIRAM a10 = PPU A10)
    private int nametableArrangement() {
        return header[6] | 0b00000001;
    }

    private int hasBatteryPrgRam() {
        return header[6] | 0b00000010;
    }

    private int hasTrainerData() {
        return header[6] | 0b00000100;
    }

    private int hasAlternativeNametableLayout() {
        return header[6] | 0b00001000;
    }

    // first 4 bits of 6 and 7
    // 6's is low nibble, 7's is high nibble
    private byte getMapper() {
        return (byte) ((header[6] & 0b11110000) | ((header[7] & 0b11110000) >> 4));
    }

    // FLAGS 7

    private int hasVsUnisystem() {
        return header[7] | 0b00000001;
    }

    private int hasPlayChoice10() {
        return header[7] | 0b00000010;
    }

    // if equal to 2, then yes
    private int hasNes2format() {
        return header[7] | 0b00001100;
    }

    // FLAGS 8

    private int prgRamSize() {
        return header[8];
    }

    // 0:NTSC or 1:PAL
    private int tvSystem() {
        return header[9] | 0b00000001;
    }

    // reserved, set to 0
    private int f9Reserved() {
        int i = header[9] & 0b11111110;
        if (i != 0) throw new IllegalStateException("f9 bits should be 0");
        return i;
    }

    // FLAGS 10

    // 0: NTSC, 2: PAL, 1/3: Dual Compatible
    private int f10tvSystem() {
        return header[10] | 0b00000011;
    }

    // 0: present, 1: not present
    private int f10prgRam() {
        return header[10] | 0b00010000;
    }

    // 0: no conflicts, 1: conflicts
    private int f10busConflicts() {
        return header[10] | 0b00100000;
    }
}*/
