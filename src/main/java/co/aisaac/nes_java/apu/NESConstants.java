package co.aisaac.nes_java.apu;

public class NESConstants {
    public static final double frameCounterRate = CPUFrequency / 240.0;

    public static final byte[] lengthTable = new byte[]{
            10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14,
            12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30,
    };

    public static final byte[][] dutyTable = new byte[][]{
            {0, 1, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 1, 1},
    };

    public static final byte[] triangleTable = new byte[]{
            15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
    };

    public static final int[] noiseTable = new int[]{
            4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068,
    };

    public static final byte[] dmcTable = new byte[]{
            (byte) 214, (byte) 190, (byte) 170, (byte) 160, (byte) 143, (byte) 127, (byte) 113, (byte) 107, (byte) 95, (byte) 80, (byte) 71, (byte) 64, (byte) 53, (byte) 42, (byte) 36, (byte) 27,
    };

    public static final float[] pulseTable = new float[31];
    public static final float[] tndTable = new float[203];

    static {
        for (int i = 0; i < 31; i++) {
            pulseTable[i] = 95.52f / ((8128.0f / (i)) + 100);
        }
        for (int i = 0; i < 203; i++) {
            tndTable[i] = 163.67f / ((24329.0f / (i)) + 100);
        }
    }
}
