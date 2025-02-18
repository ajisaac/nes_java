package co.aisaac.nes_java;


import java.io.IOException;

public class GameView {

    private final co.aisaac.nes_java.cpu.Console console;

    GameView(String path) throws IOException {
        console = new co.aisaac.nes_java.cpu.Console(path);
    }

    public void update(long now, long delta) {
        if (delta > 1) {
            delta = 0;
        }

        updateControllers();
        console.stepSeconds(delta);

    }

    private void updateControllers() {

    }
}
