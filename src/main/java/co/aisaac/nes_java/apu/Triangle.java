package co.aisaac.nes_java.apu;

class Triangle {
    public boolean enabled;
    public boolean lengthEnabled;
    public int /*byte*/ lengthValue;
    public int timerPeriod;
    public int timerValue;
    public int /*byte*/ dutyValue;
    public int /*byte*/ counterPeriod;
    public int /*byte*/ counterValue;
    public boolean counterReload;

    public void writeControl(int /*byte*/ value) {
        lengthEnabled = ((value >> 7) & 1) == 0;
        counterPeriod = (int /*byte*/) (value & 0x7F);
    }

    public void writeTimerLow(int /*byte*/ value) {
        timerPeriod = (timerPeriod & 0xFF00) | (value & 0xFF);
    }

    public void writeTimerHigh(int /*byte*/ value) {
        lengthValue = NESConstants.lengthTable[(value & 0xFF) >> 3];
        timerPeriod = (timerPeriod & 0x00FF) | ((value & 7) << 8);
        timerValue = timerPeriod;
        counterReload = true;
    }

    public void stepTimer() {
        if (timerValue == 0) {
            timerValue = timerPeriod;
            if ((lengthValue & 0xFF) > 0 && (counterValue & 0xFF) > 0) {
                dutyValue = (int /*byte*/) ((dutyValue + 1) % 32);
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

    public int /*byte*/ output() {
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
