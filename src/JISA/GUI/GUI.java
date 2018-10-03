package JISA.GUI;

import JISA.Util;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class GUI extends Application {

    private static boolean done = false;
    private static File    file;

    public static void errorAlert(String title, String header, String text) {
        alert(Alert.AlertType.ERROR, title, header, text);
    }

    public static void infoAlert(String title, String header, String text) {
        alert(Alert.AlertType.INFORMATION, title, header, text);
    }

    public static void warningAlert(String title, String header, String text) {
        alert(Alert.AlertType.WARNING, title, header, text);
    }

    private static void alert(Alert.AlertType type, String title, String header, String text) {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(text);
            alert.showAndWait();
            semaphore.release();
        });

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Util.exceptionHandler(e);
        }
    }

    public static String saveFileSelect() {

        Semaphore semaphore = new Semaphore(0);

        AtomicReference<File> file = new AtomicReference<>();

        Platform.runLater(() -> {
            FileChooser chooser = new FileChooser();
            file.set(chooser.showSaveDialog(new Stage()));
            semaphore.release();
        });

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Util.exceptionHandler(e);
        }

        return file.get().getAbsolutePath();

    }

    public static boolean confirmWindow(String title, String header, String text) {

        Semaphore                             semaphore = new Semaphore(0);
        AtomicReference<Optional<ButtonType>> response  = new AtomicReference<>();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(text);
            response.set(alert.showAndWait());
            semaphore.release();
        });

        try {
            semaphore.acquire();
            return response.get().get() == ButtonType.OK;
        } catch (InterruptedException e) {
            Util.exceptionHandler(e);
        }

        return false;

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
