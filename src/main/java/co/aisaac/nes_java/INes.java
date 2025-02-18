package co.aisaac.nes_java;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// iNES file loader and Cartridge creation
public class INes {

    public static final int iNESFileMagic = 0x1a53454e;

    // iNESFileHeader struct:
    // Magic    uint32  // iNES magic number
    // NumPRG   byte    // number of PRG-ROM banks (16KB each)
    // NumCHR   byte    // number of CHR-ROM banks (8KB each)
    // Control1 byte    // control bits
    // Control2 byte    // control bits
    // NumRAM   byte    // PRG-RAM size (x 8KB)
    // _        [7]byte // unused padding
    public static class iNESFileHeader {
        public int Magic;     // iNES magic number
        public byte NumPRG;   // number of PRG-ROM banks (16KB each)
        public byte NumCHR;   // number of CHR-ROM banks (8KB each)
        public byte Control1; // control bits
        public byte Control2; // control bits
        public byte NumRAM;   // PRG-RAM size (x 8KB)
        public byte[] padding; // unused padding (7 bytes)
    }

    // LoadNESFile reads an iNES file (.nes) and returns a Cartridge on success.
    // http://wiki.nesdev.com/w/index.php/INES
    // http://nesdev.com/NESDoc.pdf (page 28)
    public static Cartridge LoadNESFile(String path) throws Exception {
        // open file
        try (FileInputStream file = new FileInputStream(path);
             DataInputStream dataInput = new DataInputStream(file)) {

            // read file header
            iNESFileHeader header = readHeader(dataInput);

            // verify header magic number
            if (header.Magic != iNESFileMagic) {
                throw new Exception("invalid .nes file");
            }

            // Convert byte values to unsigned integers for bitwise operations
            int control1 = header.Control1 & 0xFF;
            int control2 = header.Control2 & 0xFF;
            int numPRG = header.NumPRG & 0xFF;
            int numCHR = header.NumCHR & 0xFF;

            // mapper type
            int mapper1 = control1 >> 4;
            int mapper2 = control2 >> 4;
            int mapper = mapper1 | (mapper2 << 4);

            // mirroring type
            int mirror1 = control1 & 1;
            int mirror2 = (control1 >> 3) & 1;
            int mirror = mirror1 | (mirror2 << 1);

            // battery-backed RAM
            int battery = (control1 >> 1) & 1;

            // read trainer if present (unused)
            if ((control1 & 4) == 4) {
                byte[] trainer = new byte[512];
                dataInput.readFully(trainer);
            }

            // read prg-rom bank(s)
            byte[] prg = new byte[numPRG * 16384];
            dataInput.readFully(prg);

            // read chr-rom bank(s)
            byte[] chr = new byte[numCHR * 8192];
            dataInput.readFully(chr);

            // provide chr-rom/ram if not in file
            if (numCHR == 0) {
                chr = new byte[8192];
            }

            // success
            return new Cartridge(prg, chr, mapper, mirror, battery);
        } catch (EOFException e) {
            throw new Exception("Unexpected end of file", e);
        } catch (IOException e) {
            throw new Exception("IO Exception occurred", e);
        }
    }

    // Helper method to read the iNES file header from the input stream.
    private static iNESFileHeader readHeader(DataInputStream input) throws IOException {
        byte[] headerBytes = new byte[16];
        input.readFully(headerBytes);
        ByteBuffer buffer = ByteBuffer.wrap(headerBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        iNESFileHeader header = new iNESFileHeader();
        header.Magic = buffer.getInt();
        header.NumPRG = buffer.get();
        header.NumCHR = buffer.get();
        header.Control1 = buffer.get();
        header.Control2 = buffer.get();
        header.NumRAM = buffer.get();
        header.padding = new byte[7];
        buffer.get(header.padding);
        return header;
    }
}
