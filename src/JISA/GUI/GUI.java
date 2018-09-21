package JISA.GUI;

import JISA.Util;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;

public class GUI extends Application {

    private static boolean done = false;
    private static File    file;

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

    public static String saveFileSelect() {

        file = null;

        Platform.runLater(() -> {
            file = null;
            FileChooser chooser = new FileChooser();
            file = chooser.showSaveDialog(new Stage());
        });

        while (file == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }

        return file.getAbsolutePath();

    }

    public static Object create(String fxmlFile) throws IOException {

        FXMLLoader loader     = new FXMLLoader(GUI.class.getResource(fxmlFile));
        Parent     root       = loader.load();
        Object     controller = loader.getController();
        Scene      scene      = new Scene(root);

        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setScene(scene);
        });

        return controller;

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

}
