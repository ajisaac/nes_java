package co.aisaac.nes_java;

public class Cartridge {
    public int /*byte*/[] PRG;     // PRG-ROM banks
    public int /*byte*/[] CHR;     // CHR-ROM banks
    public int /*byte*/[] SRAM;    // Save RAM
    public int /*byte*/ mapper;    // mapper type
    public int /*byte*/ mirror;    // mirroring mode
    public int /*byte*/ battery;   // battery present

    public Cartridge(int /*byte*/[] PRG, int /*byte*/[] CHR, int /*byte*/[] SRAM, int /*byte*/ Mapper, int /*byte*/ Mirror, int /*byte*/ Battery) {
        this.PRG = PRG;
        this.CHR = CHR;
        this.SRAM = SRAM;
        this.mapper = Mapper;
        this.mirror = Mirror;
        this.battery = Battery;
    }

    public static Cartridge NewCartridge(int /*byte*/[] prg, int /*byte*/[] chr, int /*byte*/ mapper, int /*byte*/ mirror, int /*byte*/ battery) {
        int /*byte*/[] sram = new int /*byte*/[0x2000];
        return new Cartridge(prg, chr, sram, mapper, mirror, battery);
    }


}
