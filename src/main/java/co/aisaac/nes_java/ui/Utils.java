package co.aisaac.nes_java.ui;

//import org.lwjgl.glfw.GLFW;
//import org.lwjgl.opengl.GL11;

import co.aisaac.nes_java.Controller;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
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
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class Utils {

    // Global variable equivalent to var homeDir string
    public static String homeDir;

    static {
        // init function equivalent
        try {
            // In Java, we get the current user's home directory via system property.
            homeDir = System.getProperty("user.home");
        } catch (Exception err) {
            System.err.println(err);
            System.exit(1);
        }
    }

    // thumbnailURL returns the URL for a given hash.
    public static String thumbnailURL(String hash) {
        return "http://www.michaelfogleman.com/static/nes/" + hash + ".png";
    }

    // thumbnailPath returns the local thumbnail path for a given hash.
    public static String thumbnailPath(String hash) {
        return homeDir + "/.nes/thumbnail/" + hash + ".png";
    }

    // sramPath returns the sram file path based on hash and snapshot
    public static String sramPath(String hash, int snapshot) {
        if (snapshot >= 0) {
            return String.format("%s/.nes/sram/%s-%d.dat", homeDir, hash, snapshot);
        }
        return String.format("%s/.nes/sram/%s.dat", homeDir, hash);
    }

    // savePath returns the save file path based on hash and snapshot
    public static String savePath(String hash, int snapshot) {
        if (snapshot >= 0) {
            return String.format("%s/.nes/save/%s-%d.dat", homeDir, hash, snapshot);
        }
        return String.format("%s/.nes/save/%s.dat", homeDir, hash);
    }

    // readKey checks if the given key is pressed in the GLFW window.
//    public static boolean readKey(long window, int key) {
//        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
//    }

    // readKeys returns an array representing the state of 8 buttons.
//    public static boolean[] readKeys(long window, boolean turbo) {
//        boolean[] result = new boolean[8];
//        result[Controller.ButtonA] = readKey(window, GLFW.GLFW_KEY_Z) || (turbo && readKey(window, GLFW.GLFW_KEY_A));
//        result[Controller.ButtonB] = readKey(window, GLFW.GLFW_KEY_X) || (turbo && readKey(window, GLFW.GLFW_KEY_S));
//        result[Controller.ButtonSelect] = readKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
//        result[Controller.ButtonStart] = readKey(window, GLFW.GLFW_KEY_ENTER);
//        result[Controller.ButtonUp] = readKey(window, GLFW.GLFW_KEY_UP);
//        result[Controller.ButtonDown] = readKey(window, GLFW.GLFW_KEY_DOWN);
//        result[Controller.ButtonLeft] = readKey(window, GLFW.GLFW_KEY_LEFT);
//        result[Controller.ButtonRight] = readKey(window, GLFW.GLFW_KEY_RIGHT);
//        return result;
//    }

    // readJoystick returns an array representing the state of 8 buttons from the joystick.
//    public static boolean[] readJoystick(int joy, boolean turbo) {
//        boolean[] result = new boolean[8];
//        if (!GLFW.glfwJoystickPresent(joy)) {
//            return result;
//        }
        // todo
//        String joyname = GLFW.glfwGetJoystickName(joy);
//        float[] axes = GLFW.glfwGetJoystickAxes(joy);
//        byte[] buttons = GLFW.glfwGetJoystickButtons(joy);
//        String joyname = "";
//        float[] axes = new float[1];
//        byte[] buttons = new byte[]{3};
//        if ("PLAYSTATION(R)3 Controller".equals(joyname)) {
//            result[Controller.ButtonA] = (buttons[14] == 1) || (turbo && (buttons[2] == 1));
//            result[Controller.ButtonB] = (buttons[13] == 1) || (turbo && (buttons[3] == 1));
//            result[Controller.ButtonSelect] = (buttons[0] == 1);
//            result[Controller.ButtonStart] = (buttons[3] == 1);
//            result[Controller.ButtonUp] = (buttons[4] == 1) || (axes[1] < -0.5f);
//            result[Controller.ButtonDown] = (buttons[6] == 1) || (axes[1] > 0.5f);
//            result[Controller.ButtonLeft] = (buttons[7] == 1) || (axes[0] < -0.5f);
//            result[Controller.ButtonRight] = (buttons[5] == 1) || (axes[0] > 0.5f);
//            return result;
//        }
//        if (buttons.length < 8) {
//            return result;
//        }
//        result[Controller.ButtonA] = (buttons[0] == 1) || (turbo && (buttons[2] == 1));
//        result[Controller.ButtonB] = (buttons[1] == 1) || (turbo && (buttons[3] == 1));
//        result[Controller.ButtonSelect] = (buttons[6] == 1);
//        result[Controller.ButtonStart] = (buttons[7] == 1);
//        result[Controller.ButtonUp] = axes[1] < -0.5f;
//        result[Controller.ButtonDown] = axes[1] > 0.5f;
//        result[Controller.ButtonLeft] = axes[0] < -0.5f;
//        result[Controller.ButtonRight] = axes[0] > 0.5f;
//        return result;
//    }

    // joystickReset checks if the joystick reset buttons are pressed.
    public static boolean joystickReset(int joy) {
//        if (!GLFW.glfwJoystickPresent(joy)) {
//            return false;
//        }
//        byte[] buttons = GLFW.glfwGetJoystickButtons(joy);
//        if (buttons.length < 6) {
//            return false;
//        }
//        return (buttons[4] == 1 && buttons[5] == 1);
        return false;
    }

    // combineButtons returns the logical OR combination of two button arrays.
    public static boolean[] combineButtons(boolean[] a, boolean[] b) {
        boolean[] result = new boolean[8];
        for (int i = 0; i < 8; i++) {
            result[i] = a[i] || b[i];
        }
        return result;
    }

    // hashFile computes the MD5 hash of the file at the given path and returns it as a hex string.
    public static String hashFile(String path) throws IOException, NoSuchAlgorithmException {
        byte[] data = Files.readAllBytes(Paths.get(path));
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(data);
        Formatter formatter = new Formatter(Locale.US);
        for (byte b : hashBytes) {
            formatter.format("%02x", b);
        }
        String ret = formatter.toString();
        formatter.close();
        return ret;
    }

    public static int createTexture() {
        // todo
//        int texture = GL11.glGenTextures();
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
//        return texture;
        return 0;
    }

    // setTexture sets the texture image data based on the provided BufferedImage.
    public static void setTexture(BufferedImage im) {
        int width = im.getWidth();
        int height = im.getHeight();
        ByteBuffer buffer = convertImageToByteBuffer(im);
//        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height,
//                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    // Helper function to convert a BufferedImage to a ByteBuffer in RGBA order.
    private static ByteBuffer convertImageToByteBuffer(BufferedImage image) {
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
    public static BufferedImage copyImage(BufferedImage src) {
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dst;
    }

    // loadPNG loads a PNG image from the specified path.
    public static BufferedImage loadPNG(String path) throws IOException {
        File file = new File(path);
        return ImageIO.read(file);
    }

    // savePNG saves the image as a PNG file to the specified path.
    public static void savePNG(String path, BufferedImage im) throws IOException {
        File file = new File(path);
        ImageIO.write(im, "png", file);
    }

    // saveGIF saves an animated GIF made from the given frames to the specified path.
    public static void saveGIF(String path, List<BufferedImage> frames) throws IOException {
        // Build palette from NES.Palette (assuming NES.Palette is a Color[]).
//        Color[] nesPalette = NES.Palette;
//        int paletteSize = nesPalette.length;
//        byte[] r = new byte[paletteSize];
//        byte[] g = new byte[paletteSize];
//        byte[] b = new byte[paletteSize];
//        for (int i = 0; i < paletteSize; i++) {
//            Color c = nesPalette[i];
//            r[i] = (byte) c.getRed();
//            g[i] = (byte) c.getGreen();
//            b[i] = (byte) c.getBlue();
//        }
//        IndexColorModel colorModel = new IndexColorModel(8, paletteSize, r, g, b);

        // Create a list of framed images after processing (only every 3rd frame).
        List<BufferedImage> gifFrames = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            BufferedImage src = frames.get(i);
            if (i % 3 != 0) {
                continue;
            }
//            BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, colorModel);
//            Graphics2D g2 = dst.createGraphics();
//            g2.drawImage(src, 0, 0, null);
//            g2.dispose();
//            gifFrames.add(dst);
        }
        if (gifFrames.isEmpty()) {
            return;
        }

        // Use GifSequenceWriter to write the animated GIF.
        ImageOutputStream output = new FileImageOutputStream(new File(path));
        // Delay is 5 (assumed to be hundredths of a second - same as Go code).
        GifSequenceWriter writer = new GifSequenceWriter(output, gifFrames.get(0).getType(), 5, true);
        writer.writeToSequence(gifFrames.get(0));
        for (int i = 1; i < gifFrames.size(); i++) {
            writer.writeToSequence(gifFrames.get(i));
        }
        writer.close();
        output.close();
    }

    // screenshot saves the image as a PNG file with an incremental filename.
    public static void screenshot(BufferedImage im) throws IOException {
        for (int i = 0; i < 1000; i++) {
            String path = String.format("%03d.png", i);
            File file = new File(path);
            if (!file.exists()) {
                savePNG(path, im);
                return;
            }
        }
    }

    // animation saves an animated GIF from the frames with an incremental filename.
    public static void animation(List<BufferedImage> frames) throws IOException {
        for (int i = 0; i < 1000; i++) {
            String path = String.format("%03d.gif", i);
            File file = new File(path);
            if (!file.exists()) {
                saveGIF(path, frames);
                return;
            }
        }
    }

    // writeSRAM writes the SRAM byte array to the specified file.
    public static void writeSRAM(String filename, byte[] sram) throws IOException {
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

    // readSRAM reads SRAM data from the specified file and returns a byte array.
    public static byte[] readSRAM(String filename) throws IOException {
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

    /**
     * GifSequenceWriter class
     * This class provides the functionality to write a sequence of images into an animated GIF.
     * This implementation uses standard ImageIO classes.
     */
    public static class GifSequenceWriter {
        protected ImageOutputStream outputStream;
        protected javax.imageio.ImageWriter gifWriter;
        protected javax.imageio.metadata.IIOMetadata imageMetaData;
        protected javax.imageio.ImageWriteParam imageWriteParam;

        /**
         * Creates a new GifSequenceWriter
         *
         * @param outputStream the ImageOutputStream to be written to
         * @param imageType one of the imageTypes specified in BufferedImage
         * @param timeBetweenFramesCS the time between frames in hundredths of a second
         * @param loopContinuously whether the gif should loop repeatedly
         * @throws IOException if I/O error occurs
         */
        public GifSequenceWriter(ImageOutputStream outputStream, int imageType, int timeBetweenFramesCS, boolean loopContinuously) throws IOException {
            this.outputStream = outputStream;
            gifWriter = ImageIO.getImageWritersBySuffix("gif").next();
            imageWriteParam = gifWriter.getDefaultWriteParam();
//            javax.imageio.metadata.IIOMetadata metadata = gifWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(imageType), imageWriteParam);

//            String metaFormatName = metadata.getNativeMetadataFormatName();

//            javax.imageio.metadata.IIOMetadataNode root = (javax.imageio.metadata.IIOMetadataNode) metadata.getAsTree(metaFormatName);
//            javax.imageio.metadata.IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
//            graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
//            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
//            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
//            graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesCS));
//            graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

//            javax.imageio.metadata.IIOMetadataNode appEntensionsNode = getNode(root, "ApplicationExtensions");
//            javax.imageio.metadata.IIOMetadataNode appExtensionNode = new javax.imageio.metadata.IIOMetadataNode("ApplicationExtension");
//            appExtensionNode.setAttribute("applicationID", "NETSCAPE");
//            appExtensionNode.setAttribute("authenticationCode", "2.0");
//            int loop = loopContinuously ? 0 : 1;
//            appExtensionNode.setUserObject(new byte[]{ 0x1, (byte) (loop & 0xFF), (byte) ((loop >> 8) & 0xFF) });
//            appEntensionsNode.appendChild(appExtensionNode);
//            metadata.setFromTree(metaFormatName, root);
//            this.imageMetaData = metadata;
//            gifWriter.setOutput(outputStream);
//            gifWriter.prepareWriteSequence(null);
        }

        private static javax.imageio.metadata.IIOMetadataNode getNode(javax.imageio.metadata.IIOMetadataNode rootNode, String nodeName) {
            int nNodes = rootNode.getLength();
            for (int i = 0; i < nNodes; i++) {
                if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                    return (javax.imageio.metadata.IIOMetadataNode) rootNode.item(i);
                }
            }
            javax.imageio.metadata.IIOMetadataNode node = new javax.imageio.metadata.IIOMetadataNode(nodeName);
            rootNode.appendChild(node);
            return node;
        }

        public void writeToSequence(BufferedImage img) throws IOException {
            gifWriter.writeToSequence(new javax.imageio.IIOImage(img, null, imageMetaData), imageWriteParam);
        }

        public void close() throws IOException {
            gifWriter.endWriteSequence();
        }
    }
}
