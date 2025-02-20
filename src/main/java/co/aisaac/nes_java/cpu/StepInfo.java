package co.aisaac.nes_java.cpu;

// Class to hold step information for instructions
class StepInfo {
    public int address;
    public int pc;
    public int mode;

    public StepInfo(int address, int pc, int mode) {
        this.address = address;
        this.pc = pc;
        this.mode = mode;
    }
}
