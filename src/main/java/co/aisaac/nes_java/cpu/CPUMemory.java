package co.aisaac.nes_java.cpu;

import co.aisaac.nes_java.Console;
import co.aisaac.nes_java.memory.Memory;

// CPU Memory Map
public class CPUMemory implements Memory {
    public Console console;

    public CPUMemory(Console console) {
        this.console = console;
    }

    @Override
    public int /*byte*/ read(int address) {
        address = address & 0xFFFF;
        if (address < 0x2000) {
            return this.console.ram[address % 0x0800];
        } else if (address < 0x4000) {
            return this.console.PPU.readRegister(0x2000 + address % 8);
        } else if (address == 0x4014) {
            return this.console.PPU.readRegister(address);
        } else if (address == 0x4015) {
            return this.console.APU.readRegister(address);
        } else if (address == 0x4016) {
            return this.console.controller1.read();
        } else if (address == 0x4017) {
            return this.console.controller2.read();
        } else if (address < 0x6000) {
            // TODO: I/O registers
        } else if (address >= 0x6000) {
            return this.console.mapper.read(address);
        } else {
            System.err.printf("unhandled cpu memory read at address: 0x%04X%n", address);
            System.exit(1);
        }
        return 0;
    }

    @Override
    public void write(int address, int /*byte*/ value) {
        value = value & 0xFF;
        if (address < 0x2000) {
            this.console.ram[address % 0x0800] = value;
        } else if (address < 0x4000) {
            this.console.PPU.writeRegister(0x2000 + address % 8, value);
        } else if (address < 0x4014) {
            this.console.APU.writeRegister(address, value);
        } else if (address == 0x4014) {
            this.console.PPU.writeRegister(address, value);
        } else if (address == 0x4015) {
            this.console.APU.writeRegister(address, value);
        } else if (address == 0x4016) {
            this.console.controller1.write(value);
            this.console.controller2.write(value);
        } else if (address == 0x4017) {
            this.console.APU.writeRegister(address, value);
        } else if (address < 0x6000) {
            // TODO: I/O registers
        } else if (address >= 0x6000) {
            this.console.mapper.write(address, value);
        } else {
            System.err.printf("unhandled cpu memory write at address: 0x%04X%n", address);
            System.exit(1);
        }
    }
}
