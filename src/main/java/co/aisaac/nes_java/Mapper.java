package co.aisaac.nes_java;

import java.io.IOException;

import static co.aisaac.nes_java.Mapper1.NewMapper1;
import static co.aisaac.nes_java.Mapper2.NewMapper2;
import static co.aisaac.nes_java.Mapper3.NewMapper3;
import static co.aisaac.nes_java.Mapper4.NewMapper4;
import static co.aisaac.nes_java.Mapper40.NewMapper40;
import static co.aisaac.nes_java.Mapper7.NewMapper7;
import static nes.Mapper225.NewMapper225;

// MapperFactory holds the NewMapper creation functions corresponding to the Go version.
abstract class Mapper {

    // This method corresponds to the function NewMapper(console *Console) (Mapper, error) in Golang
    public static Mapper NewMapper(co.aisaac.nes_java.cpu.Console console) throws Exception {
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

    public abstract void Save(Encoder encoder) throws IOException;

    public abstract void Load(Decoder decoder) throws IOException;

    public abstract void Step();

    public abstract byte Read(int address);

    public abstract void Write(int address, byte value);
}
