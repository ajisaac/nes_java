module co.aisaac.nes_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;


    opens co.aisaac.nes_java to javafx.fxml;
    exports co.aisaac.nes_java;
}