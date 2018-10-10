package JISA.GUI;

import JISA.Addresses.InstrumentAddress;
import JISA.GUI.FXML.BrowseVISA;
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

    private static boolean   done = false;
    private static File      file;
    private static Semaphore s;

    public static void errorAlert(String title, String header, String text) {
        alert(Alert.AlertType.ERROR, title, header, text);
    }

    public static void infoAlert(String title, String header, String text) {
        alert(Alert.AlertType.INFORMATION, title, header, text);
    }

    public static void warningAlert(String title, String header, String text) {
        alert(Alert.AlertType.WARNING, title, header, text);
    }

    private static void alert(final Alert.AlertType type, final String title, final String header, final String text) {
        final Semaphore semaphore = new Semaphore(0);
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

    public static InstrumentAddress browseVISA() {

        AtomicReference<InstrumentAddress> ref       = new AtomicReference<>();
        BrowseVISA                         browse    = BrowseVISA.create("Find Instrument");
        Semaphore                          semaphore = new Semaphore(0);

        if (browse == null) {
            return null;
        }

        Platform.runLater(() -> browse.search((a) -> {
            ref.set(a);
            semaphore.release();
        }));

        try {
            semaphore.acquire();
        } catch (InterruptedException ignored) {
        }

        return ref.get();

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    public static class App extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
            s.release();
        }

    }


    public static void startGUI() {

        s = new Semaphore(0);

        Thread t = new Thread(() -> Application.launch(App.class));

        t.start();
        try {
            s.acquire();
        } catch (InterruptedException ignored) {

        }

    }

}
