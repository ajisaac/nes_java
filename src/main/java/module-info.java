module co.aisaac.nesjava {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.aisaac.nesjava to javafx.fxml;
    exports co.aisaac.nesjava;
}