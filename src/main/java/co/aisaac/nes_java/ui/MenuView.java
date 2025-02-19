package co.aisaac.nes_java.ui;

import java.io.File;
import java.util.List;
import java.util.Locale;

// MenuView class as translated from Golang.
public class MenuView implements View {

    public Director director;
    public List<String> paths;
    public Texture texture;
    public int nx, ny, i, j;
    public int scroll;
    public double t;
    public boolean[] buttons = new boolean[8];
    public double[] times = new double[8];
    public String typeBuffer;
    public double typeTime;

    // UI Constants as in the original Golang code
    final class UIConstants {
        public static final int border = 10;
        public static final int margin = 10;
        public static final double initialDelay = 0.3;
        public static final double repeatDelay = 0.1;
        public static final double typeDelay = 0.5;
    }

    public MenuView() {
        // Default constructor.
    }

    public static View NewMenuView(Director director, List<String> paths) {
        MenuView view = new MenuView();
        view.director = director;
        view.paths = paths;
        view.texture = new Texture();
        return view;
    }

    public void checkButtons() {
//        GLFWWindow window = this.director.window;
//        boolean[] k1 = Utils.readKeys(window, false);
//        boolean[] j1 = Utils.readJoystick(glfw.Joystick1, false);
//        boolean[] j2 = Utils.readJoystick(glfw.Joystick2, false);
//        boolean[] buttonsCombined = Utils.combineButtons(Utils.combineButtons(j1, j2), k1);
//        double now = glfw.GetTime();
//        for (int i = 0; i < buttonsCombined.length; i++) {
//            if (buttonsCombined[i] && !this.buttons[i]) {
//                this.times[i] = now + UIConstants.initialDelay;
//                this.onPress(i);
//            } else if (!buttonsCombined[i] && this.buttons[i]) {
//                this.onRelease(i);
//            } else if (buttonsCombined[i] && now >= this.times[i]) {
//                this.times[i] = now + UIConstants.repeatDelay;
//                this.onPress(i);
//            }
//        }
//        this.buttons = buttonsCombined;
    }

    public void onPress(int index) {
//        switch (index) {
//            case nes.ButtonUp:
//                this.j--;
//                break;
//            case nes.ButtonDown:
//                this.j++;
//                break;
//            case nes.ButtonLeft:
//                this.i--;
//                break;
//            case nes.ButtonRight:
//                this.i++;
//                break;
//            default:
//                return;
//        }
//        this.t = glfw.GetTime();
    }

    public void onRelease(int index) {
//        switch (index) {
//            case nes.ButtonStart:
//                this.onSelect();
//                break;
//            default:
//                return;
//        }
    }

    public void onSelect() {
//        int index = this.nx * (this.j + this.scroll) + this.i;
//        if (index >= this.paths.size()) {
//            return;
//        }
//        this.director.PlayGame(this.paths.get(index));
    }

//    public void onChar(GLFWWindow window, char ch) {
//        double now = glfw.GetTime();
//        if (now > this.typeTime) {
//            this.typeBuffer = "";
//        }
//        this.typeTime = now + UIConstants.typeDelay;
//         Append new character and convert to lower case.
//        this.typeBuffer = (this.typeBuffer == null ? "" : this.typeBuffer).toLowerCase(Locale.ENGLISH) + Character.toString(ch).toLowerCase(Locale.ENGLISH);
//        for (int index = 0; index < this.paths.size(); index++) {
//            String p = this.paths.get(index);
//            File f = new File(p);
//            String name = f.getName().toLowerCase(Locale.ENGLISH);
//            if (name.compareTo(this.typeBuffer) >= 0) {
//                this.highlight(index);
//                return;
//            }
//        }
//    }

    public void highlight(int index) {
        this.scroll = index / this.nx - (this.ny - 1) / 2;
        this.clampScroll(false);
        this.i = index % this.nx;
        this.j = (index - this.i) / this.nx - this.scroll;
    }

    public void Enter() {
        // In real GL, ClearColor would set the clear color.
        // Here we simulate by a no-op.
        // gl.ClearColor(0.333, 0.333, 0.333, 1) is omitted because our gl stub does not support it.
//        this.director.SetTitle("Select Game");
//        this.director.window.SetCharCallback(new CharCallback() {
//            public void invoke(GLFWWindow window, char ch) {
//                onChar(window, ch);
//            }
//        });
    }

    public void Exit() {
//        this.director.window.SetCharCallback(null);
    }

    public void Update(double t, double dt) {
        /*
        this.checkButtons();
        this.texture.Purge();
        GLFWWindow window = this.director.window;
        int[] fbSize = window.GetFramebufferSize();
        int w = fbSize[0];
        int h = fbSize[1];
        int sx = 256 + UIConstants.margin * 2;
        int sy = 240 + UIConstants.margin * 2;
        int nx = (w - UIConstants.border * 2) / sx;
        int ny = (h - UIConstants.border * 2) / sy;
        int ox = (w - nx * sx) / 2 + UIConstants.margin;
        int oy = (h - ny * sy) / 2 + UIConstants.margin;
        if (nx < 1) {
            nx = 1;
        }
        if (ny < 1) {
            ny = 1;
        }
        this.nx = nx;
        this.ny = ny;
        this.clampSelection();
        gl.PushMatrix();
        gl.Ortho(0, (double) w, (double) h, 0, -1, 1);
        this.texture.Bind();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                float x = (float) (ox + i * sx);
                float y = (float) (oy + j * sy);
                int index = nx * (j + this.scroll) + i;
                if (index >= this.paths.size() || index < 0) {
                    continue;
                }
                String pathStr = this.paths.get(index);
                float[] lookup = this.texture.Lookup(pathStr);
                float tx = lookup[0];
                float ty = lookup[1];
                float tw = lookup[2];
                float th = lookup[3];
                UIUtil.drawThumbnail(x, y, tx, ty, tw, th);
            }
        }
        this.texture.Unbind();
        if (((int) ((t - this.t) * 4)) % 2 == 0) {
            float x = (float) (ox + this.i * sx);
            float y = (float) (oy + this.j * sy);
            UIUtil.drawSelection(x, y, 8, 4);
        }
        gl.PopMatrix();*/
    }

    public void clampSelection() {
        if (this.i < 0) {
            this.i = this.nx - 1;
        }
        if (this.i >= this.nx) {
            this.i = 0;
        }
        if (this.j < 0) {
            this.j = 0;
            this.scroll--;
        }
        if (this.j >= this.ny) {
            this.j = this.ny - 1;
            this.scroll++;
        }
        this.clampScroll(true);
    }

    public void clampScroll(boolean wrap) {
        int n = this.paths.size();
        int rows = n / this.nx;
        if (n % this.nx > 0) {
            rows++;
        }
        int maxScroll = rows - this.ny;
        if (this.scroll < 0) {
            if (wrap) {
                this.scroll = maxScroll;
                this.j = this.ny - 1;
            } else {
                this.scroll = 0;
                this.j = 0;
            }
        }
        if (this.scroll > maxScroll) {
            if (wrap) {
                this.scroll = 0;
                this.j = 0;
            } else {
                this.scroll = maxScroll;
                this.j = this.ny - 1;
            }
        }
    }

    // UI utility functions that were package-level in Go.
    public static void drawThumbnail(float x, float y, float tx, float ty, float tw, float th) {
        /*
        float sx = x + 4;
        float sy = y + 4;
        gl.Disable(gl.TEXTURE_2D);
        gl.Color3f(0.2f, 0.2f, 0.2f);
        gl.Begin(gl.QUADS);
        gl.Vertex2f(sx, sy);
        gl.Vertex2f(sx + 256, sy);
        gl.Vertex2f(sx + 256, sy + 240);
        gl.Vertex2f(sx, sy + 240);
        gl.End();
        gl.Enable(gl.TEXTURE_2D);
        gl.Color3f(1f, 1f, 1f);
        gl.Begin(gl.QUADS);
        gl.TexCoord2f(tx, ty);
        gl.Vertex2f(x, y);
        gl.TexCoord2f(tx + tw, ty);
        gl.Vertex2f(x + 256, y);
        gl.TexCoord2f(tx + tw, ty + th);
        gl.Vertex2f(x + 256, y + 240);
        gl.TexCoord2f(tx, ty + th);
        gl.Vertex2f(x, y + 240);
        gl.End();
         */
    }

    public static void drawSelection(float x, float y, float p, float w) {
        /*
        gl.LineWidth(w);
        gl.Begin(gl.LINE_STRIP);
        gl.Vertex2f(x - p, y - p);
        gl.Vertex2f(x + 256 + p, y - p);
        gl.Vertex2f(x + 256 + p, y + 240 + p);
        gl.Vertex2f(x - p, y + 240 + p);
        gl.Vertex2f(x - p, y - p);
        gl.End();
         */
    }
}
