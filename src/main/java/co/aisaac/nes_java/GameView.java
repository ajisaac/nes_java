package co.aisaac.nes_java;


public class GameView {

    private final Console console;

    GameView(String path) {
        console = new Console(path);
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
