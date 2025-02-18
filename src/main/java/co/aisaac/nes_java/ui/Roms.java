package co.aisaac.nes_java.ui;

/* File: Roms.java */
import java.io.File;

public class Roms {
    public static Exception testRom(String path) {
        Exception err = null;
        try {
            // Call NewConsole from the nes package to create a new Console instance.
            Console console = NES.NewConsole(path);
            // Step the console for 3 seconds.
            console.stepSeconds(3);
        } catch (Throwable r) {
            if (r instanceof Exception) {
                err = (Exception) r;
            } else {
                err = new Exception(r);
            }
        }
        return err;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Roms roms_directory");
            System.exit(1);
        }
        String dir = args[0];
        File directory = new File(dir);
        if (!directory.isDirectory()) {
            System.err.println("Provided path is not a directory: " + dir);
            System.exit(1);
        }
        File[] infos = directory.listFiles();
        if (infos == null) {
            throw new RuntimeException("Cannot read directory: " + dir);
        }
        for (File info : infos) {
            String name = info.getName();
            if (!name.endsWith(".nes")) {
                continue;
            }
            // Construct the full path for the ROM file.
            String fullName = new File(directory, name).getPath();
            Exception err = testRom(fullName);
            if (err == null) {
                System.out.println("OK  " + fullName);
            } else {
                System.out.println("FAIL " + fullName);
                System.out.println(err);
            }
        }
    }
}

