package co.aisaac.nes_java.mappers;

import co.aisaac.nes_java.Cartridge;
import co.aisaac.nes_java.Console;

// Mapper40 class implementing Mapper interface and translating the original Golang code
public class Mapper40 extends Mapper {
    Cartridge Cartridge;          // corresponds to *Cartridge
    Console console;              // corresponds to *Console
    int bank;                     // bank field
    int cycles;                   // cycles field

    // Constructor equivalent to NewMapper40 in Golang
    public Mapper40(Console console, Cartridge cartridge) {
        this.Cartridge = cartridge;
        this.console = console;
        this.bank = 0;
        this.cycles = 0;
    }

    // Static factory method equivalent to NewMapper40 in Golang
    public static Mapper NewMapper40(Console console, Cartridge cartridge) {
        return new Mapper40(console, cartridge);
    }

    // Step method simulating clock cycles and triggering IRQ when necessary
    public void Step() {
        if (this.cycles < 0) {
            return;
        }
        this.cycles++;
        if (this.cycles % (4096 * 3) == 0) {
            this.cycles = 0;
            this.console.CPU.triggerIRQ();
        }
    }

    // Read method to return data from CHR or PRG based on address ranges
    public int /*byte*/ read(int address) {
        if (address < 0x2000) {
            return this.Cartridge.CHR[address];
        } else if (address >= 0x6000 && address < 0x8000) {
            return this.Cartridge.PRG[address - 0x6000 + 0x2000 * 6];
        } else if (address >= 0x8000 && address < 0xa000) {
            return this.Cartridge.PRG[address - 0x8000 + 0x2000 * 4];
        } else if (address >= 0xa000 && address < 0xc000) {
            return this.Cartridge.PRG[address - 0xa000 + 0x2000 * 5];
        } else if (address >= 0xc000 && address < 0xe000) {
            return this.Cartridge.PRG[address - 0xc000 + 0x2000 * this.bank];
        } else if (address >= 0xe000) {
            return this.Cartridge.PRG[address - 0xe000 + 0x2000 * 7];
        } else {
            System.err.printf("unhandled mapper40 read at address: 0x%04X\n", address);
            System.exit(1);
        }
        return 0;
    }

    // Write method to write data to CHR or modify cycles/bank based on address ranges
    public void write(int address, int /*byte*/ value) {
        if (address < 0x2000) {
            this.Cartridge.CHR[address] = value;
        } else if (address >= 0x8000 && address < 0xa000) {
            this.cycles = -1;
        } else if (address >= 0xa000 && address < 0xc000) {
            this.cycles = 0;
        } else if (address >= 0xe000) {
            this.bank = value;
        } else {
            // log.Fatalf("unhandled mapper40 write at address: 0x%04X", address)
            System.out.printf("unhandled mapper40 write at address: 0x%04X\n", address);
        }
    }
}
