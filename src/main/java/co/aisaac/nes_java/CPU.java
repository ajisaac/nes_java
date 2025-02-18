package co.aisaac.nes_java;

import co.aisaac.nes_java.memory.Memory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CPU {
    // Functional interface for CPU instruction functions
    @FunctionalInterface
    interface Instruction {
        void execute(stepInfo info);
    }

    // Class to hold step information for instructions
    class stepInfo {
        public int address;
        public int pc;
        public int mode;

        public stepInfo(int address, int pc, int mode) {
            this.address = address;
            this.pc = pc;
            this.mode = mode;
        }
    }

    // memory interface
    public Memory memory;
    public long Cycles; // number of cycles
    public int PC;      // program counter (16-bit)
    public int SP;      // stack pointer (8-bit)
    public int A;       // accumulator (8-bit)
    public int X;       // x register (8-bit)
    public int Y;       // y register (8-bit)
    public int C;       // carry flag (8-bit)
    public int Z;       // zero flag (8-bit)
    public int I;       // interrupt disable flag (8-bit)
    public int D;       // decimal mode flag (8-bit)
    public int B;       // break command flag (8-bit)
    public int U;       // unused flag (8-bit)
    public int V;       // overflow flag (8-bit)
    public int N;       // negative flag (8-bit)
    public int interrupt; // interrupt type to perform (8-bit)
    public int stall;     // number of cycles to stall
    public Instruction[] table = new Instruction[256];

    // CPU every instruction constant
    public static final int CPUFrequency = 1789773;

    // interrupt types
    public static final int interruptNone = 1;
    public static final int interruptNMI = 2;
    public static final int interruptIRQ = 3;

    // addressing modes
    public static final int modeAbsolute = 1;
    public static final int modeAbsoluteX = 2;
    public static final int modeAbsoluteY = 3;
    public static final int modeAccumulator = 4;
    public static final int modeImmediate = 5;
    public static final int modeImplied = 6;
    public static final int modeIndexedIndirect = 7;
    public static final int modeIndirect = 8;
    public static final int modeIndirectIndexed = 9;
    public static final int modeRelative = 10;
    public static final int modeZeroPage = 11;
    public static final int modeZeroPageX = 12;
    public static final int modeZeroPageY = 13;

    // instructionModes indicates the addressing mode for each instruction
    public static final byte[] instructionModes = {
            6, 7, 6, 7, 11, 11, 11, 11, 6, 5, 4, 5, 1, 1, 1, 1,
            10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
            1, 7, 6, 7, 11, 11, 11, 11, 6, 5, 4, 5, 1, 1, 1, 1,
            10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
            6, 7, 6, 7, 11, 11, 11, 11, 6, 5, 4, 5, 1, 1, 1, 1,
            10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
            6, 7, 6, 7, 11, 11, 11, 11, 6, 5, 4, 5, 8, 1, 1, 1,
            10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
            5, 7, 5, 7, 11, 11, 11, 11, 6, 5, 6, 5, 1, 1, 1, 1,
            10, 9, 6, 9, 12, 12, 13, 13, 6, 3, 6, 3, 2, 2, 3, 3,
            5, 7, 5, 7, 11, 11, 11, 11, 6, 5, 6, 5, 1, 1, 1, 1,
            10, 9, 6, 9, 12, 12, 13, 13, 6, 3, 6, 3, 2, 2, 3, 3,
            5, 7, 5, 7, 11, 11, 11, 11, 6, 5, 6, 5, 1, 1, 1, 1,
            10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
            5, 7, 5, 7, 11, 11, 11, 11, 6, 5, 6, 5, 1, 1, 1, 1,
            10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
    };

    // instructionSizes indicates the size of each instruction in bytes
    public static final byte[] instructionSizes = {
            2, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
            3, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
            1, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
            1, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 0, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 0, 3, 0, 0,
            2, 2, 2, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
            2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
    };

    // instructionCycles indicates the number of cycles used by each instruction,
    // not including conditional cycles
    public static final byte[] instructionCycles = {
            7, 6, 2, 8, 3, 3, 5, 5, 3, 2, 2, 2, 4, 4, 6, 6,
            2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
            6, 6, 2, 8, 3, 3, 5, 5, 4, 2, 2, 2, 4, 4, 6, 6,
            2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
            6, 6, 2, 8, 3, 3, 5, 5, 3, 2, 2, 2, 3, 4, 6, 6,
            2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
            6, 6, 2, 8, 3, 3, 5, 5, 4, 2, 2, 2, 5, 4, 6, 6,
            2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
            2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4,
            2, 6, 2, 6, 4, 4, 4, 4, 2, 5, 2, 5, 5, 5, 5, 5,
            2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4,
            2, 5, 2, 5, 4, 4, 4, 4, 2, 4, 2, 4, 4, 4, 4, 4,
            2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6,
            2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
            2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6,
            2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
    };

    // instructionPageCycles indicates the number of cycles used by each
    // instruction when a page is crossed
    public static final byte[] instructionPageCycles = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
    };

    // instructionNames indicates the name of each instruction
    public static final String[] instructionNames = {
            "BRK", "ORA", "KIL", "SLO", "NOP", "ORA", "ASL", "SLO",
            "PHP", "ORA", "ASL", "ANC", "NOP", "ORA", "ASL", "SLO",
            "BPL", "ORA", "KIL", "SLO", "NOP", "ORA", "ASL", "SLO",
            "CLC", "ORA", "NOP", "SLO", "NOP", "ORA", "ASL", "SLO",
            "JSR", "AND", "KIL", "RLA", "BIT", "AND", "ROL", "RLA",
            "PLP", "AND", "ROL", "ANC", "BIT", "AND", "ROL", "RLA",
            "BMI", "AND", "KIL", "RLA", "NOP", "AND", "ROL", "RLA",
            "SEC", "AND", "NOP", "RLA", "NOP", "AND", "ROL", "RLA",
            "RTI", "EOR", "KIL", "SRE", "NOP", "EOR", "LSR", "SRE",
            "PHA", "EOR", "LSR", "ALR", "JMP", "EOR", "LSR", "SRE",
            "BVC", "EOR", "KIL", "SRE", "NOP", "EOR", "LSR", "SRE",
            "CLI", "EOR", "NOP", "SRE", "NOP", "EOR", "LSR", "SRE",
            "RTS", "ADC", "KIL", "RRA", "NOP", "ADC", "ROR", "RRA",
            "PLA", "ADC", "ROR", "ARR", "JMP", "ADC", "ROR", "RRA",
            "BVS", "ADC", "KIL", "RRA", "NOP", "ADC", "ROR", "RRA",
            "SEI", "ADC", "NOP", "RRA", "NOP", "ADC", "ROR", "RRA",
            "NOP", "STA", "NOP", "SAX", "STY", "STA", "STX", "SAX",
            "DEY", "NOP", "TXA", "XAA", "STY", "STA", "STX", "SAX",
            "BCC", "STA", "KIL", "AHX", "STY", "STA", "STX", "SAX",
            "TYA", "STA", "TXS", "TAS", "SHY", "STA", "SHX", "AHX",
            "LDY", "LDA", "LDX", "LAX", "LDY", "LDA", "LDX", "LAX",
            "TAY", "LDA", "TAX", "LAX", "LDY", "LDA", "LDX", "LAX",
            "BCS", "LDA", "KIL", "LAX", "LDY", "LDA", "LDX", "LAX",
            "CLV", "LDA", "TSX", "LAS", "LDY", "LDA", "LDX", "LAX",
            "CPY", "CMP", "NOP", "DCP", "CPY", "CMP", "DEC", "DCP",
            "INY", "CMP", "DEX", "AXS", "CPY", "CMP", "DEC", "DCP",
            "BNE", "CMP", "KIL", "DCP", "NOP", "CMP", "DEC", "DCP",
            "CLD", "CMP", "NOP", "DCP", "NOP", "CMP", "DEC", "DCP",
            "CPX", "SBC", "NOP", "ISC", "CPX", "SBC", "INC", "ISC",
            "INX", "SBC", "NOP", "SBC", "CPX", "SBC", "INC", "ISC",
            "BEQ", "SBC", "KIL", "ISC", "NOP", "SBC", "INC", "ISC",
            "SED", "SBC", "NOP", "ISC", "NOP", "SBC", "INC", "ISC",
    };

    // NewCPU builds and returns a new CPU instance
    public static CPU NewCPU(Console console) {
        CPU cpu = new CPU();
        cpu.memory = new CPUMemory(console);
        cpu.createTable();
        cpu.Reset();
        return cpu;
    }

    // createTable builds a function table for each instruction
    public void createTable() {
        table = new Instruction[]{
                this::brk, this::ora, this::kil, this::slo, this::nop, this::ora, this::asl, this::slo,
                this::php, this::ora, this::asl, this::anc, this::nop, this::ora, this::asl, this::slo,
                this::bpl, this::ora, this::kil, this::slo, this::nop, this::ora, this::asl, this::slo,
                this::clc, this::ora, this::nop, this::slo, this::nop, this::ora, this::asl, this::slo,
                this::jsr, this::and, this::kil, this::rla, this::bit, this::and, this::rol, this::rla,
                this::plp, this::and, this::rol, this::anc, this::bit, this::and, this::rol, this::rla,
                this::bmi, this::and, this::kil, this::rla, this::nop, this::and, this::rol, this::rla,
                this::sec, this::and, this::nop, this::rla, this::nop, this::and, this::rol, this::rla,
                this::rti, this::eor, this::kil, this::sre, this::nop, this::eor, this::lsr, this::sre,
                this::pha, this::eor, this::lsr, this::alr, this::jmp, this::eor, this::lsr, this::sre,
                this::bvc, this::eor, this::kil, this::sre, this::nop, this::eor, this::lsr, this::sre,
                this::cli, this::eor, this::nop, this::sre, this::nop, this::eor, this::lsr, this::sre,
                this::rts, this::adc, this::kil, this::rra, this::nop, this::adc, this::ror, this::rra,
                this::pla, this::adc, this::ror, this::arr, this::jmp, this::adc, this::ror, this::rra,
                this::bvs, this::adc, this::kil, this::rra, this::nop, this::adc, this::ror, this::rra,
                this::sei, this::adc, this::nop, this::rra, this::nop, this::adc, this::ror, this::rra,
                this::nop, this::sta, this::nop, this::sax, this::sty, this::sta, this::stx, this::sax,
                this::dey, this::nop, this::txa, this::xaa, this::sty, this::sta, this::stx, this::sax,
                this::bcc, this::sta, this::kil, this::ahx, this::sty, this::sta, this::stx, this::sax,
                this::tya, this::sta, this::txs, this::tas, this::shy, this::sta, this::shx, this::ahx,
                this::ldy, this::lda, this::ldx, this::lax, this::ldy, this::lda, this::ldx, this::lax,
                this::tay, this::lda, this::tax, this::lax, this::ldy, this::lda, this::ldx, this::lax,
                this::bcs, this::lda, this::kil, this::lax, this::ldy, this::lda, this::ldx, this::lax,
                this::clv, this::lda, this::tsx, this::las, this::ldy, this::lda, this::ldx, this::lax,
                this::cpy, this::cmp, this::nop, this::dcp, this::cpy, this::cmp, this::dec, this::dcp,
                this::iny, this::cmp, this::dex, this::axs, this::cpy, this::cmp, this::dec, this::dcp,
                this::bne, this::cmp, this::kil, this::dcp, this::nop, this::cmp, this::dec, this::dcp,
                this::cld, this::cmp, this::nop, this::dcp, this::nop, this::cmp, this::dec, this::dcp,
                this::cpx, this::sbc, this::nop, this::isc, this::cpx, this::sbc, this::inc, this::isc,
                this::inx, this::sbc, this::nop, this::sbc, this::cpx, this::sbc, this::inc, this::isc,
                this::beq, this::sbc, this::kil, this::isc, this::nop, this::sbc, this::inc, this::isc,
                this::sed, this::sbc, this::nop, this::isc, this::nop, this::sbc, this::inc, this::isc,
        };
    }

    // Save writes the CPU state to the given DataOutputStream
    public void Save(DataOutputStream encoder) throws IOException {
        encoder.writeLong(Cycles);
        encoder.writeShort(PC);
        encoder.writeByte(SP);
        encoder.writeByte(A);
        encoder.writeByte(X);
        encoder.writeByte(Y);
        encoder.writeByte(C);
        encoder.writeByte(Z);
        encoder.writeByte(I);
        encoder.writeByte(D);
        encoder.writeByte(B);
        encoder.writeByte(U);
        encoder.writeByte(V);
        encoder.writeByte(N);
        encoder.writeByte(interrupt);
        encoder.writeInt(stall);
    }

    // Load reads the CPU state from the given DataInputStream
    public void Load(DataInputStream decoder) throws IOException {
        Cycles = decoder.readLong();
        PC = decoder.readUnsignedShort();
        SP = decoder.readUnsignedByte();
        A = decoder.readUnsignedByte();
        X = decoder.readUnsignedByte();
        Y = decoder.readUnsignedByte();
        C = decoder.readUnsignedByte();
        Z = decoder.readUnsignedByte();
        I = decoder.readUnsignedByte();
        D = decoder.readUnsignedByte();
        B = decoder.readUnsignedByte();
        U = decoder.readUnsignedByte();
        V = decoder.readUnsignedByte();
        N = decoder.readUnsignedByte();
        interrupt = decoder.readUnsignedByte();
        stall = decoder.readInt();
    }

    // Reset resets the CPU to its initial powerup state
    public void Reset() {
        PC = Read16(0xFFFC);
        SP = 0xFD;
        SetFlags(0x24);
    }

    // PrintInstruction prints the current CPU state
    public void PrintInstruction() {
        int opcode = Read(PC);
        int bytes = instructionSizes[opcode];
        String name = instructionNames[opcode];
        String w0 = String.format("%02X", Read(PC + 0));
        String w1 = String.format("%02X", Read(PC + 1));
        String w2 = String.format("%02X", Read(PC + 2));
        if (bytes < 2) {
            w1 = "  ";
        }
        if (bytes < 3) {
            w2 = "  ";
        }
        System.out.printf(
                "%4X  %s %s %s  %s %28s" +
                        "A:%02X X:%02X Y:%02X P:%02X SP:%02X CYC:%3d\n",
                PC, w0, w1, w2, name, "",
                A, X, Y, Flags(), SP, (int) ((Cycles * 3) % 341)
        );
    }

    // pagesDiffer returns true if the two addresses reference different pages
    public static boolean pagesDiffer(int a, int b) {
        return (a & 0xFF00) != (b & 0xFF00);
    }

    // addBranchCycles adds a cycle for taking a branch and adds another cycle
    // if the branch jumps to a new page
    public void addBranchCycles(stepInfo info) {
        Cycles++;
        if (pagesDiffer(info.pc, info.address)) {
            Cycles++;
        }
    }

    public void compare(int a, int b) {
        setZN(a - b);
        if (a >= b) {
            C = 1;
        } else {
            C = 0;
        }
    }

    // Read16 reads two bytes using Read to return a double-word value
    public int Read16(int address) {
        int lo = Read(address);
        int hi = Read(address + 1);
        return (hi << 8) | lo;
    }

    // read16bug emulates a 6502 bug that caused the low byte to wrap without
    // incrementing the high byte
    public int read16bug(int address) {
        int a = address;
        int b = (a & 0xFF00) | (((a & 0xFF) + 1) & 0xFF);
        int lo = Read(a);
        int hi = Read(b);
        return (hi << 8) | lo;
    }

    // push pushes a byte onto the stack
    public void push(int value) {
        Write(0x100 | SP, value);
        SP = (SP - 1) & 0xFF;
    }

    // pull pops a byte from the stack
    public int pull() {
        SP = (SP + 1) & 0xFF;
        return Read(0x100 | SP);
    }

    // push16 pushes two bytes onto the stack
    public void push16(int value) {
        int hi = (value >> 8) & 0xFF;
        int lo = value & 0xFF;
        push(hi);
        push(lo);
    }

    // pull16 pops two bytes from the stack
    public int pull16() {
        int lo = pull();
        int hi = pull();
        return (hi << 8) | lo;
    }

    // Flags returns the processor status flags
    public int Flags() {
        int flags = 0;
        flags |= (C & 1) << 0;
        flags |= (Z & 1) << 1;
        flags |= (I & 1) << 2;
        flags |= (D & 1) << 3;
        flags |= (B & 1) << 4;
        flags |= (U & 1) << 5;
        flags |= (V & 1) << 6;
        flags |= (N & 1) << 7;
        return flags;
    }

    // SetFlags sets the processor status flags
    public void SetFlags(int flags) {
        C = (flags >> 0) & 1;
        Z = (flags >> 1) & 1;
        I = (flags >> 2) & 1;
        D = (flags >> 3) & 1;
        B = (flags >> 4) & 1;
        U = (flags >> 5) & 1;
        V = (flags >> 6) & 1;
        N = (flags >> 7) & 1;
    }

    // setZ sets the zero flag if the argument is zero
    public void setZ(int value) {
        if ((value & 0xFF) == 0) {
            Z = 1;
        } else {
            Z = 0;
        }
    }

    // setN sets the negative flag if the argument is negative (high bit is set)
    public void setN(int value) {
        if ((value & 0x80) != 0) {
            N = 1;
        } else {
            N = 0;
        }
    }

    // setZN sets the zero flag and the negative flag
    public void setZN(int value) {
        setZ(value);
        setN(value);
    }

    // triggerNMI causes a non-maskable interrupt to occur on the next cycle
    public void triggerNMI() {
        interrupt = interruptNMI;
    }

    // triggerIRQ causes an IRQ interrupt to occur on the next cycle
    public void triggerIRQ() {
        if (I == 0) {
            interrupt = interruptIRQ;
        }
    }

    // Read delegates to the memory read method
    public int Read(int address) {
        return memory.read(address);
    }

    // Write delegates to the memory write method
    public void Write(int address, int value) {
        memory.write(address, value);
    }

    // Step executes a single CPU instruction
    public int Step() {
        if (stall > 0) {
            stall--;
            return 1;
        }

        long cyclesBefore = Cycles;

        switch (interrupt) {
            case interruptNMI:
                nmi();
                break;
            case interruptIRQ:
                irq();
                break;
            default:
                break;
        }
        interrupt = interruptNone;

        int opcode = Read(PC);
        int mode = instructionModes[opcode] & 0xFF;

        int address = 0;
        boolean pageCrossed = false;
        switch (mode) {
            case modeAbsolute:
                address = Read16(PC + 1);
                break;
            case modeAbsoluteX: {
                int base = Read16(PC + 1);
                address = base + X;
                pageCrossed = pagesDiffer(address - X, address);
            }
            break;
            case modeAbsoluteY: {
                int base = Read16(PC + 1);
                address = base + Y;
                pageCrossed = pagesDiffer(address - Y, address);
            }
            break;
            case modeAccumulator:
                address = 0;
                break;
            case modeImmediate:
                address = PC + 1;
                break;
            case modeImplied:
                address = 0;
                break;
            case modeIndexedIndirect: {
                int temp = Read(PC + 1) + X;
                address = read16bug(temp & 0xFF);
            }
            break;
            case modeIndirect:
                address = read16bug(Read16(PC + 1));
                break;
            case modeIndirectIndexed: {
                int temp = Read(PC + 1) & 0xFF;
                address = read16bug(temp) + Y;
                pageCrossed = pagesDiffer(address - Y, address);
            }
            break;
            case modeRelative: {
                int offset = Read(PC + 1);
                if (offset < 0x80) {
                    address = PC + 2 + offset;
                } else {
                    address = PC + 2 + offset - 0x100;
                }
            }
            break;
            case modeZeroPage:
                address = Read(PC + 1);
                break;
            case modeZeroPageX:
                address = (Read(PC + 1) + X) & 0xFF;
                break;
            case modeZeroPageY:
                address = (Read(PC + 1) + Y) & 0xFF;
                break;
        }

        PC += instructionSizes[opcode] & 0xFF;
        Cycles += instructionCycles[opcode] & 0xFF;
        if (pageCrossed) {
            Cycles += instructionPageCycles[opcode] & 0xFF;
        }
        stepInfo info = new stepInfo(address, PC, mode);
        table[opcode].execute(info);

        return (int) (Cycles - cyclesBefore);
    }

    // NMI - Non-Maskable Interrupt
    public void nmi() {
        push16(PC);
        php(null);
        PC = Read16(0xFFFA);
        I = 1;
        Cycles += 7;
    }

    // IRQ - IRQ Interrupt
    public void irq() {
        push16(PC);
        php(null);
        PC = Read16(0xFFFE);
        I = 1;
        Cycles += 7;
    }

    // ADC - Add with Carry
    public void adc(stepInfo info) {
        int a = A;
        int b = Read(info.address);
        int c = C;
        A = (a + b + c) & 0xFF;
        setZN(A);
        if (a + b + c > 0xFF) {
            C = 1;
        } else {
            C = 0;
        }
        if (((a ^ b) & 0x80) == 0 && ((a ^ A) & 0x80) != 0) {
            V = 1;
        } else {
            V = 0;
        }
    }

    // AND - Logical AND
    public void and(stepInfo info) {
        A = A & Read(info.address);
        setZN(A);
    }

    // ASL - Arithmetic Shift Left
    public void asl(stepInfo info) {
        if (info.mode == modeAccumulator) {
            C = (A >> 7) & 1;
            A = (A << 1) & 0xFF;
            setZN(A);
        } else {
            int value = Read(info.address);
            C = (value >> 7) & 1;
            value = (value << 1) & 0xFF;
            Write(info.address, value);
            setZN(value);
        }
    }

    // BCC - Branch if Carry Clear
    public void bcc(stepInfo info) {
        if (C == 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BCS - Branch if Carry Set
    public void bcs(stepInfo info) {
        if (C != 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BEQ - Branch if Equal
    public void beq(stepInfo info) {
        if (Z != 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BIT - Bit Test
    public void bit(stepInfo info) {
        int value = Read(info.address);
        V = (value >> 6) & 1;
        setZ(value & A);
        setN(value);
    }

    // BMI - Branch if Minus
    public void bmi(stepInfo info) {
        if (N != 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BNE - Branch if Not Equal
    public void bne(stepInfo info) {
        if (Z == 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BPL - Branch if Positive
    public void bpl(stepInfo info) {
        if (N == 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BRK - Force Interrupt
    public void brk(stepInfo info) {
        push16(PC);
        php(info);
        sei(info);
        PC = Read16(0xFFFE);
    }

    // BVC - Branch if Overflow Clear
    public void bvc(stepInfo info) {
        if (V == 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BVS - Branch if Overflow Set
    public void bvs(stepInfo info) {
        if (V != 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // CLC - Clear Carry Flag
    public void clc(stepInfo info) {
        C = 0;
    }

    // CLD - Clear Decimal Mode
    public void cld(stepInfo info) {
        D = 0;
    }

    // CLI - Clear Interrupt Disable
    public void cli(stepInfo info) {
        I = 0;
    }

    // CLV - Clear Overflow Flag
    public void clv(stepInfo info) {
        V = 0;
    }

    // CMP - Compare
    public void cmp(stepInfo info) {
        int value = Read(info.address);
        compare(A, value);
    }

    // CPX - Compare X Register
    public void cpx(stepInfo info) {
        int value = Read(info.address);
        compare(X, value);
    }

    // CPY - Compare Y Register
    public void cpy(stepInfo info) {
        int value = Read(info.address);
        compare(Y, value);
    }

    // DEC - Decrement Memory
    public void dec(stepInfo info) {
        int value = (Read(info.address) - 1) & 0xFF;
        Write(info.address, value);
        setZN(value);
    }

    // DEX - Decrement X Register
    public void dex(stepInfo info) {
        X = (X - 1) & 0xFF;
        setZN(X);
    }

    // DEY - Decrement Y Register
    public void dey(stepInfo info) {
        Y = (Y - 1) & 0xFF;
        setZN(Y);
    }

    // EOR - Exclusive OR
    public void eor(stepInfo info) {
        A = A ^ Read(info.address);
        setZN(A);
    }

    // INC - Increment Memory
    public void inc(stepInfo info) {
        int value = (Read(info.address) + 1) & 0xFF;
        Write(info.address, value);
        setZN(value);
    }

    // INX - Increment X Register
    public void inx(stepInfo info) {
        X = (X + 1) & 0xFF;
        setZN(X);
    }

    // INY - Increment Y Register
    public void iny(stepInfo info) {
        Y = (Y + 1) & 0xFF;
        setZN(Y);
    }

    // JMP - Jump
    public void jmp(stepInfo info) {
        PC = info.address;
    }

    // JSR - Jump to Subroutine
    public void jsr(stepInfo info) {
        push16(PC - 1);
        PC = info.address;
    }

    // LDA - Load Accumulator
    public void lda(stepInfo info) {
        A = Read(info.address);
        setZN(A);
    }

    // LDX - Load X Register
    public void ldx(stepInfo info) {
        X = Read(info.address);
        setZN(X);
    }

    // LDY - Load Y Register
    public void ldy(stepInfo info) {
        Y = Read(info.address);
        setZN(Y);
    }

    // LSR - Logical Shift Right
    public void lsr(stepInfo info) {
        if (info.mode == modeAccumulator) {
            C = A & 1;
            A = (A >> 1) & 0xFF;
            setZN(A);
        } else {
            int value = Read(info.address);
            C = value & 1;
            value = (value >> 1) & 0xFF;
            Write(info.address, value);
            setZN(value);
        }
    }

    // NOP - No Operation
    public void nop(stepInfo info) {
    }

    // ORA - Logical Inclusive OR
    public void ora(stepInfo info) {
        A = A | Read(info.address);
        setZN(A);
    }

    // PHA - Push Accumulator
    public void pha(stepInfo info) {
        push(A);
    }

    // PHP - Push Processor Status
    public void php(stepInfo info) {
        push(Flags() | 0x10);
    }

    // PLA - Pull Accumulator
    public void pla(stepInfo info) {
        A = pull();
        setZN(A);
    }

    // PLP - Pull Processor Status
    public void plp(stepInfo info) {
        SetFlags((pull() & 0xEF) | 0x20);
    }

    // ROL - Rotate Left
    public void rol(stepInfo info) {
        if (info.mode == modeAccumulator) {
            int c = C;
            C = (A >> 7) & 1;
            A = ((A << 1) & 0xFF) | c;
            setZN(A);
        } else {
            int c = C;
            int value = Read(info.address);
            C = (value >> 7) & 1;
            value = ((value << 1) & 0xFF) | c;
            Write(info.address, value);
            setZN(value);
        }
    }

    // ROR - Rotate Right
    public void ror(stepInfo info) {
        if (info.mode == modeAccumulator) {
            int c = C;
            C = A & 1;
            A = (A >> 1) | (c << 7);
            A = A & 0xFF;
            setZN(A);
        } else {
            int c = C;
            int value = Read(info.address);
            C = value & 1;
            value = (value >> 1) | (c << 7);
            value = value & 0xFF;
            Write(info.address, value);
            setZN(value);
        }
    }

    // RTI - Return from Interrupt
    public void rti(stepInfo info) {
        SetFlags((pull() & 0xEF) | 0x20);
        PC = pull16();
    }

    // RTS - Return from Subroutine
    public void rts(stepInfo info) {
        PC = pull16() + 1;
    }

    // SBC - Subtract with Carry
    public void sbc(stepInfo info) {
        int a = A;
        int b = Read(info.address);
        int c = C;
        A = (a - b - (1 - c)) & 0xFF;
        setZN(A);
        if (a - b - (1 - c) >= 0) {
            C = 1;
        } else {
            C = 0;
        }
        if (((a ^ b) & 0x80) != 0 && ((a ^ A) & 0x80) != 0) {
            V = 1;
        } else {
            V = 0;
        }
    }

    // SEC - Set Carry Flag
    public void sec(stepInfo info) {
        C = 1;
    }

    // SED - Set Decimal Flag
    public void sed(stepInfo info) {
        D = 1;
    }

    // SEI - Set Interrupt Disable
    public void sei(stepInfo info) {
        I = 1;
    }

    // STA - Store Accumulator
    public void sta(stepInfo info) {
        Write(info.address, A);
    }

    // STX - Store X Register
    public void stx(stepInfo info) {
        Write(info.address, X);
    }

    // STY - Store Y Register
    public void sty(stepInfo info) {
        Write(info.address, Y);
    }

    // TAX - Transfer Accumulator to X
    public void tax(stepInfo info) {
        X = A;
        setZN(X);
    }

    // TAY - Transfer Accumulator to Y
    public void tay(stepInfo info) {
        Y = A;
        setZN(Y);
    }

    // TSX - Transfer Stack Pointer to X
    public void tsx(stepInfo info) {
        X = SP;
        setZN(X);
    }

    // TXA - Transfer X to Accumulator
    public void txa(stepInfo info) {
        A = X;
        setZN(A);
    }

    // TXS - Transfer X to Stack Pointer
    public void txs(stepInfo info) {
        SP = X;
    }

    // TYA - Transfer Y to Accumulator
    public void tya(stepInfo info) {
        A = Y;
        setZN(A);
    }

    // illegal opcodes below

    public void ahx(stepInfo info) {
    }

    public void alr(stepInfo info) {
    }

    public void anc(stepInfo info) {
    }

    public void arr(stepInfo info) {
    }

    public void axs(stepInfo info) {
    }

    public void dcp(stepInfo info) {
    }

    public void isc(stepInfo info) {
    }

    public void kil(stepInfo info) {
    }

    public void las(stepInfo info) {
    }

    public void lax(stepInfo info) {
    }

    public void rla(stepInfo info) {
    }

    public void rra(stepInfo info) {
    }

    public void sax(stepInfo info) {
    }

    public void shx(stepInfo info) {
    }

    public void shy(stepInfo info) {
    }

    public void slo(stepInfo info) {
    }

    public void sre(stepInfo info) {
    }

    public void tas(stepInfo info) {
    }

    public void xaa(stepInfo info) {
    }
}
