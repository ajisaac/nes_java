package co.aisaac.nes_java.cpu;

import co.aisaac.nes_java.Console;
import co.aisaac.nes_java.memory.Memory;


public class CPU {
    // Functional interface for CPU instruction functions
    @FunctionalInterface
    interface Instruction {
        void execute(StepInfo info);
    }

    // NewCPU builds and returns a new CPU instance
    public CPU(Console console) {
        this.memory = new CPUMemory(console);
        this.createTable();
        this.reset();
    }

    // memory interface
    Memory memory;
    public long Cycles; // number of cycles
    int PC;      // program counter (16-bit)
    int SP;      // stack pointer (8-bit)
    int A;       // accumulator (8-bit)
    int X;       // x register (8-bit)
    int Y;       // y register (8-bit)
    int C;       // carry flag (8-bit)
    int Z;       // zero flag (8-bit)
    int I;       // interrupt disable flag (8-bit)
    int D;       // decimal mode flag (8-bit)
    int B;       // break command flag (8-bit)
    int U;       // unused flag (8-bit)
    int V;       // overflow flag (8-bit)
    int N;       // negative flag (8-bit)
    int interrupt; // interrupt type to perform (8-bit)
    public int stall;     // number of cycles to stall
    Instruction[] table = new Instruction[256];

    // CPU every instruction constant
    public static final int CPUFrequency = 1789773;

    // interrupt types
    static final int interruptNone = 1;
    static final int interruptNMI = 2;
    static final int interruptIRQ = 3;

    // addressing modes
    static final int modeAbsolute = 1;
    static final int modeAbsoluteX = 2;
    static final int modeAbsoluteY = 3;
    static final int modeAccumulator = 4;
    static final int modeImmediate = 5;
    static final int modeImplied = 6;
    static final int modeIndexedIndirect = 7;
    static final int modeIndirect = 8;
    static final int modeIndirectIndexed = 9;
    static final int modeRelative = 10;
    static final int modeZeroPage = 11;
    static final int modeZeroPageX = 12;
    static final int modeZeroPageY = 13;

    // instructionModes indicates the addressing mode for each instruction
    static final byte[] instructionModes = {
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
    static final byte[] instructionSizes = {
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
    static final byte[] instructionCycles = {
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
    static final byte[] instructionPageCycles = {
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
    static final String[] instructionNames = {
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


    // createTable builds a function table for each instruction
    void createTable() {
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

    // Reset resets the CPU to its initial powerup state
    public void reset() {
        PC = Read16(0xFFFC);
        SP = 0xFD;
        SetFlags(0x24);
    }

    // PrintInstruction prints the current CPU state
    void PrintInstruction() {
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
    static boolean pagesDiffer(int a, int b) {
        return (a & 0xFF00) != (b & 0xFF00);
    }

    // addBranchCycles adds a cycle for taking a branch and adds another cycle
    // if the branch jumps to a new page
    void addBranchCycles(StepInfo info) {
        Cycles++;
        if (pagesDiffer(info.pc, info.address)) {
            Cycles++;
        }
    }

    void compare(int a, int b) {
        setZN(a - b);
        if (a >= b) {
            C = 1;
        } else {
            C = 0;
        }
    }

    // Read16 reads two bytes using Read to return a double-word value
    int Read16(int address) {
        int lo = Read(address);
        int hi = Read(address + 1);
        return (hi << 8) | lo;
    }

    // read16bug emulates a 6502 bug that caused the low byte to wrap without
    // incrementing the high byte
    int read16bug(int address) {
        int a = address;
        int b = (a & 0xFF00) | (((a & 0xFF) + 1) & 0xFF);
        int lo = Read(a);
        int hi = Read(b);
        return (hi << 8) | lo;
    }

    // push pushes a byte onto the stack
    void push(int value) {
        Write(0x100 | SP, value);
        SP = (SP - 1) & 0xFF;
    }

    // pull pops a byte from the stack
    int pull() {
        SP = (SP + 1) & 0xFF;
        return Read(0x100 | SP);
    }

    // push16 pushes two bytes onto the stack
    void push16(int value) {
        int hi = (value >> 8) & 0xFF;
        int lo = value & 0xFF;
        push(hi);
        push(lo);
    }

    // pull16 pops two bytes from the stack
    int pull16() {
        int lo = pull();
        int hi = pull();
        return (hi << 8) | lo;
    }

    // Flags returns the processor status flags
    int Flags() {
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
    void SetFlags(int flags) {
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
    void setZ(int value) {
        if ((value & 0xFF) == 0) {
            Z = 1;
        } else {
            Z = 0;
        }
    }

    // setN sets the negative flag if the argument is negative (high bit is set)
    void setN(int value) {
        if ((value & 0x80) != 0) {
            N = 1;
        } else {
            N = 0;
        }
    }

    // setZN sets the zero flag and the negative flag
    void setZN(int value) {
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
        return memory.Read(address);
    }

    // Write delegates to the memory write method
    void Write(int address, int value) {
        memory.Write(address, (byte) value);
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
        StepInfo info = new StepInfo(address, PC, mode);
        table[opcode].execute(info);

        return (int) (Cycles - cyclesBefore);
    }

    // NMI - Non-Maskable Interrupt
    void nmi() {
        push16(PC);
        php(null);
        PC = Read16(0xFFFA);
        I = 1;
        Cycles += 7;
    }

    // IRQ - IRQ Interrupt
    void irq() {
        push16(PC);
        php(null);
        PC = Read16(0xFFFE);
        I = 1;
        Cycles += 7;
    }

    // ADC - Add with Carry
    void adc(StepInfo info) {
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
    void and(StepInfo info) {
        A = A & Read(info.address);
        setZN(A);
    }

    // ASL - Arithmetic Shift Left
    void asl(StepInfo info) {
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
    void bcc(StepInfo info) {
        if (C == 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BCS - Branch if Carry Set
    void bcs(StepInfo info) {
        if (C != 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BEQ - Branch if Equal
    void beq(StepInfo info) {
        if (Z != 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BIT - Bit Test
    void bit(StepInfo info) {
        int value = Read(info.address);
        V = (value >> 6) & 1;
        setZ(value & A);
        setN(value);
    }

    // BMI - Branch if Minus
    void bmi(StepInfo info) {
        if (N != 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BNE - Branch if Not Equal
    void bne(StepInfo info) {
        if (Z == 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BPL - Branch if Positive
    void bpl(StepInfo info) {
        if (N == 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BRK - Force Interrupt
    void brk(StepInfo info) {
        push16(PC);
        php(info);
        sei(info);
        PC = Read16(0xFFFE);
    }

    // BVC - Branch if Overflow Clear
    void bvc(StepInfo info) {
        if (V == 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // BVS - Branch if Overflow Set
    void bvs(StepInfo info) {
        if (V != 0) {
            PC = info.address;
            addBranchCycles(info);
        }
    }

    // CLC - Clear Carry Flag
    void clc(StepInfo info) {
        C = 0;
    }

    // CLD - Clear Decimal Mode
    void cld(StepInfo info) {
        D = 0;
    }

    // CLI - Clear Interrupt Disable
    void cli(StepInfo info) {
        I = 0;
    }

    // CLV - Clear Overflow Flag
    void clv(StepInfo info) {
        V = 0;
    }

    // CMP - Compare
    void cmp(StepInfo info) {
        int value = Read(info.address);
        compare(A, value);
    }

    // CPX - Compare X Register
    void cpx(StepInfo info) {
        int value = Read(info.address);
        compare(X, value);
    }

    // CPY - Compare Y Register
    void cpy(StepInfo info) {
        int value = Read(info.address);
        compare(Y, value);
    }

    // DEC - Decrement Memory
    void dec(StepInfo info) {
        int value = (Read(info.address) - 1) & 0xFF;
        Write(info.address, value);
        setZN(value);
    }

    // DEX - Decrement X Register
    void dex(StepInfo info) {
        X = (X - 1) & 0xFF;
        setZN(X);
    }

    // DEY - Decrement Y Register
    void dey(StepInfo info) {
        Y = (Y - 1) & 0xFF;
        setZN(Y);
    }

    // EOR - Exclusive OR
    void eor(StepInfo info) {
        A = A ^ Read(info.address);
        setZN(A);
    }

    // INC - Increment Memory
    void inc(StepInfo info) {
        int value = (Read(info.address) + 1) & 0xFF;
        Write(info.address, value);
        setZN(value);
    }

    // INX - Increment X Register
    void inx(StepInfo info) {
        X = (X + 1) & 0xFF;
        setZN(X);
    }

    // INY - Increment Y Register
    void iny(StepInfo info) {
        Y = (Y + 1) & 0xFF;
        setZN(Y);
    }

    // JMP - Jump
    void jmp(StepInfo info) {
        PC = info.address;
    }

    // JSR - Jump to Subroutine
    void jsr(StepInfo info) {
        push16(PC - 1);
        PC = info.address;
    }

    // LDA - Load Accumulator
    void lda(StepInfo info) {
        A = Read(info.address);
        setZN(A);
    }

    // LDX - Load X Register
    void ldx(StepInfo info) {
        X = Read(info.address);
        setZN(X);
    }

    // LDY - Load Y Register
    void ldy(StepInfo info) {
        Y = Read(info.address);
        setZN(Y);
    }

    // LSR - Logical Shift Right
    void lsr(StepInfo info) {
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
    void nop(StepInfo info) {
    }

    // ORA - Logical Inclusive OR
    void ora(StepInfo info) {
        A = A | Read(info.address);
        setZN(A);
    }

    // PHA - Push Accumulator
    void pha(StepInfo info) {
        push(A);
    }

    // PHP - Push Processor Status
    void php(StepInfo info) {
        push(Flags() | 0x10);
    }

    // PLA - Pull Accumulator
    void pla(StepInfo info) {
        A = pull();
        setZN(A);
    }

    // PLP - Pull Processor Status
    void plp(StepInfo info) {
        SetFlags((pull() & 0xEF) | 0x20);
    }

    // ROL - Rotate Left
    void rol(StepInfo info) {
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
    void ror(StepInfo info) {
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
    void rti(StepInfo info) {
        SetFlags((pull() & 0xEF) | 0x20);
        PC = pull16();
    }

    // RTS - Return from Subroutine
    void rts(StepInfo info) {
        PC = pull16() + 1;
    }

    // SBC - Subtract with Carry
    void sbc(StepInfo info) {
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
    void sec(StepInfo info) {
        C = 1;
    }

    // SED - Set Decimal Flag
    void sed(StepInfo info) {
        D = 1;
    }

    // SEI - Set Interrupt Disable
    void sei(StepInfo info) {
        I = 1;
    }

    // STA - Store Accumulator
    void sta(StepInfo info) {
        Write(info.address, A);
    }

    // STX - Store X Register
    void stx(StepInfo info) {
        Write(info.address, X);
    }

    // STY - Store Y Register
    void sty(StepInfo info) {
        Write(info.address, Y);
    }

    // TAX - Transfer Accumulator to X
    void tax(StepInfo info) {
        X = A;
        setZN(X);
    }

    // TAY - Transfer Accumulator to Y
    void tay(StepInfo info) {
        Y = A;
        setZN(Y);
    }

    // TSX - Transfer Stack Pointer to X
    void tsx(StepInfo info) {
        X = SP;
        setZN(X);
    }

    // TXA - Transfer X to Accumulator
    void txa(StepInfo info) {
        A = X;
        setZN(A);
    }

    // TXS - Transfer X to Stack Pointer
    void txs(StepInfo info) {
        SP = X;
    }

    // TYA - Transfer Y to Accumulator
    void tya(StepInfo info) {
        A = Y;
        setZN(A);
    }

    // illegal opcodes below

    void ahx(StepInfo info) {
    }

    void alr(StepInfo info) {
    }

    void anc(StepInfo info) {
    }

    void arr(StepInfo info) {
    }

    void axs(StepInfo info) {
    }

    void dcp(StepInfo info) {
    }

    void isc(StepInfo info) {
    }

    void kil(StepInfo info) {
    }

    void las(StepInfo info) {
    }

    void lax(StepInfo info) {
    }

    void rla(StepInfo info) {
    }

    void rra(StepInfo info) {
    }

    void sax(StepInfo info) {
    }

    void shx(StepInfo info) {
    }

    void shy(StepInfo info) {
    }

    void slo(StepInfo info) {
    }

    void sre(StepInfo info) {
    }

    void tas(StepInfo info) {
    }

    void xaa(StepInfo info) {
    }
}
