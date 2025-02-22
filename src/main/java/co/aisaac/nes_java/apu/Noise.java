package co.aisaac.nes_java.apu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class Noise {
    public boolean enabled;
    public boolean mode;
    public int shiftRegister;
    public boolean lengthEnabled;
    public int /*byte*/ lengthValue;
    public int timerPeriod;
    public int timerValue;
    public boolean envelopeEnabled;
    public boolean envelopeLoop;
    public boolean envelopeStart;
    public int /*byte*/ envelopePeriod;
    public int /*byte*/ envelopeValue;
    public int /*byte*/ envelopeVolume;
    public int /*byte*/ constantVolume;

    public void writeControl(int /*byte*/ value) {
        lengthEnabled = ((value >> 5) & 1) == 0;
        envelopeLoop = ((value >> 5) & 1) == 1;
        envelopeEnabled = ((value >> 4) & 1) == 0;
        envelopePeriod = (int /*byte*/) (value & 15);
        constantVolume = (int /*byte*/) (value & 15);
        envelopeStart = true;
    }

    public void writePeriod(int /*byte*/ value) {
        mode = (value & 0x80) == 0x80;
        timerPeriod = NESConstants.noiseTable[value & 0x0F];
    }

    public void writeLength(int /*byte*/ value) {
        lengthValue = NESConstants.lengthTable[(value & 0xFF) >> 3];
        envelopeStart = true;
    }

    public void stepTimer() {
        if (timerValue == 0) {
            timerValue = timerPeriod;
            int /*byte*/ shift;
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

    public int /*byte*/ output() {
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
