package co.aisaac.nes_java.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Director {
//    GLFWWindow window;
    Audio audio;
    View view;
    View menuView;
    double timestamp;

//    public Director(GLFWWindow window, Audio audio) {
        // director := Director{}
        // director.window = window
        // director.audio = audio
//        this.window = window;
//        this.audio = audio;
//    }

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

    public void Start(String[] paths) {
//        this.menuView = new MenuView(this, paths);
//        if (paths.length == 1) {
//            this.PlayGame(paths[0]);
//        } else {
//            this.ShowMenu();
//        }
//        this.Run();
    }

    public void Run() {
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

