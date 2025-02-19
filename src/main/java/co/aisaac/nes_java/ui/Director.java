package co.aisaac.nes_java.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class Director {
    long window;
    Audio audio;
    View view;
    View menuView;
    double timestamp;

    public Director(long window) {
        this.window = window;

    }

    public void SetTitle(String title) {
//        this.window.setTitle(title);
    }

    public void SetView(View view) {
        if (this.view != null) {
            this.view.Exit();
        }
        this.view = view;
        if (this.view != null) {
            this.view.Enter();
        }
//        this.timestamp = GLFW.getTime();
    }

    // Equivalent to: func (d *Director) Step()
    public void Step() {
//        gl.Clear(gl.COLOR_BUFFER_BIT);
//        double currentTimestamp = GLFW.getTime();
//        double dt = currentTimestamp - this.timestamp;
//        this.timestamp = currentTimestamp;
//        if (this.view != null) {
//            this.view.Update(currentTimestamp, dt);
//        }
    }

    public void start(String[] paths) {
//        this.menuView = new MenuView(this, paths);
//        if (paths.length == 1) {
//            this.PlayGame(paths[0]);
//        } else {
//            this.ShowMenu();
//        }
//        this.Run();
    }

    public void run() {
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
//        while (!this.window.ShouldClose()) {
//            this.Step();
//            this.window.SwapBuffers();
//            GLFW.PollEvents();
//        }
//        this.SetView(null);
    }

    // Equivalent to: func (d *Director) PlayGame(path string)
    public void PlayGame(String path) {
        String hash = null;
        try {
            hash = hashFile(path);
        } catch (Exception err) {
            System.err.println(err);
            System.exit(1);
        }
//        nes.Console console = null;
//        try {
//            console = nes.NewConsole(path);
//        } catch (Exception err) {
//            System.err.println(err);
//            System.exit(1);
//        }
//        this.SetView(new GameView(this, console, path, hash));
    }

    // Equivalent to: func (d *Director) ShowMenu()
    public void ShowMenu() {
        this.SetView(this.menuView);
    }

    public static String hashFile(String path) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] data = Files.readAllBytes(Paths.get(path));
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

