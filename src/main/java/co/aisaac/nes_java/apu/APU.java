package co.aisaac.nes_java.apu;

import co.aisaac.nes_java.Console;
import co.aisaac.nes_java.filter.FilterChain;

import java.io.*;
import java.util.concurrent.*;


// APU

public class APU {
    public Console console;
    public BlockingQueue<Float> channel;
    public double sampleRate;
    public Pulse pulse1 = new Pulse();
    public Pulse pulse2 = new Pulse();
    public Triangle triangle = new Triangle();
    public Noise noise = new Noise();
    public DMC dmc = new DMC();
    public long cycle;
    public byte framePeriod;
    public byte frameValue;
    public boolean frameIRQ;
    public FilterChain filterChain = new FilterChain();

    public APU() {
        // Default constructor
        channel = new LinkedBlockingQueue<Float>();
    }

    public static APU NewAPU(Console console) {
        APU apu = new APU();
        apu.console = console;
        apu.noise.shiftRegister = 1;
        apu.pulse1.channel = 1;
        apu.pulse2.channel = 2;
        apu.framePeriod = 4;
        apu.dmc.cpu = console.CPU;
        return apu;
    }

    public void Save(ObjectOutputStream encoder) throws IOException {
        encoder.writeObject(cycle);
        encoder.writeObject(framePeriod);
        encoder.writeObject(frameValue);
        encoder.writeObject(frameIRQ);
        pulse1.Save(encoder);
        pulse2.Save(encoder);
        triangle.Save(encoder);
        noise.Save(encoder);
        dmc.Save(encoder);
    }

    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        cycle = (Long) decoder.readObject();
        framePeriod = (Byte) decoder.readObject();
        frameValue = (Byte) decoder.readObject();
        frameIRQ = (Boolean) decoder.readObject();
        pulse1.Load(decoder);
        pulse2.Load(decoder);
        triangle.Load(decoder);
        noise.Load(decoder);
        dmc.Load(decoder);
    }

    public void Step() {
        long cycle1 = cycle;
        cycle++;
        long cycle2 = cycle;
        stepTimer();
        int f1 = (int) (cycle1 / NESConstants.frameCounterRate);
        int f2 = (int) (cycle2 / NESConstants.frameCounterRate);
        if (f1 != f2) {
            stepFrameCounter();
        }
        int s1 = (int) (cycle1 / sampleRate);
        int s2 = (int) (cycle2 / sampleRate);
        if (s1 != s2) {
            sendSample();
        }
    }

    public void sendSample() {
        float output = filterChain.Step(output());
        // Non-blocking channel send
        channel.offer(output);
    }

    public float output() {
        int p1 = pulse1.output() & 0xFF;
        int p2 = pulse2.output() & 0xFF;
        int t = triangle.output() & 0xFF;
        int n = noise.output() & 0xFF;
        int d = dmc.output() & 0xFF;
        float pulseOut = NESConstants.pulseTable[p1 + p2];
        float tndOut = NESConstants.tndTable[3 * t + 2 * n + d];
        return pulseOut + tndOut;
    }

    // mode 0:    mode 1:       function
    // ---------  -----------  -----------------------------
    //  - - - f    - - - - -    IRQ (if bit 6 is clear)
    //  - l - l    l - l - -    Length counter and sweep
    //  e e e e    e e e e -    Envelope and linear counter
    public void stepFrameCounter() {
        switch (framePeriod) {
            case 4:
                frameValue = (byte) ((frameValue + 1) % 4);
                switch (frameValue) {
                    case 0:
                    case 2:
                        stepEnvelope();
                        break;
                    case 1:
                        stepEnvelope();
                        stepSweep();
                        stepLength();
                        break;
                    case 3:
                        stepEnvelope();
                        stepSweep();
                        stepLength();
                        fireIRQ();
                        break;
                }
                break;
            case 5:
                frameValue = (byte) ((frameValue + 1) % 5);
                switch (frameValue) {
                    case 0:
                    case 2:
                        stepEnvelope();
                        break;
                    case 1:
                    case 3:
                        stepEnvelope();
                        stepSweep();
                        stepLength();
                        break;
                }
                break;
        }
    }

    public void stepTimer() {
        if (cycle % 2 == 0) {
            pulse1.stepTimer();
            pulse2.stepTimer();
            noise.stepTimer();
            dmc.stepTimer();
        }
        triangle.stepTimer();
    }

    public void stepEnvelope() {
        pulse1.stepEnvelope();
        pulse2.stepEnvelope();
        triangle.stepCounter();
        noise.stepEnvelope();
    }

    public void stepSweep() {
        pulse1.stepSweep();
        pulse2.stepSweep();
    }

    public void stepLength() {
        pulse1.stepLength();
        pulse2.stepLength();
        triangle.stepLength();
        noise.stepLength();
    }

    public void fireIRQ() {
        if (frameIRQ) {
            console.CPU.triggerIRQ();
        }
    }

    public byte readRegister(int address) {
        switch (address) {
            case 0x4015:
                return readStatus();
            // default:
            //  System.err.printf("unhandled apu register read at address: 0x%04X\n", address);
        }
        return 0;
    }

    public void writeRegister(int address, byte value) {
        switch (address) {
            case 0x4000:
                pulse1.writeControl(value);
                break;
            case 0x4001:
                pulse1.writeSweep(value);
                break;
            case 0x4002:
                pulse1.writeTimerLow(value);
                break;
            case 0x4003:
                pulse1.writeTimerHigh(value);
                break;
            case 0x4004:
                pulse2.writeControl(value);
                break;
            case 0x4005:
                pulse2.writeSweep(value);
                break;
            case 0x4006:
                pulse2.writeTimerLow(value);
                break;
            case 0x4007:
                pulse2.writeTimerHigh(value);
                break;
            case 0x4008:
                triangle.writeControl(value);
                break;
            case 0x4009:
                break;
            case 0x4010:
                dmc.writeControl(value);
                break;
            case 0x4011:
                dmc.writeValue(value);
                break;
            case 0x4012:
                dmc.writeAddress(value);
                break;
            case 0x4013:
                dmc.writeLength(value);
                break;
            case 0x400A:
                triangle.writeTimerLow(value);
                break;
            case 0x400B:
                triangle.writeTimerHigh(value);
                break;
            case 0x400C:
                noise.writeControl(value);
                break;
            case 0x400D:
                break;
            case 0x400E:
                noise.writePeriod(value);
                break;
            case 0x400F:
                noise.writeLength(value);
                break;
            case 0x4015:
                writeControl(value);
                break;
            case 0x4017:
                writeFrameCounter(value);
                break;
            // default:
            //  System.err.printf("unhandled apu register write at address: 0x%04X\n", address);
        }
    }

    public byte readStatus() {
        byte result = 0;
        if ((pulse1.lengthValue & 0xFF) > 0) {
            result |= 1;
        }
        if ((pulse2.lengthValue & 0xFF) > 0) {
            result |= 2;
        }
        if ((triangle.lengthValue & 0xFF) > 0) {
            result |= 4;
        }
        if ((noise.lengthValue & 0xFF) > 0) {
            result |= 8;
        }
        if ((dmc.currentLength & 0xFFFF) > 0) {
            result |= 16;
        }
        return result;
    }

    public void writeControl(byte value) {
        // Using bitwise operations; casting to int for clarity.
        pulse1.enabled = ((value & 1) == 1);
        pulse2.enabled = (((value >> 1) & 1) == 1);
        triangle.enabled = (((value >> 2) & 1) == 1);
        noise.enabled = (((value >> 3) & 1) == 1);
        dmc.enabled = (((value >> 4) & 1) == 1);
        if (!pulse1.enabled) {
            pulse1.lengthValue = 0;
        }
        if (!pulse2.enabled) {
            pulse2.lengthValue = 0;
        }
        if (!triangle.enabled) {
            triangle.lengthValue = 0;
        }
        if (!noise.enabled) {
            noise.lengthValue = 0;
        }
        if (!dmc.enabled) {
            dmc.currentLength = 0;
        } else {
            if (dmc.currentLength == 0) {
                dmc.restart();
            }
        }
    }

    public void writeFrameCounter(byte value) {
        framePeriod = (byte) (4 + ((value >> 7) & 1));
        frameIRQ = (((value >> 6) & 1) == 0);
        // frameValue = 0;
        if (framePeriod == 5) {
            stepEnvelope();
            stepSweep();
            stepLength();
        }
    }
}
