package co.aisaac.nes_java.ui;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.imageio.ImageIO;

public class Texture {
    public static final int textureSize = 4096;
    public static final int textureDim = textureSize / 256;
    public static final int textureCount = textureDim * textureDim;

    public int texture;
    public Map<String, Integer> lookup;
    public String[] reverse = new String[textureCount];
    public int[] access = new int[textureCount];
    public int counter;
    public BlockingQueue<String> ch;

    public static Texture NewTexture() {
        int texture = Util.createTexture();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
                textureSize, textureSize,
                0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glBindTexture(GL_TEXTURE_2D, 0);
        Texture t = new Texture();
        t.texture = texture;
        t.lookup = new HashMap<String, Integer>();
        t.ch = new ArrayBlockingQueue<String>(1024);
        return t;
    }

    public void Purge() {
        while (true) {
            String path = ch.poll();
            if (path != null) {
                lookup.remove(path);
            } else {
                return;
            }
        }
    }

    public void Bind() {
        glBindTexture(GL_TEXTURE_2D, this.texture);
    }

    public void Unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public float[] Lookup(String path) {
        if (lookup.containsKey(path)) {
            return this.coord(lookup.get(path));
        } else {
            return this.coord(this.load(path));
        }
    }

    public void mark(int index) {
        this.counter++;
        this.access[index] = this.counter;
    }

    public int lru() {
        int minIndex = 0;
        int minValue = this.counter + 1;
        for (int i = 0; i < access.length; i++) {
            int n = access[i];
            if (n < minValue) {
                minIndex = i;
                minValue = n;
            }
        }
        return minIndex;
    }

    public float[] coord(int index) {
        float x = (float) (index % textureDim) / textureDim;
        float y = (float) (index / textureDim) / textureDim;
        float dx = 1.0f / textureDim;
        float dy = dx * 240f / 256f;
        return new float[] { x, y, dx, dy };
    }

    public int load(String path) {
        int index = this.lru();
        lookup.remove(this.reverse[index]);
        this.mark(index);
        lookup.put(path, index);
        this.reverse[index] = path;
        int x = (index % textureDim) * 256;
        int y = (index / textureDim) * 256;
        BufferedImage im = Util.copyImage(this.loadThumbnail(path));
        int width = im.getWidth();
        int height = im.getHeight();
        ByteBuffer buffer = Util.convertImageData(im);
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height,
                GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        return index;
    }

    public BufferedImage loadThumbnail(String romPath) {
        File romFile = new File(romPath);
        String name = romFile.getName();
        if (name.endsWith(".nes")) {
            name = name.substring(0, name.length() - 4);
        }
        name = name.replace("_", " ");
        name = Util.toTitleCase(name);
        BufferedImage im = Util.CreateGenericThumbnail(name);
        String hash;
        try {
            hash = Util.hashFile(romPath);
        } catch (Exception e) {
            return im;
        }
        String filename = Util.thumbnailPath(hash);
        File thumbFile = new File(filename);
        if (!thumbFile.exists()) {
            new Thread(() -> {
                this.downloadThumbnail(romPath, hash);
            }).start();
            return im;
        } else {
            BufferedImage thumbnail = null;
            try {
                thumbnail = Util.loadPNG(filename);
            } catch (Exception e) {
                return im;
            }
            return thumbnail;
        }
    }

    public Exception downloadThumbnail(String romPath, String hash) {
        String url = Util.thumbnailURL(hash);
        String filename = Util.thumbnailPath(hash);
        File file = new File(filename);
        File dir = file.getParentFile();

        HttpURLConnection conn = null;
        InputStream input = null;
        FileOutputStream output = null;
        try {
            URL downloadUrl = new URL(url);
            conn = (HttpURLConnection) downloadUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            input = conn.getInputStream();

            if (dir != null && !dir.exists()) {
                if (!dir.mkdirs()) {
                    return new IOException("Failed to create directories: " + dir.getAbsolutePath());
                }
            }

            output = new FileOutputStream(file);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            this.ch.offer(romPath);

            return null;
        } catch (Exception e) {
            return e;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
