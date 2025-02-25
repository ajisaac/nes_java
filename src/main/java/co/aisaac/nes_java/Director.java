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
        this.hash = hashFile(path);

        if (hash == null) {
            RuntimeException runtimeException = new RuntimeException("Something went wrong.");
            runtimeException.printStackTrace();
            throw runtimeException;
        }

        this.console = new Console(path);
        this.texture = createTexture();
        this.audio = new Audio();

        if (this.console.cartridge.battery != 0) {
            int /*byte*/[] sram;
            try {
                sram = readSRAM(sramPath(this.hash, -1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.console.cartridge.SRAM = sram;
        }
    }

    public void run() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            double timestamp1 = GLFW.glfwGetTime();
            double dt = timestamp1 - this.timestamp;
            this.timestamp = timestamp1;

            if (dt > 1) {
                dt = 0;
            }

            boolean turbo = (console.PPU.Frame % 6) < 3;
            boolean[] k1 = readKeys(window, turbo);
            boolean[] j1 = readJoystick(GLFW.GLFW_JOYSTICK_1, turbo);
            boolean[] j2 = readJoystick(GLFW.GLFW_JOYSTICK_2, turbo);
            console.SetButtons1(combineButtons(k1, j1));
            console.SetButtons2(j2);
            console.stepSeconds(dt);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
            BufferedImage im = this.console.Buffer();
            int width = im.getWidth();
            int height = im.getHeight();
            ByteBuffer buffer = convertImageToByteBuffer(im);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height,
                    0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            drawBuffer(window);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            if (this.record) {
                if (this.frames == null) {
                    this.frames = new ArrayList<>();
                }
                this.frames.add(copyImage(this.console.Buffer()));
            }

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GLFW.glfwSwapBuffers(window);

            GLFW.glfwPollEvents();
        }
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


    void save(int snapshot) {
//        if (this.console.cartridge.battery != 0) {
//            try {
                // todo
//                writeSRAM(sramPath(this.hash, snapshot), this.console.cartridge.SRAM);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    // writeSRAM writes the SRAM byte array to the specified file.
    // todo
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

    int /*byte*/[] readSRAM(String filename) throws IOException {
//        File file = new File(filename);
//        FileInputStream fis = new FileInputStream(file);
//        DataInputStream dis = new DataInputStream(fis);
        int /*byte*/[] sram = new int /*byte*/[0x2000];
        // todo
//        int bytesRead = dis.read(sram);
//        if (bytesRead != sram.length) {
//            dis.close();
//            fis.close();
//            throw new IOException("Failed to read full SRAM data");
//        }
//        dis.close();
//        fis.close();
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
        float[] axes = GLFW.glfwGetJoystickAxes(joy).array();
        byte[] buttons = GLFW.glfwGetJoystickButtons(joy).array();
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

    // combineButtons returns the logical OR combination of two button arrays.
    boolean[] combineButtons(boolean[] a, boolean[] b) {
        boolean[] result = new boolean[8];
        for (int i = 0; i < 8; i++) {
            result[i] = a[i] || b[i];
        }
        return result;
    }

    String hashFile(String path) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] data = Files.readAllBytes(Paths.get(path));
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (int /*byte*/ b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
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
            // TODO
            // Convert ARGB to RGBA
            int /*byte*/ a = (int /*byte*/) ((pixel >> 24) & 0xFF);
            int /*byte*/ r = (int /*byte*/) ((pixel >> 16) & 0xFF);
            int /*byte*/ g = (int /*byte*/) ((pixel >> 8) & 0xFF);
            int /*byte*/ b = (int /*byte*/) (pixel & 0xFF);
            buffer.put((byte) r);
            buffer.put((byte) g);
            buffer.put((byte) b);
            buffer.put((byte) a);
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

