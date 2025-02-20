package co.aisaac.nes_java;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Director {
    long window;
    Audio audio;
    double timestamp;
    String title;
    String hash;

    public Console console;
    public int texture;
    public boolean record;
    public List<BufferedImage> frames;

    private static final float padding = 0;

    public Director(long window, String path) {
        this.window = window;
        this.timestamp = GLFW.glfwGetTime();
        this.title = path;

        try {
            this.hash = hashFile(path);
            this.console = Console.NewConsole(path);
        } catch (Exception err) {
            err.printStackTrace();
            System.exit(1);
        }

        this.texture = createTexture();

        // todo create audio
        this.console.SetAudioChannel(this.audio.channel);
        this.console.SetAudioSampleRate(this.audio.sampleRate);

        this.load_cartridge(-1);
    }

    public void run() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            this.step();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GLFW.glfwSwapBuffers(window);

            GLFW.glfwPollEvents();
        }
    }

    public void step() {
        double timestamp = GLFW.glfwGetTime();
        double dt = timestamp - this.timestamp;
        this.timestamp = timestamp;

        if (dt > 1) {
            dt = 0;
        }

        updateControllers();
        console.stepSeconds(dt);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
        setTexture(this.console.Buffer());

        drawBuffer(window);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        if (this.record) {
            if (this.frames == null) {
                this.frames = new ArrayList<>();
            }
            this.frames.add(copyImage(this.console.Buffer()));
        }
    }

    // setTexture sets the texture image data based on the provided BufferedImage.
    void setTexture(BufferedImage im) {
        int width = im.getWidth();
        int height = im.getHeight();
        ByteBuffer buffer = convertImageToByteBuffer(im);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    void updateControllers() {
        boolean turbo = (console.PPU.Frame % 6) < 3;
        boolean[] k1 = readKeys(window, turbo);
        boolean[] j1 = readJoystick(GLFW.GLFW_JOYSTICK_1, turbo);
        boolean[] j2 = readJoystick(GLFW.GLFW_JOYSTICK_2, turbo);
        console.SetButtons1(combineButtons(k1, j1));
        console.SetButtons2(j2);
    }

    // GAME VIEW
    void drawBuffer(long window) {
        // todo check this
        int[] w = new int[1];
        int[] h = new int[1];
        GLFW.glfwGetFramebufferSize(window, w, h);
        int width = w[0];
        int height = h[0];
        float s1 = (float) width / 256;
        float s2 = (float) height / 240;
        float f = 1 - padding;
        float x, y;
        if (s1 >= s2) {
            x = f * s2 / s1;
            y = f;
        } else {
            x = f;
            y = f * s1 / s2;
        }
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(-x, -y);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(x, -y);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(-x, y);
        GL11.glEnd();
    }


    void load_cartridge(int snapshot) {
        if (this.console.Cartridge.Battery != 0) {
            byte[] sram;
            try {
                sram = readSRAM(sramPath(this.hash, snapshot));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.console.Cartridge.SRAM = sram;
        }
    }

    void save(int snapshot) {
        if (this.console.Cartridge.Battery != 0) {
            try {
                writeSRAM(sramPath(this.hash, snapshot), this.console.Cartridge.SRAM);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // writeSRAM writes the SRAM byte array to the specified file.
    void writeSRAM(String filename, byte[] sram) throws IOException {
        File file = new File(filename);
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
            }
        }
        FileOutputStream fos = new FileOutputStream(file);
        DataOutputStream dos = new DataOutputStream(fos);
        // Write raw bytes; endianness does not matter for a byte array.
        dos.write(sram);
        dos.close();
        fos.close();
    }

    byte[] readSRAM(String filename) throws IOException {
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] sram = new byte[0x2000];
        int bytesRead = dis.read(sram);
        if (bytesRead != sram.length) {
            dis.close();
            fis.close();
            throw new IOException("Failed to read full SRAM data");
        }
        dis.close();
        fis.close();
        return sram;
    }

    String homeDir = "/Users/aaron/Code/personal/nes_java/src/main/resources/co/aisaac/nes_java";
    // sramPath returns the sram file path based on hash and snapshot
    String sramPath(String hash, int snapshot) {
        if (snapshot >= 0) {
            return String.format("%s/.nes/sram/%s-%d.dat", homeDir, hash, snapshot);
        }
        return String.format("%s/.nes/sram/%s.dat", homeDir, hash);
    }

    // readKey checks if the given key is pressed in the GLFW window.
    boolean readKey(long window, int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    // readKeys returns an array representing the state of 8 buttons.
    boolean[] readKeys(long window, boolean turbo) {
        boolean[] result = new boolean[8];
        result[Controller.ButtonA] = readKey(window, GLFW.GLFW_KEY_Z) || (turbo && readKey(window, GLFW.GLFW_KEY_A));
        result[Controller.ButtonB] = readKey(window, GLFW.GLFW_KEY_X) || (turbo && readKey(window, GLFW.GLFW_KEY_S));
        result[Controller.ButtonSelect] = readKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        result[Controller.ButtonStart] = readKey(window, GLFW.GLFW_KEY_ENTER);
        result[Controller.ButtonUp] = readKey(window, GLFW.GLFW_KEY_UP);
        result[Controller.ButtonDown] = readKey(window, GLFW.GLFW_KEY_DOWN);
        result[Controller.ButtonLeft] = readKey(window, GLFW.GLFW_KEY_LEFT);
        result[Controller.ButtonRight] = readKey(window, GLFW.GLFW_KEY_RIGHT);
        return result;
    }

    // readJoystick returns an array representing the state of 8 buttons from the joystick.
    boolean[] readJoystick(int joy, boolean turbo) {
        boolean[] result = new boolean[8];
        if (!GLFW.glfwJoystickPresent(joy)) {
            return result;
        }
        String joyname = GLFW.glfwGetJoystickName(joy);
        float[] axes = GLFW.glfwGetJoystickAxes(joy).array();
        byte[] buttons = GLFW.glfwGetJoystickButtons(joy).array();
        if ("PLAYSTATION(R)3 Controller".equals(joyname)) {
            result[Controller.ButtonA] = (buttons[14] == 1) || (turbo && (buttons[2] == 1));
            result[Controller.ButtonB] = (buttons[13] == 1) || (turbo && (buttons[3] == 1));
            result[Controller.ButtonSelect] = (buttons[0] == 1);
            result[Controller.ButtonStart] = (buttons[3] == 1);
            result[Controller.ButtonUp] = (buttons[4] == 1) || (axes[1] < -0.5f);
            result[Controller.ButtonDown] = (buttons[6] == 1) || (axes[1] > 0.5f);
            result[Controller.ButtonLeft] = (buttons[7] == 1) || (axes[0] < -0.5f);
            result[Controller.ButtonRight] = (buttons[5] == 1) || (axes[0] > 0.5f);
            return result;
        }
        if (buttons.length < 8) {
            return result;
        }
        result[Controller.ButtonA] = (buttons[0] == 1) || (turbo && (buttons[2] == 1));
        result[Controller.ButtonB] = (buttons[1] == 1) || (turbo && (buttons[3] == 1));
        result[Controller.ButtonSelect] = (buttons[6] == 1);
        result[Controller.ButtonStart] = (buttons[7] == 1);
        result[Controller.ButtonUp] = axes[1] < -0.5f;
        result[Controller.ButtonDown] = axes[1] > 0.5f;
        result[Controller.ButtonLeft] = axes[0] < -0.5f;
        result[Controller.ButtonRight] = axes[0] > 0.5f;
        return result;
    }

    // joystickReset checks if the joystick reset buttons are pressed.
    boolean joystickReset(int joy) {
        if (!GLFW.glfwJoystickPresent(joy)) {
            return false;
        }
        // todo
        ByteBuffer buttons = GLFW.glfwGetJoystickButtons(joy);
        if (buttons.array().length < 6) {
            return false;
        }
        // todo
        return (buttons.get(4) == 1 && buttons.get(5) == 1);
    }

    // combineButtons returns the logical OR combination of two button arrays.
    boolean[] combineButtons(boolean[] a, boolean[] b) {
        boolean[] result = new boolean[8];
        for (int i = 0; i < 8; i++) {
            result[i] = a[i] || b[i];
        }
        return result;
    }

    String hashFile(String path) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] data = Files.readAllBytes(Paths.get(path));
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    int createTexture() {
        // todo
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return texture;
    }


    ByteBuffer convertImageToByteBuffer(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        buffer.order(ByteOrder.nativeOrder());
        for (int pixel : pixels) {
            // Convert ARGB to RGBA
            byte a = (byte) ((pixel >> 24) & 0xFF);
            byte r = (byte) ((pixel >> 16) & 0xFF);
            byte g = (byte) ((pixel >> 8) & 0xFF);
            byte b = (byte) (pixel & 0xFF);
            buffer.put(r);
            buffer.put(g);
            buffer.put(b);
            buffer.put(a);
        }
        buffer.flip();
        return buffer;
    }

    // copyImage creates a copy of the given image.
    BufferedImage copyImage(BufferedImage src) {
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dst;
    }


}

