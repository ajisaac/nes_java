module co.aisaac.nes_java {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.aisaac.nes_java to javafx.fxml;
    exports co.aisaac.nes_java;
}