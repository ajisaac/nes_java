package co.aisaac.nes_java.apu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
