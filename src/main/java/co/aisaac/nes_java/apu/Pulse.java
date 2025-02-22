package co.aisaac.nes_java.apu;

class Pulse {
    public boolean enabled;
    public int channel;
    public boolean lengthEnabled;
    public int /*byte*/ lengthValue;
    public int timerPeriod;
    public int timerValue;
    public int /*byte*/ dutyMode;
    public int /*byte*/ dutyValue;
    public boolean sweepReload;
    public boolean sweepEnabled;
    public boolean sweepNegate;
    public int /*byte*/ sweepShift;
    public int /*byte*/ sweepPeriod;
    public int /*byte*/ sweepValue;
    public boolean envelopeEnabled;
    public boolean envelopeLoop;
    public boolean envelopeStart;
    public int /*byte*/ envelopePeriod;
    public int /*byte*/ envelopeValue;
    public int /*byte*/ envelopeVolume;
    public int /*byte*/ constantVolume;

    public void writeControl(int /*byte*/ value) {
        dutyMode = (int /*byte*/) ((value >> 6) & 3);
        lengthEnabled = ((value >> 5) & 1) == 0;
        envelopeLoop = ((value >> 5) & 1) == 1;
        envelopeEnabled = ((value >> 4) & 1) == 0;
        envelopePeriod = (int /*byte*/) (value & 15);
        constantVolume = (int /*byte*/) (value & 15);
        envelopeStart = true;
    }

    public void writeSweep(int /*byte*/ value) {
        sweepEnabled = ((value >> 7) & 1) == 1;
        sweepPeriod = (int /*byte*/) (((value >> 4) & 7) + 1);
        sweepNegate = ((value >> 3) & 1) == 1;
        sweepShift = (int /*byte*/) (value & 7);
        sweepReload = true;
    }

    public void writeTimerLow(int /*byte*/ value) {
        timerPeriod = (timerPeriod & 0xFF00) | (value & 0xFF);
    }

    public void writeTimerHigh(int /*byte*/ value) {
        lengthValue = NESConstants.lengthTable[(value & 0xFF) >> 3];
        timerPeriod = (timerPeriod & 0x00FF) | ((value & 7) << 8);
        envelopeStart = true;
        dutyValue = 0;
    }

    public void stepTimer() {
        if (timerValue == 0) {
            timerValue = timerPeriod;
            dutyValue = (int /*byte*/) ((dutyValue + 1) % 8);
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

    public int /*byte*/ output() {
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
