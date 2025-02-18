package co.aisaac.nes_java;

import java.io.*;
import java.util.concurrent.*;

// Constant for the CPU frequency, assumed value for NES
// In the original Go code, CPUFrequency is external. Here we define it.
public class NESConstants {
    public static final double CPUFrequency = 1789773.0;
    public static final double frameCounterRate = CPUFrequency / 240.0;

    public static final byte[] lengthTable = new byte[]{
            10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14,
            12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30,
    };

    public static final byte[][] dutyTable = new byte[][]{
            {0, 1, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 1, 1},
    };

    public static final byte[] triangleTable = new byte[]{
            15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
    };

    public static final int[] noiseTable = new int[]{
            4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068,
    };

    public static final byte[] dmcTable = new byte[]{
            (byte)214, (byte)190, (byte)170, (byte)160, (byte)143, (byte)127, (byte)113, (byte)107, (byte)95, (byte)80, (byte)71, (byte)64, (byte)53, (byte)42, (byte)36, (byte)27,
    };

    public static final float[] pulseTable = new float[31];
    public static final float[] tndTable = new float[203];

    static {
        for (int i = 0; i < 31; i++) {
            pulseTable[i] = 95.52f / ((8128.0f / (i)) + 100);
        }
        for (int i = 0; i < 203; i++) {
            tndTable[i] = 163.67f / ((24329.0f / (i)) + 100);
        }
    }
}

// Stub for CPU
class CPU {
    public int stall = 0;

    // Simulate reading a byte from memory at the given address.
    public byte Read(int address) {
        // For complete implementation, this method should actually read from memory.
        // Here we return 0 as a placeholder.
        return 0;
    }

    // Trigger an IRQ.
    public void triggerIRQ() {
        // Actual IRQ handling code would be here.
    }
}

// Stub for Console, which contains a CPU.
class Console {
    public CPU CPU = new CPU();
}

// Stub for FilterChain, which applies filtering to samples.
class FilterChain {
    // The Step function processes the output sample.
    public float Step(float input) {
        // In a complete implementation, filtering would occur here.
        // We return the input sample unmodified.
        return input;
    }
}

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

// Pulse

class Pulse {
    public boolean enabled;
    public int channel;
    public boolean lengthEnabled;
    public byte lengthValue;
    public int timerPeriod;
    public int timerValue;
    public byte dutyMode;
    public byte dutyValue;
    public boolean sweepReload;
    public boolean sweepEnabled;
    public boolean sweepNegate;
    public byte sweepShift;
    public byte sweepPeriod;
    public byte sweepValue;
    public boolean envelopeEnabled;
    public boolean envelopeLoop;
    public boolean envelopeStart;
    public byte envelopePeriod;
    public byte envelopeValue;
    public byte envelopeVolume;
    public byte constantVolume;

    public void Save(ObjectOutputStream encoder) throws IOException {
        encoder.writeObject(enabled);
        encoder.writeObject(channel);
        encoder.writeObject(lengthEnabled);
        encoder.writeObject(lengthValue);
        encoder.writeObject(timerPeriod);
        encoder.writeObject(timerValue);
        encoder.writeObject(dutyMode);
        encoder.writeObject(dutyValue);
        encoder.writeObject(sweepReload);
        encoder.writeObject(sweepEnabled);
        encoder.writeObject(sweepNegate);
        encoder.writeObject(sweepShift);
        encoder.writeObject(sweepPeriod);
        encoder.writeObject(sweepValue);
        encoder.writeObject(envelopeEnabled);
        encoder.writeObject(envelopeLoop);
        encoder.writeObject(envelopeStart);
        encoder.writeObject(envelopePeriod);
        encoder.writeObject(envelopeValue);
        encoder.writeObject(envelopeVolume);
        encoder.writeObject(constantVolume);
    }

    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        enabled = (Boolean) decoder.readObject();
        channel = (Integer) decoder.readObject();
        lengthEnabled = (Boolean) decoder.readObject();
        lengthValue = (Byte) decoder.readObject();
        timerPeriod = (Integer) decoder.readObject();
        timerValue = (Integer) decoder.readObject();
        dutyMode = (Byte) decoder.readObject();
        dutyValue = (Byte) decoder.readObject();
        sweepReload = (Boolean) decoder.readObject();
        sweepEnabled = (Boolean) decoder.readObject();
        sweepNegate = (Boolean) decoder.readObject();
        sweepShift = (Byte) decoder.readObject();
        sweepPeriod = (Byte) decoder.readObject();
        sweepValue = (Byte) decoder.readObject();
        envelopeEnabled = (Boolean) decoder.readObject();
        envelopeLoop = (Boolean) decoder.readObject();
        envelopeStart = (Boolean) decoder.readObject();
        envelopePeriod = (Byte) decoder.readObject();
        envelopeValue = (Byte) decoder.readObject();
        envelopeVolume = (Byte) decoder.readObject();
        constantVolume = (Byte) decoder.readObject();
    }

    public void writeControl(byte value) {
        dutyMode = (byte) ((value >> 6) & 3);
        lengthEnabled = ((value >> 5) & 1) == 0;
        envelopeLoop = ((value >> 5) & 1) == 1;
        envelopeEnabled = ((value >> 4) & 1) == 0;
        envelopePeriod = (byte) (value & 15);
        constantVolume = (byte) (value & 15);
        envelopeStart = true;
    }

    public void writeSweep(byte value) {
        sweepEnabled = ((value >> 7) & 1) == 1;
        sweepPeriod = (byte) (((value >> 4) & 7) + 1);
        sweepNegate = ((value >> 3) & 1) == 1;
        sweepShift = (byte) (value & 7);
        sweepReload = true;
    }

    public void writeTimerLow(byte value) {
        timerPeriod = (timerPeriod & 0xFF00) | (value & 0xFF);
    }

    public void writeTimerHigh(byte value) {
        lengthValue = NESConstants.lengthTable[(value & 0xFF) >> 3];
        timerPeriod = (timerPeriod & 0x00FF) | ((value & 7) << 8);
        envelopeStart = true;
        dutyValue = 0;
    }

    public void stepTimer() {
        if (timerValue == 0) {
            timerValue = timerPeriod;
            dutyValue = (byte) ((dutyValue + 1) % 8);
        } else {
            timerValue--;
        }
    }

    public void stepEnvelope() {
        if (envelopeStart) {
            envelopeVolume = 15;
            envelopeValue = envelopePeriod;
            envelopeStart = false;
        } else if (envelopeValue > 0) {
            envelopeValue--;
        } else {
            if (envelopeVolume > 0) {
                envelopeVolume--;
            } else if (envelopeLoop) {
                envelopeVolume = 15;
            }
            envelopeValue = envelopePeriod;
        }
    }

    public void stepSweep() {
        if (sweepReload) {
            if (sweepEnabled && sweepValue == 0) {
                sweep();
            }
            sweepValue = sweepPeriod;
            sweepReload = false;
        } else if (sweepValue > 0) {
            sweepValue--;
        } else {
            if (sweepEnabled) {
                sweep();
            }
            sweepValue = sweepPeriod;
        }
    }

    public void stepLength() {
        if (lengthEnabled && (lengthValue & 0xFF) > 0) {
            lengthValue--;
        }
    }

    public void sweep() {
        int delta = timerPeriod >> sweepShift;
        if (sweepNegate) {
            timerPeriod -= delta;
            if (channel == 1) {
                timerPeriod--;
            }
        } else {
            timerPeriod += delta;
        }
    }

    public byte output() {
        if (!enabled) {
            return 0;
        }
        if ((lengthValue & 0xFF) == 0) {
            return 0;
        }
        if (NESConstants.dutyTable[dutyMode][dutyValue] == 0) {
            return 0;
        }
        if (timerPeriod < 8 || timerPeriod > 0x7FF) {
            return 0;
        }
        // if (!sweepNegate && timerPeriod+(timerPeriod>>sweepShift) > 0x7FF) {
        //     return 0;
        // }
        if (envelopeEnabled) {
            return envelopeVolume;
        } else {
            return constantVolume;
        }
    }
}

// Triangle

class Triangle {
    public boolean enabled;
    public boolean lengthEnabled;
    public byte lengthValue;
    public int timerPeriod;
    public int timerValue;
    public byte dutyValue;
    public byte counterPeriod;
    public byte counterValue;
    public boolean counterReload;

    public void Save(ObjectOutputStream encoder) throws IOException {
        encoder.writeObject(enabled);
        encoder.writeObject(lengthEnabled);
        encoder.writeObject(lengthValue);
        encoder.writeObject(timerPeriod);
        encoder.writeObject(timerValue);
        encoder.writeObject(dutyValue);
        encoder.writeObject(counterPeriod);
        encoder.writeObject(counterValue);
        encoder.writeObject(counterReload);
    }

    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        enabled = (Boolean) decoder.readObject();
        lengthEnabled = (Boolean) decoder.readObject();
        lengthValue = (Byte) decoder.readObject();
        timerPeriod = (Integer) decoder.readObject();
        timerValue = (Integer) decoder.readObject();
        dutyValue = (Byte) decoder.readObject();
        counterPeriod = (Byte) decoder.readObject();
        counterValue = (Byte) decoder.readObject();
        counterReload = (Boolean) decoder.readObject();
    }

    public void writeControl(byte value) {
        lengthEnabled = ((value >> 7) & 1) == 0;
        counterPeriod = (byte) (value & 0x7F);
    }

    public void writeTimerLow(byte value) {
        timerPeriod = (timerPeriod & 0xFF00) | (value & 0xFF);
    }

    public void writeTimerHigh(byte value) {
        lengthValue = NESConstants.lengthTable[(value & 0xFF) >> 3];
        timerPeriod = (timerPeriod & 0x00FF) | ((value & 7) << 8);
        timerValue = timerPeriod;
        counterReload = true;
    }

    public void stepTimer() {
        if (timerValue == 0) {
            timerValue = timerPeriod;
            if ((lengthValue & 0xFF) > 0 && (counterValue & 0xFF) > 0) {
                dutyValue = (byte) ((dutyValue + 1) % 32);
            }
        } else {
            timerValue--;
        }
    }

    public void stepLength() {
        if (lengthEnabled && (lengthValue & 0xFF) > 0) {
            lengthValue--;
        }
    }

    public void stepCounter() {
        if (counterReload) {
            counterValue = counterPeriod;
        } else if ((counterValue & 0xFF) > 0) {
            counterValue--;
        }
        if (lengthEnabled) {
            counterReload = false;
        }
    }

    public byte output() {
        if (!enabled) {
            return 0;
        }
        if (timerPeriod < 3) {
            return 0;
        }
        if ((lengthValue & 0xFF) == 0) {
            return 0;
        }
        if ((counterValue & 0xFF) == 0) {
            return 0;
        }
        return NESConstants.triangleTable[dutyValue];
    }
}

// Noise

class Noise {
    public boolean enabled;
    public boolean mode;
    public int shiftRegister;
    public boolean lengthEnabled;
    public byte lengthValue;
    public int timerPeriod;
    public int timerValue;
    public boolean envelopeEnabled;
    public boolean envelopeLoop;
    public boolean envelopeStart;
    public byte envelopePeriod;
    public byte envelopeValue;
    public byte envelopeVolume;
    public byte constantVolume;

    public void Save(ObjectOutputStream encoder) throws IOException {
        encoder.writeObject(enabled);
        encoder.writeObject(mode);
        encoder.writeObject(shiftRegister);
        encoder.writeObject(lengthEnabled);
        encoder.writeObject(lengthValue);
        encoder.writeObject(timerPeriod);
        encoder.writeObject(timerValue);
        encoder.writeObject(envelopeEnabled);
        encoder.writeObject(envelopeLoop);
        encoder.writeObject(envelopeStart);
        encoder.writeObject(envelopePeriod);
        encoder.writeObject(envelopeValue);
        encoder.writeObject(envelopeVolume);
        encoder.writeObject(constantVolume);
    }

    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        enabled = (Boolean) decoder.readObject();
        mode = (Boolean) decoder.readObject();
        shiftRegister = (Integer) decoder.readObject();
        lengthEnabled = (Boolean) decoder.readObject();
        lengthValue = (Byte) decoder.readObject();
        timerPeriod = (Integer) decoder.readObject();
        timerValue = (Integer) decoder.readObject();
        envelopeEnabled = (Boolean) decoder.readObject();
        envelopeLoop = (Boolean) decoder.readObject();
        envelopeStart = (Boolean) decoder.readObject();
        envelopePeriod = (Byte) decoder.readObject();
        envelopeValue = (Byte) decoder.readObject();
        envelopeVolume = (Byte) decoder.readObject();
        constantVolume = (Byte) decoder.readObject();
    }

    public void writeControl(byte value) {
        lengthEnabled = ((value >> 5) & 1) == 0;
        envelopeLoop = ((value >> 5) & 1) == 1;
        envelopeEnabled = ((value >> 4) & 1) == 0;
        envelopePeriod = (byte) (value & 15);
        constantVolume = (byte) (value & 15);
        envelopeStart = true;
    }

    public void writePeriod(byte value) {
        mode = (value & 0x80) == 0x80;
        timerPeriod = NESConstants.noiseTable[value & 0x0F];
    }

    public void writeLength(byte value) {
        lengthValue = NESConstants.lengthTable[(value & 0xFF) >> 3];
        envelopeStart = true;
    }

    public void stepTimer() {
        if (timerValue == 0) {
            timerValue = timerPeriod;
            byte shift;
            if (mode) {
                shift = 6;
            } else {
                shift = 1;
            }
            int b1 = shiftRegister & 1;
            int b2 = (shiftRegister >> shift) & 1;
            shiftRegister >>= 1;
            shiftRegister |= (b1 ^ b2) << 14;
        } else {
            timerValue--;
        }
    }

    public void stepEnvelope() {
        if (envelopeStart) {
            envelopeVolume = 15;
            envelopeValue = envelopePeriod;
            envelopeStart = false;
        } else if (envelopeValue > 0) {
            envelopeValue--;
        } else {
            if (envelopeVolume > 0) {
                envelopeVolume--;
            } else if (envelopeLoop) {
                envelopeVolume = 15;
            }
            envelopeValue = envelopePeriod;
        }
    }

    public void stepLength() {
        if (lengthEnabled && (lengthValue & 0xFF) > 0) {
            lengthValue--;
        }
    }

    public byte output() {
        if (!enabled) {
            return 0;
        }
        if ((lengthValue & 0xFF) == 0) {
            return 0;
        }
        if ((shiftRegister & 1) == 1) {
            return 0;
        }
        if (envelopeEnabled) {
            return envelopeVolume;
        } else {
            return constantVolume;
        }
    }
}

// DMC

class DMC {
    public CPU cpu;
    public boolean enabled;
    public byte value;
    public int sampleAddress;
    public int sampleLength;
    public int currentAddress;
    public int currentLength;
    public byte shiftRegister;
    public byte bitCount;
    public byte tickPeriod;
    public byte tickValue;
    public boolean loop;
    public boolean irq;

    public void Save(ObjectOutputStream encoder) throws IOException {
        encoder.writeObject(enabled);
        encoder.writeObject(value);
        encoder.writeObject(sampleAddress);
        encoder.writeObject(sampleLength);
        encoder.writeObject(currentAddress);
        encoder.writeObject(currentLength);
        encoder.writeObject(shiftRegister);
        encoder.writeObject(bitCount);
        encoder.writeObject(tickPeriod);
        encoder.writeObject(tickValue);
        encoder.writeObject(loop);
        encoder.writeObject(irq);
    }

    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        enabled = (Boolean) decoder.readObject();
        value = (Byte) decoder.readObject();
        sampleAddress = (Integer) decoder.readObject();
        sampleLength = (Integer) decoder.readObject();
        currentAddress = (Integer) decoder.readObject();
        currentLength = (Integer) decoder.readObject();
        shiftRegister = (Byte) decoder.readObject();
        bitCount = (Byte) decoder.readObject();
        tickPeriod = (Byte) decoder.readObject();
        tickValue = (Byte) decoder.readObject();
        loop = (Boolean) decoder.readObject();
        irq = (Boolean) decoder.readObject();
    }

    public void writeControl(byte value) {
        irq = (value & 0x80) == 0x80;
        loop = (value & 0x40) == 0x40;
        tickPeriod = NESConstants.dmcTable[value & 0x0F];
    }

    public void writeValue(byte value) {
        this.value = (byte) (value & 0x7F);
    }

    public void writeAddress(byte value) {
        // Sample address = %11AAAAAA.AA000000
        sampleAddress = 0xC000 | ((value & 0xFF) << 6);
    }

    public void writeLength(byte value) {
        // Sample length = %0000LLLL.LLLL0001
        sampleLength = ((value & 0xFF) << 4) | 1;
    }

    public void restart() {
        currentAddress = sampleAddress;
        currentLength = sampleLength;
    }

    public void stepTimer() {
        if (!enabled) {
            return;
        }
        stepReader();
        if (tickValue == 0) {
            tickValue = tickPeriod;
            stepShifter();
        } else {
            tickValue--;
        }
    }

    public void stepReader() {
        if (currentLength > 0 && bitCount == 0) {
            cpu.stall += 4;
            shiftRegister = cpu.Read(currentAddress);
            bitCount = 8;
            currentAddress++;
            if (currentAddress == 0) {
                currentAddress = 0x8000;
            }
            currentLength--;
            if (currentLength == 0 && loop) {
                restart();
            }
        }
    }

    public void stepShifter() {
        if (bitCount == 0) {
            return;
        }
        if ((shiftRegister & 1) == 1) {
            if ((value & 0xFF) <= 125) {
                value += 2;
            }
        } else {
            if ((value & 0xFF) >= 2) {
                value -= 2;
            }
        }
        shiftRegister = (byte) ((shiftRegister & 0xFF) >> 1);
        bitCount--;
    }

    public byte output() {
        return value;
    }
}

// End of code
