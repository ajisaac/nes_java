package co.aisaac.nes_java;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Interface Mapper as defined in the Golang code
public interface Mapper {
    byte Read(int address);
    void Write(int address, byte value);
    void Step();
    void Save(ObjectOutputStream encoder) throws IOException;
    void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException;
}

// Class representing a Console with its Cartridge
class Console {
    public Cartridge Cartridge;

    public Console(Cartridge cartridge) {
        this.Cartridge = cartridge;
    }
}

// Class representing a Cartridge with at least a Mapper identifier and PRG data
class Cartridge {
    public int Mapper;
    public byte[] PRG;

    public Cartridge(int mapper, byte[] prg) {
        this.Mapper = mapper;
        this.PRG = prg;
    }
}

// MapperFactory holds the NewMapper creation functions corresponding to the Go version.
class MapperFactory {

    // This method corresponds to the function NewMapper(console *Console) (Mapper, error) in Golang
    public static Mapper NewMapper(Console console) throws Exception {
        Cartridge cartridge = console.Cartridge;
        switch (cartridge.Mapper) {
            case 0:
                return NewMapper2(cartridge);
            case 1:
                return NewMapper1(cartridge);
            case 2:
                return NewMapper2(cartridge);
            case 3:
                return NewMapper3(cartridge);
            case 4:
                return NewMapper4(console, cartridge);
            case 7:
                return NewMapper7(cartridge);
            case 40:
                return NewMapper40(console, cartridge);
            case 225:
                return NewMapper225(cartridge);
            default:
                throw new Exception(String.format("unsupported mapper: %d", cartridge.Mapper));
        }
    }

    // Corresponds to function NewMapper1(cartridge *Cartridge) Mapper in Golang
    public static Mapper NewMapper1(Cartridge cartridge) {
        Mapper1 m = new Mapper1();
        m.Cartridge = cartridge;
        m.shiftRegister = 0x10;
        m.prgOffsets[1] = m.prgBankOffset(-1);
        return m;
    }

    // Corresponds to function NewMapper4(console *Console, cartridge *Cartridge) Mapper in Golang
    public static Mapper NewMapper4(Console console, Cartridge cartridge) {
        Mapper4 m = new Mapper4();
        m.Cartridge = cartridge;
        m.console = console;
        m.prgOffsets[0] = m.prgBankOffset(0);
        m.prgOffsets[1] = m.prgBankOffset(1);
        m.prgOffsets[2] = m.prgBankOffset(-2);
        m.prgOffsets[3] = m.prgBankOffset(-1);
        return m;
    }

    // Corresponds to function NewMapper2(cartridge *Cartridge) Mapper in Golang
    public static Mapper NewMapper2(Cartridge cartridge) {
        int prgBanks = cartridge.PRG.length / 0x4000;
        int prgBank1 = 0;
        int prgBank2 = prgBanks - 1;
        return new Mapper2(cartridge, prgBanks, prgBank1, prgBank2);
    }

    // Stub for NewMapper3 corresponding to mapper3.go in the original code
    public static Mapper NewMapper3(Cartridge cartridge) {
        return new Mapper3(cartridge);
    }

    // Stub for NewMapper7 corresponding to mapper7.go in the original code
    public static Mapper NewMapper7(Cartridge cartridge) {
        return new Mapper7(cartridge);
    }

    // Corresponds to function NewMapper40(console *Console, cartridge *Cartridge) Mapper in Golang
    public static Mapper NewMapper40(Console console, Cartridge cartridge) {
        return new Mapper40(cartridge, console, 0, 0);
    }

    // Stub for NewMapper225 corresponding to mapper225.go in the original code
    public static Mapper NewMapper225(Cartridge cartridge) {
        return new Mapper225(cartridge);
    }
}

// Implementation of Mapper1 as defined in mapper1.go
class Mapper1 implements Mapper {
    public Cartridge Cartridge;
    public int shiftRegister;
    public int[] prgOffsets = new int[2];

    // Dummy implementation of prgBankOffset method used for setting offsets
    public int prgBankOffset(int bank) {
        return bank * 0x4000;
    }

    @Override
    public byte Read(int address) {
        // Dummy implementation; return 0
        return 0;
    }

    @Override
    public void Write(int address, byte value) {
        // Dummy implementation; do nothing
    }

    @Override
    public void Step() {
        // Dummy implementation; do nothing
    }

    @Override
    public void Save(ObjectOutputStream encoder) throws IOException {
        // Dummy implementation; do nothing
    }

    @Override
    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        // Dummy implementation; do nothing
    }
}

// Implementation of Mapper4 as defined in mapper4.go
class Mapper4 implements Mapper {
    public Cartridge Cartridge;
    public Console console;
    public int[] prgOffsets = new int[4];

    // Dummy implementation of prgBankOffset method used for setting offsets
    public int prgBankOffset(int bank) {
        return bank * 0x4000;
    }

    @Override
    public byte Read(int address) {
        // Dummy implementation; return 0
        return 0;
    }

    @Override
    public void Write(int address, byte value) {
        // Dummy implementation; do nothing
    }

    @Override
    public void Step() {
        // Dummy implementation; do nothing
    }

    @Override
    public void Save(ObjectOutputStream encoder) throws IOException {
        // Dummy implementation; do nothing
    }

    @Override
    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        // Dummy implementation; do nothing
    }
}

// Implementation of Mapper2 as defined in mapper2.go
class Mapper2 implements Mapper {
    public Cartridge cartridge;
    public int prgBanks;
    public int prgBank1;
    public int prgBank2;

    public Mapper2(Cartridge cartridge, int prgBanks, int prgBank1, int prgBank2) {
        this.cartridge = cartridge;
        this.prgBanks = prgBanks;
        this.prgBank1 = prgBank1;
        this.prgBank2 = prgBank2;
    }

    @Override
    public byte Read(int address) {
        // Dummy implementation; return 0
        return 0;
    }

    @Override
    public void Write(int address, byte value) {
        // Dummy implementation; do nothing
    }

    @Override
    public void Step() {
        // Dummy implementation; do nothing
    }

    @Override
    public void Save(ObjectOutputStream encoder) throws IOException {
        // Dummy implementation; do nothing
    }

    @Override
    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        // Dummy implementation; do nothing
    }
}

// Stub implementation for Mapper3 corresponding to mapper3.go
class Mapper3 implements Mapper {
    public Cartridge Cartridge;

    public Mapper3(Cartridge cartridge) {
        this.Cartridge = cartridge;
    }

    @Override
    public byte Read(int address) {
        // Dummy implementation; return 0
        return 0;
    }

    @Override
    public void Write(int address, byte value) {
        // Dummy implementation; do nothing
    }

    @Override
    public void Step() {
        // Dummy implementation; do nothing
    }

    @Override
    public void Save(ObjectOutputStream encoder) throws IOException {
        // Dummy implementation; do nothing
    }

    @Override
    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        // Dummy implementation; do nothing
    }
}

// Stub implementation for Mapper7 corresponding to mapper7.go
class Mapper7 implements Mapper {
    public Cartridge Cartridge;

    public Mapper7(Cartridge cartridge) {
        this.Cartridge = cartridge;
    }

    @Override
    public byte Read(int address) {
        // Dummy implementation; return 0
        return 0;
    }

    @Override
    public void Write(int address, byte value) {
        // Dummy implementation; do nothing
    }

    @Override
    public void Step() {
        // Dummy implementation; do nothing
    }

    @Override
    public void Save(ObjectOutputStream encoder) throws IOException {
        // Dummy implementation; do nothing
    }

    @Override
    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        // Dummy implementation; do nothing
    }
}

// Implementation of Mapper40 as defined in mapper40.go
class Mapper40 implements Mapper {
    public Cartridge cartridge;
    public Console console;
    public int field1;
    public int field2;

    public Mapper40(Cartridge cartridge, Console console, int field1, int field2) {
        this.cartridge = cartridge;
        this.console = console;
        this.field1 = field1;
        this.field2 = field2;
    }

    @Override
    public byte Read(int address) {
        // Dummy implementation; return 0
        return 0;
    }

    @Override
    public void Write(int address, byte value) {
        // Dummy implementation; do nothing
    }

    @Override
    public void Step() {
        // Dummy implementation; do nothing
    }

    @Override
    public void Save(ObjectOutputStream encoder) throws IOException {
        // Dummy implementation; do nothing
    }

    @Override
    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        // Dummy implementation; do nothing
    }
}

// Stub implementation for Mapper225 corresponding to mapper225.go
class Mapper225 implements Mapper {
    public Cartridge Cartridge;

    public Mapper225(Cartridge cartridge) {
        this.Cartridge = cartridge;
    }

    @Override
    public byte Read(int address) {
        // Dummy implementation; return 0
        return 0;
    }

    @Override
    public void Write(int address, byte value) {
        // Dummy implementation; do nothing
    }

    @Override
    public void Step() {
        // Dummy implementation; do nothing
    }

    @Override
    public void Save(ObjectOutputStream encoder) throws IOException {
        // Dummy implementation; do nothing
    }

    @Override
    public void Load(ObjectInputStream decoder) throws IOException, ClassNotFoundException {
        // Dummy implementation; do nothing
    }
}
