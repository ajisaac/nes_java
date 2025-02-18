package co.aisaac.nes_java;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Nes {
    // Global Palette array of 64 RGBA colors
    public static Color[] Palette = new Color[64];

    static {
        int[] colors = new int[]{
                0x666666, 0x002A88, 0x1412A7, 0x3B00A4, 0x5C007E, 0x6E0040, 0x6C0600, 0x561D00,
                0x333500, 0x0B4800, 0x005200, 0x004F08, 0x00404D, 0x000000, 0x000000, 0x000000,
                0xADADAD, 0x155FD9, 0x4240FF, 0x7527FE, 0xA01ACC, 0xB71E7B, 0xB53120, 0x994E00,
                0x6B6D00, 0x388700, 0x0C9300, 0x008F32, 0x007C8D, 0x000000, 0x000000, 0x000000,
                0xFFFEFF, 0x64B0FF, 0x9290FF, 0xC676FF, 0xF36AFF, 0xFE6ECC, 0xFE8170, 0xEA9E22,
                0xBCBE00, 0x88D800, 0x5CE430, 0x45E082, 0x48CDDE, 0x4F4F4F, 0x000000, 0x000000,
                0xFFFEFF, 0xC0DFFF, 0xD3D2FF, 0xE8C8FF, 0xFBC2FF, 0xFEC4EA, 0xFECCC5, 0xF7D8A5,
                0xE4E594, 0xCFEF96, 0xBDF4AB, 0xB3F3CC, 0xB5EBF2, 0xB8B8B8, 0x000000, 0x000000,
        };
        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            byte r = (byte) (c >> 16);
            byte g = (byte) (c >> 8);
            byte b = (byte) (c);
            // Construct the Color with red, green, blue and full opacity (0xFF)
            Palette[i] = new Color(r & 0xFF, g & 0xFF, b & 0xFF, 0xFF);
        }
    }
}

class PPU {
    // Array of palette data bytes
    public byte[] paletteData;
    // Front buffer as a BufferedImage (acting as image.RGBA)
    public BufferedImage front;

    // Read palette data at the given address with adjustments
    public int readPalette(int address) {
        if (address >= 16 && address % 4 == 0) {
            address -= 16;
        }
        return paletteData[address] & 0xFF;
    }

    // Write a byte value to the palette data at the given address with adjustments
    public void writePalette(int address, int value) {
        if (address >= 16 && address % 4 == 0) {
            address -= 16;
        }
        paletteData[address] = (byte)(value & 0xFF);
    }
}

class Console {
    // The PPU instance associated with this Console
    public PPU PPU;

    // Return the background color using palette lookup
    public Color BackgroundColor() {
        return Nes.Palette[PPU.readPalette(0) % 64];
    }

    // Return the front buffer image
    public BufferedImage Buffer() {
        return PPU.front;
    }
}
