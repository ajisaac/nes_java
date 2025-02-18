package co.aisaac.nes_java.apu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
