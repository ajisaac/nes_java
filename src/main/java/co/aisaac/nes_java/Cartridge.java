package co.aisaac.nes_java;

/*
 Header (16 bytes)
 Trainer, if present (0 or 512 bytes)
 PRG ROM data (16384 * x bytes)
 CHR ROM data, if present (8192 * y bytes)
 PlayChoice INST-ROM, if present (0 or 8192 bytes)
 PlayChoice PROM, if present (16 bytes Data, 16 bytes CounterOut) (this is often missing; see PC10 ROM-Images for details)
 */
public class Cartridge {

    byte[] prg;
    byte[] chr;
    byte[] sram;
    int mapper;
    int mirror;
    int battery;

    /**
     * The beginning of every ines file format
     */
    final int MAGIC = 0x1a53454e;

    /**
     * The first 16 bytes
     */

    public Cartridge(String path) {
        System.out.println(path);

        // open file
        std::ifstream file;
        file.open(path);
        if (!file.is_open()) {
            exit(1);
        }


        // read file header
        // Magic number First 4 bytes
        int c = bytes[0] | bytes[1] << 8 | bytes[2] << 16 | bytes[3] << 24;
        if (c != MAGIC) {
            std::cout << "Invalid iNES file" << std::endl;
            exit(1);
        }

        // num of PRG-ROM in 16kb chunks
        char numPrg = bytes[4];

        // num of CHR-ROM banks in 8kb chunks, 0 means board uses CHR-RAM
        char numChr = bytes[5];

        char control1 = bytes[6];
        char control2 = bytes[7];
        char numRam = bytes[8]; // x 8KB

        // unused
        char flags9 = bytes[9];
        char flags10 = bytes[10];

        // mapper type
        int mapper1 = control1 >> 4;
        int mapper2 = control2 >> 4;
        mapper = mapper1 | mapper2 << 4;

        // mirroring type
        int mirror1 = control1 & 1;
        int mirror2 = (control1 >> 3) & 1;
        mirror = mirror1 | mirror2 << 1;

        // battery-backed RAM
        battery = (control1 >> 1) & 1;

        // read trainer if present (unused)
        if ((control1 & 4) == 4) {
            std::vector<char> trainer{};
            for (int i = 0; i < 512; i++) {
                trainer[i] = bytes[i];
            }
        }

        // read prg-rom bank(s)
        prg.resize(numPrg * 16384);
        for (int i = 0; i < numPrg * 16384; i++) {
            prg[i] = bytes[i];
        }

        // read chr-rom bank(s)
        chr.resize(numChr * 8192);
        for (int i = 0; i < numChr * 8192; i++) {
            chr[i] = bytes[i];
        }

        std::cout << "get out of my house " << std::endl;
    }
};

#endif //NES_EMU_CARTRIDGE_H
