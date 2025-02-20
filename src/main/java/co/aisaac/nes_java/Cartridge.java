package co.aisaac.nes_java;

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

    public static Cartridge NewCartridge(byte[] prg, byte[] chr, byte mapper, byte mirror, byte battery) {
        byte[] sram = new byte[0x2000];
        return new Cartridge(prg, chr, sram, mapper, mirror, battery);
    }


}
