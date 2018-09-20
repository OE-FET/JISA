package JISA.GUI;

import JISA.Util;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class GUI extends Application {

    private static boolean done = false;

    public static void error(String title, String header, String text) {

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(text);
            alert.showAndWait();
            done = true;
        });

        while (!done) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }
}
