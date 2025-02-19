package co.aisaac.nes_java.ui;

import co.aisaac.nes_java.Cartridge;
import co.aisaac.nes_java.Console;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


// constant padding
public class GameView implements View {
    // fields exactly as in original code
    public Director director;
    public Console console;
    public String title;
    public String hash;
    public int texture;
    public boolean record;
    public List<BufferedImage> frames;

    private static final float padding = 0;

    // Constructor matching NewGameView functionality via factory method below
    public GameView(Director director, Console console, String title, String hash, int texture, boolean record, List<BufferedImage> frames) {
        this.director = director;
        this.console = console;
        this.title = title;
        this.hash = hash;
        this.texture = texture;
        this.record = record;
        this.frames = frames;
    }

    // factory method NewGameView
    public static View NewGameView(Director director, Console console, String title, String hash) {
        int texture = Utils.createTexture();
        return new GameView(director, console, title, hash, texture, false, null);
    }

    // load method
    public void load(int snapshot) {
        // load state
//        if (this.console.LoadState(Utils.savePath(this.hash, snapshot))) {
//            return;
//        } else {
//            this.console.Reset();
//        }
        // load sram
//        Cartridge cartridge = this.console.Cartridge;
//        if (cartridge.Battery != 0) {
//            byte[] sram = Utils.readSRAM(Utils.sramPath(this.hash, snapshot));
//            if (sram != null) {
//                cartridge.SRAM = sram;
//            }
//        }
    }

    // save method
    public void save(int snapshot) {
        // save sram
        Cartridge cartridge = this.console.Cartridge;
        if (cartridge.Battery != 0) {
//            Utils.writeSRAM(Utils.sramPath(this.hash, snapshot), cartridge.SRAM);
        }
        // save state
//        this.console.SaveState(Utils.savePath(this.hash, snapshot));
    }

    // Enter method
    public void Enter() {
//        GL11.glClearColor(0, 0, 0, 1);
        this.director.SetTitle(this.title);
        this.console.SetAudioChannel(this.director.audio.channel);
        this.console.SetAudioSampleRate(this.director.audio.sampleRate);
//        this.director.window.setKeyCallback(new GLFWKeyCallback() {
//            @Override
//            public void invoke(long window, int key, int scancode, int action, int mods) {
//                onKey(window, key, scancode, action, mods);
//            }
//        });
        this.load(-1);
    }

    // Exit method
    public void Exit() {
//        this.director.window.setKeyCallback(null);
        this.console.SetAudioChannel(null);
        this.console.SetAudioSampleRate(0);
        this.save(-1);
    }

    // Update method
    public void Update(double t, double dt) {
//        if (dt > 1) {
//            dt = 0;
//        }
//        long window = this.director.window;
//        Console consoleLocal = this.console;
//        if (Utils.joystickReset(GLFW.JOYSTICK_1)) {
//            this.director.ShowMenu();
//        }
//        if (Utils.joystickReset(GLFW.JOYSTICK_2)) {
//            this.director.ShowMenu();
//        }
//        if (Utils.readKey(window, GLFW.GLFW_KEY_ESCAPE)) {
//            this.director.ShowMenu();
//        }
//        Utils.updateControllers(window, consoleLocal);
//        consoleLocal.StepSeconds(dt);
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
//        Utils.setTexture(consoleLocal.Buffer());
//        drawBuffer(this.director.window);
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
//        if (this.record) {
//            if (this.frames == null) {
//                this.frames = new ArrayList<>();
//            }
//            this.frames.add(Utils.copyImage(consoleLocal.Buffer()));
//        }
    }

    // onKey method
    public void onKey(long window, int key, int scancode, int action, int mods) {
//        if (action == GLFW.GLFW_PRESS) {
//            if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
//                int snapshot = key - GLFW.GLFW_KEY_0;
//                if ((mods & GLFW.GLFW_MOD_SHIFT) == 0) {
//                    this.load(snapshot);
//                } else {
//                    this.save(snapshot);
//                }
//            }
//            switch (key) {
//                case GLFW.GLFW_KEY_SPACE:
//                    Utils.screenshot(this.console.Buffer());
//                    break;
//                case GLFW.GLFW_KEY_R:
//                    this.console.Reset();
//                    break;
//                case GLFW.GLFW_KEY_TAB:
//                    if (this.record) {
//                        this.record = false;
//                        Utils.animation(this.frames);
//                        this.frames = null;
//                    } else {
//                        this.record = true;
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
    }

    // drawBuffer function
    public static void drawBuffer(long window) {
//        int[] wh = Utils.getFramebufferSize(window);
//        int w = wh[0];
//        int h = wh[1];
//        float s1 = (float) w / 256;
//        float s2 = (float) h / 240;
//        float f = 1 - padding;
//        float x, y;
//        if (s1 >= s2) {
//            x = f * s2 / s1;
//            y = f;
//        } else {
//            x = f;
//            y = f * s1 / s2;
//        }
//        GL11.glBegin(GL11.GL_QUADS);
//        GL11.glTexCoord2f(0, 1);
//        GL11.glVertex2f(-x, -y);
//        GL11.glTexCoord2f(1, 1);
//        GL11.glVertex2f(x, -y);
//        GL11.glTexCoord2f(1, 0);
//        GL11.glVertex2f(x, y);
//        GL11.glTexCoord2f(0, 0);
//        GL11.glVertex2f(-x, y);
//        GL11.glEnd();
    }

    // updateControllers function
    public static void updateControllers(long window, Console console) {
//        boolean turbo = (console.PPU.Frame % 6) < 3;
//        int k1 = Utils.readKeys(window, turbo);
//        int j1 = Utils.readJoystick(GLFW.JOYSTICK_1, turbo);
//        int j2 = Utils.readJoystick(GLFW.JOYSTICK_2, turbo);
//        console.SetButtons1(Utils.combineButtons(k1, j1));
//        console.SetButtons2(j2);
    }
}

