package co.aisaac.nes_java.mappers;

import co.aisaac.nes_java.Cartridge;
import co.aisaac.nes_java.Console;

import static co.aisaac.nes_java.mappers.Mapper1.NewMapper1;
import static co.aisaac.nes_java.mappers.Mapper2.NewMapper2;
import static co.aisaac.nes_java.mappers.Mapper3.NewMapper3;
import static co.aisaac.nes_java.mappers.Mapper4.NewMapper4;
import static co.aisaac.nes_java.mappers.Mapper40.NewMapper40;
import static co.aisaac.nes_java.mappers.Mapper7.NewMapper7;
import static co.aisaac.nes_java.mappers.Mapper225.NewMapper225;

// MapperFactory holds the NewMapper creation functions corresponding to the Go version.
public abstract class Mapper {

    // This method corresponds to the function NewMapper(console *Console) (Mapper, error) in Golang
    public static Mapper NewMapper(Console console) throws Exception {
        Cartridge cartridge = console.cartridge;
        switch (cartridge.mapper) {
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
            case (int /*byte*/) 225:
                return NewMapper225(cartridge);
            default:
                throw new Exception(String.format("unsupported mapper: %d", cartridge.mapper));
        }
    }

    public abstract void Step();

    public abstract int /*byte*/ read(int address);

    public abstract void write(int address, int /*byte*/ value);
}
