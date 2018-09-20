package JISA.GUI;

import JISA.Util;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class GUI extends Application {

    public static void showError(String title, String header, String text, ClickHandler onOkay) {

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(text);
            alert.showAndWait();

            if (onOkay != null) {

                Thread t = new Thread(() -> {
                    try {
                        onOkay.click();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                });
                t.start();
            }
        });

    }

    public static void showError(String title, String header, String text) {
        showError(title, header, text, null);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }
}
