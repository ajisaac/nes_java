package co.aisaac.nes_java.apu;

import co.aisaac.nes_java.cpu.CPU;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class DMC {
    public CPU cpu;
    public boolean enabled;
    public int /*byte*/ value;
    public int sampleAddress;
    public int sampleLength;
    public int currentAddress;
    public int currentLength;
    public int /*byte*/ shiftRegister;
    public int /*byte*/ bitCount;
    public int /*byte*/ tickPeriod;
    public int /*byte*/ tickValue;
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

    public void writeControl(int /*byte*/ value) {
        irq = (value & 0x80) == 0x80;
        loop = (value & 0x40) == 0x40;
        tickPeriod = NESConstants.dmcTable[value & 0x0F];
    }

    public void writeValue(int /*byte*/ value) {
        this.value = (int /*byte*/) (value & 0x7F);
    }

    public void writeAddress(int /*byte*/ value) {
        // Sample address = %11AAAAAA.AA000000
        sampleAddress = 0xC000 | ((value & 0xFF) << 6);
    }

    public void writeLength(int /*byte*/ value) {
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
            // todo
            shiftRegister = (int /*byte*/) cpu.Read(currentAddress);
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
        shiftRegister = (int /*byte*/) ((shiftRegister & 0xFF) >> 1);
        bitCount--;
    }

    public int /*byte*/ output() {
        return value;
    }
}
