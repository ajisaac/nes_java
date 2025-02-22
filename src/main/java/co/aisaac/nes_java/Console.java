package co.aisaac.nes_java;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static co.aisaac.nes_java.cpu.CPU.CPUFrequency;
import static co.aisaac.nes_java.Controller.newController;
import static co.aisaac.nes_java.INes.LoadNESFile;
import static co.aisaac.nes_java.mappers.Mapper.NewMapper;

import co.aisaac.nes_java.apu.APU;
import co.aisaac.nes_java.cpu.CPU;
import co.aisaac.nes_java.mappers.Mapper;

public class Console {
    public CPU CPU;
    public APU APU;
    public PPU PPU;
    public Cartridge cartridge;
    public Controller controller1;
    public Controller controller2;
    public Mapper mapper;
    public int[] ram; // byte[]

    // Constructor matching the struct initialization order.
    public Console(String path) {
        try {
            this.cartridge = LoadNESFile(path);
            this.mapper = NewMapper(this);
            this.CPU = new CPU(this);
            this.APU = new APU(this);
            this.PPU = new PPU(this);
            this.controller1 = newController();
            this.controller2 = newController();
            this.ram = new int[2048]; // byte[2048]
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void Reset() {
        this.CPU.reset();
    }

    public int Step() {
        int cpuCycles = this.CPU.Step();
        int ppuCycles = cpuCycles * 3;
        for (int i = 0; i < ppuCycles; i++) {
            this.PPU.step();
            this.mapper.Step();
        }
        for (int i = 0; i < cpuCycles; i++) {
            this.APU.Step();
        }
        return cpuCycles;
    }

    public int StepFrame() {
        int cpuCycles = 0;
        long frame = this.PPU.Frame;
        while (frame == this.PPU.Frame) {
            cpuCycles += this.Step();
        }
        return cpuCycles;
    }

    public void stepSeconds(double seconds) {
        int cycles = (int) (CPUFrequency * seconds);
        while (cycles > 0) {
            cycles -= this.Step();
        }
    }

    public BufferedImage Buffer() {
        return this.PPU.front;
    }

    public Color BackgroundColor() {
        int index = this.PPU.readPalette(0) % 64;
        return Palette.palette[index];
    }

    public void SetButtons1(boolean[] buttons) {
        this.controller1.setButtons(buttons);
    }

    public void SetButtons2(boolean[] buttons) {
        this.controller2.setButtons(buttons);
    }


}

