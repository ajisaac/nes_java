package co.aisaac.nes_java;

public class Cartridge {
    public byte[] PRG;     // PRG-ROM banks
    public byte[] CHR;     // CHR-ROM banks
    public byte[] SRAM;    // Save RAM
    public byte mapper;    // mapper type
    public byte mirror;    // mirroring mode
    public byte battery;   // battery present

    public Cartridge(byte[] PRG, byte[] CHR, byte[] SRAM, byte Mapper, byte Mirror, byte Battery) {
        this.PRG = PRG;
        this.CHR = CHR;
        this.SRAM = SRAM;
        this.mapper = Mapper;
        this.mirror = Mirror;
        this.battery = Battery;
    }

    public static Cartridge NewCartridge(byte[] prg, byte[] chr, byte mapper, byte mirror, byte battery) {
        byte[] sram = new byte[0x2000];
        return new Cartridge(prg, chr, sram, mapper, mirror, battery);
    }


}
