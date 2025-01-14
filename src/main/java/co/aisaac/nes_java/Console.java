package co.aisaac.nes_java;

import java.io.IOException;

public class Console {

    PPU ppu;
    APU apu;
    CPU cpu;
    RAM ram;

    Controller controller1;
    Controller controller2;
    Cartridge cartridge;
    Mapper mapper;

    Console(String path) throws IOException {
        ppu = new PPU();
        apu = new APU();
        controller1 = new Controller();
        controller2 = new Controller();
        mapper = new Mapper();

        cartridge = new Cartridge(path);
        ram = new RAM();
        cpu = new CPU();
    }

    void reset() {
        cpu.reset();
    }

    long step() {
        return 0;
    }

    long stepFrame() {
        long cpuCycles = 0;
        long frame = ppu.frame;
        while (frame == ppu.frame) {
            cpuCycles += step();
        }
        return cpuCycles;
    }

    public void stepSeconds(long seconds) {
        long cycles = 1789773 * seconds * 1000;
        while (cycles > 0) {
            cycles -= step();
        }
    }

    int[] buffer() {
        return ppu.front;
    }

    int backgroundColor() {
        return 0;
    }

    void setButtons1(byte buttons) {
    }

    void setButtons2(byte buttons) {
    }

    void setAudioChannel() {
    }

    void setAudioSampleRate(long sampleRate) {
    }

    void saveState(String path) {
    }

    void loadState(String path) {
    }

}
