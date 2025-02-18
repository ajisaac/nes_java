package co.aisaac.nes_java.ui;


public class Run {
    // Constant declarations (exact translation of Go constants)
    public static final int width = 256;
    public static final int height = 240;
    public static final int scale = 3;
    public static final String title = "NES";


    // Run method translating func Run(paths []string)
    public static void Run(String[] paths) {
        // initialize audio
        PortAudio.initialize();
        try {
            Audio audio = new Audio();
            try {
                try {
                    audio.start();
                } catch (Exception e) {
                    System.err.println(e);
                    System.exit(1);
                }
                // initialize glfw
                if (!Glfw.init()) {
                    System.err.println("Failed to initialize GLFW");
                    System.exit(1);
                }
                try {
                    // create window
                    Glfw.windowHint(Glfw.CONTEXT_VERSION_MAJOR, 2);
                    Glfw.windowHint(Glfw.CONTEXT_VERSION_MINOR, 1);
                    Window window = Glfw.createWindow(width * scale, height * scale, title, null, null);
                    if (window == null) {
                        System.err.println("Failed to create window");
                        System.exit(1);
                    }
                    window.makeContextCurrent();

                    // initialize gl
                    if (!GL.init()) {
                        System.err.println("Failed to initialize GL");
                        System.exit(1);
                    }
                    GL.enable(GL.TEXTURE_2D);

                    // run director
                    Director director = new Director(window, audio);
                    director.start(paths);
                } finally {
                    Glfw.terminate();
                }
            } finally {
                audio.stop();
            }
        } finally {
            PortAudio.terminate();
        }
    }

    // Main method for running the UI
    public static void main(String[] args) {
        Run(args);
    }
}
