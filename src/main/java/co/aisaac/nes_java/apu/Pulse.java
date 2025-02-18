package co.aisaac.nes_java.apu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
