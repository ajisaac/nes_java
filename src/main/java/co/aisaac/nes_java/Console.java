package co.aisaac.nes_java;

import co.aisaac.nes_java.filter.FilterChain;

import java.io.IOException;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;

import static co.aisaac.nes_java.CPU.CPUFrequency;
import static co.aisaac.nes_java.CPU.NewCPU;
import static co.aisaac.nes_java.Controller.NewController;
import static co.aisaac.nes_java.INes.LoadNESFile;
import static co.aisaac.nes_java.Mapper.NewMapper;
import static co.aisaac.nes_java.apu.APU.NewAPU;

import co.aisaac.nes_java.apu.APU;

public class Console {
    public CPU CPU;
    public APU APU;
    public PPU PPU;
    public Cartridge Cartridge;
    public Controller Controller1;
    public Controller Controller2;
    public Mapper Mapper;
    public byte[] RAM;


    // Constructor matching the struct initialization order.
    public Console(CPU CPU, APU APU, PPU PPU, Cartridge Cartridge, Controller Controller1, Controller Controller2, Mapper Mapper, byte[] RAM) {
        this.CPU = CPU;
        this.APU = APU;
        this.PPU = PPU;
        this.Cartridge = Cartridge;
        this.Controller1 = Controller1;
        this.Controller2 = Controller2;
        this.Mapper = Mapper;
        this.RAM = RAM;
    }

    public static Console NewConsole(String path) throws Exception {
        Cartridge cartridge = LoadNESFile(path);
        byte[] ram = new byte[2048];
        Controller controller1 = NewController();
        Controller controller2 = NewController();
        Console console = new Console(null, null, null, cartridge, controller1, controller2, null, ram);
        Mapper mapper = NewMapper(console);
        console.Mapper = mapper;
        console.CPU = NewCPU(console);
        console.APU = NewAPU(console);
        console.PPU = new PPU(console);
        return console;
    }

    public void Reset() {
        this.CPU.Reset();
    }

    public int Step() {
        int cpuCycles = this.CPU.Step();
        int ppuCycles = cpuCycles * 3;
        for (int i = 0; i < ppuCycles; i++) {
            this.PPU.Step();
            this.Mapper.Step();
        }
        for (int i = 0; i < cpuCycles; i++) {
            this.APU.Step();
        }
        return cpuCycles;
    }

    public int StepFrame() {
        int cpuCycles = 0;
//        int frame = this.PPU.Frame;
//        while (frame == this.PPU.Frame) {
//            cpuCycles += this.Step();
//        }
        return cpuCycles;
    }

    public void StepSeconds(double seconds) {
        int cycles = (int) (CPUFrequency * seconds);
        while (cycles > 0) {
            cycles -= this.Step();
        }
    }

    public BufferedImage Buffer() {
        return this.PPU.front;
    }

    public Color BackgroundColor() {
//        int index = this.PPU.readPalette(0) % 64;
//        return Palette[index];
        return null;
    }

    public void SetButtons1(boolean[] buttons) {
        this.Controller1.SetButtons(buttons);
    }

    public void SetButtons2(boolean[] buttons) {
        this.Controller2.SetButtons(buttons);
    }

    public void SetAudioChannel(BlockingQueue<Float> channel) {
        this.APU.channel = channel;
    }

    public void SetAudioSampleRate(double sampleRate) {
        if (sampleRate != 0) {
            this.APU.sampleRate = CPUFrequency / sampleRate;
            // todo
            this.APU.filterChain = new FilterChain(
//                    new HighPassFilter((float) sampleRate, 90),
//                    new HighPassFilter((float) sampleRate, 440),
//                    new LowPassFilter((float) sampleRate, 14000)
            );
        } else {
            this.APU.filterChain = null;
        }
    }

    public void SaveState(String filename) throws Exception {
        File fileObj = new File(filename);
        String dir = fileObj.getParent();
        if (dir != null) {
            File dirFile = new File(dir);
            if (!dirFile.exists() && !dirFile.mkdirs()) {
                throw new IOException("Failed to create directories: " + dir);
            }
        }
        try (FileOutputStream fileOut = new FileOutputStream(filename);
             ObjectOutputStream encoder = new ObjectOutputStream(fileOut)) {
            this.Save(encoder);
        }
    }

    public void Save(ObjectOutputStream encoder) throws Exception {
        encoder.writeObject(this.RAM);
        this.CPU.Save(encoder);
        this.APU.Save(encoder);
        this.PPU.Save(encoder);
        this.Cartridge.Save(encoder);
        this.Mapper.Save(encoder);
        encoder.writeBoolean(true);
    }

    public void LoadState(String filename) throws Exception {
        try (FileInputStream fileIn = new FileInputStream(filename);
             ObjectInputStream decoder = new ObjectInputStream(fileIn)) {
            this.Load(decoder);
        }
    }

    public void Load(ObjectInputStream decoder) throws Exception {
        Object obj = decoder.readObject();
        if (obj instanceof byte[]) {
            this.RAM = (byte[]) obj;
        }
//        this.CPU.Load(decoder);
        this.APU.Load(decoder);
        this.PPU.Load(decoder);
        this.Cartridge.Load(decoder);
        this.Mapper.Load(decoder);
        boolean dummy = decoder.readBoolean();
    }
}

