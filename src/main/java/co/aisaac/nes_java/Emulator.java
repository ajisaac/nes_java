package co.aisaac.nes_java;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Emulator extends Application {

    final String path = "/Users/aaron/Code/personal/nesjava/src/main/resources/data/Nintendo/Castlevania.zip";

    @Override
    public void start(Stage stage) throws IOException {

        Canvas canvas = new Canvas();
        VBox vBox = new VBox(canvas);
        Scene scene = new Scene(vBox, 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        GameView gameView = new GameView(path);

        long timestamp = System.currentTimeMillis();
        while (true) {
            long now = System.currentTimeMillis();
            long delta = now - timestamp;
            timestamp = now;

            gameView.update(now, delta);

            // do drawing
        }
    }

    public static void main(String[] args) {
        launch();
    }
}