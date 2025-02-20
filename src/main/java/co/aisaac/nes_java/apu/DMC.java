package co.aisaac.nes_java.apu;

import co.aisaac.nes_java.cpu.CPU;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
            // todo
            shiftRegister = (byte) cpu.Read(currentAddress);
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
