module co.aisaac.nes_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;


    opens co.aisaac.nes_java to javafx.fxml;
    exports co.aisaac.nes_java;
    exports co.aisaac.nes_java.apu;
    opens co.aisaac.nes_java.apu to javafx.fxml;
    exports co.aisaac.nes_java.cpu;
    opens co.aisaac.nes_java.cpu to javafx.fxml;
    exports co.aisaac.nes_java.filter;
    opens co.aisaac.nes_java.filter to javafx.fxml;
    exports co.aisaac.nes_java.memory;
    opens co.aisaac.nes_java.memory to javafx.fxml;
}