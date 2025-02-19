package co.aisaac.nes_java;


import java.io.IOException;

import static co.aisaac.nes_java.Console.NewConsole;

public class GameView {

    private final Console console;

    GameView(String path) throws IOException {
        try {
            console = NewConsole(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void update(long now, long delta) {
        if (delta > 1) {
            delta = 0;
        }

        updateControllers();
//        console.stepSeconds(delta);

    }

    private void updateControllers() {

    }
}
