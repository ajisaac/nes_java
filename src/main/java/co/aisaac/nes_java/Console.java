package co.aisaac.nes_java;

public class Console {

    PPU ppu;
    APU apu;
    CPU cpu;
    RAM ram;

    Controller controller1;
    Controller controller2;
    Cartridge cartridge;
    Mapper mapper;

    Console(String path) {
        ppu = new PPU();
    }

    public void stepSeconds(long seconds) {

        long cycles = 1789773 * seconds * 1000;
        while (cycles > 0) {
            cycles -= step();
        }
    }
}
