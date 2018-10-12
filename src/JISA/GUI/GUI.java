package JISA.GUI;

import JISA.Addresses.InstrumentAddress;
import JISA.Util;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class GUI extends Application {

    private static boolean   done = false;
    private static File      file;
    private static Semaphore s;

    public static void errorAlert(String title, String header, String text, double width) {
        alert(Alert.AlertType.ERROR, title, header, text, width);
    }

    public static void infoAlert(String title, String header, String text, double width) {
        alert(Alert.AlertType.INFORMATION, title, header, text, width);
    }

    public static void warningAlert(String title, String header, String text) {
        alert(Alert.AlertType.WARNING, title, header, text);
    }

    public static void errorAlert(String title, String header, String text) {
        alert(Alert.AlertType.ERROR, title, header, text);
    }

    public static void infoAlert(String title, String header, String text) {
        alert(Alert.AlertType.INFORMATION, title, header, text);
    }

    public static void warningAlert(String title, String header, String text, double width) {
        alert(Alert.AlertType.WARNING, title, header, text, width);
    }

    private static void alert(final Alert.AlertType type, final String title, final String header, final String text) {
        alert(type, title, header, text, 400);
    }

    private static void alert(final Alert.AlertType type, final String title, final String header, final String text, final double width) {
        final Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(text);
            alert.getDialogPane().setMinWidth(width);
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

    public static String[] inputWindow(String title, String header, String message, String... fields) {

        // Reference to take in returned value from the dialog.
        AtomicReference<String[]> toReturn = new AtomicReference<>();

        // Semaphore to make thread wait until we've returned a value.
        Semaphore semaphore = new Semaphore(0);

        // All GUI stuff must be done on the GUI thread.
        Platform.runLater(() -> {

            Dialog<String[]> dialog = new Dialog<>();
            Label img = new Label();
            img.getStyleClass().addAll("choice-dialog", "dialog-pane");
            dialog.setGraphic(img);
            dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            dialog.getDialogPane().setMinWidth(400);
            dialog.setTitle(title);
            dialog.setHeaderText(header);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            VBox list = new VBox();
            list.setSpacing(15);
            list.setPadding(new Insets(15, 15, 15, 15));
            list.getChildren().add(new Label(message));

            ArrayList<TextField> tFields = new ArrayList<>();

            for (String field : fields) {

                HBox box = new HBox();
                box.setAlignment(Pos.CENTER_LEFT);
                box.setSpacing(15);

                Label fieldName = new Label(field.concat(":"));
                fieldName.setAlignment(Pos.CENTER_RIGHT);
                fieldName.setMinWidth(75);
                HBox.setHgrow(fieldName, Priority.NEVER);

                TextField fieldInput = new TextField();
                fieldInput.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(fieldInput, Priority.ALWAYS);

                tFields.add(fieldInput);

                box.getChildren().addAll(fieldName, fieldInput);
                list.getChildren().add(box);

            }

            dialog.getDialogPane().setContent(list);

            dialog.setResultConverter((b) -> {

                if (b != ButtonType.OK) {
                    return null;
                }

                String[] values = new String[tFields.size()];

                for (int i = 0; i < values.length; i++) {
                    values[i] = tFields.get(i).getText();
                }

                return values;

            });

            Optional<String[]> values = dialog.showAndWait();
            toReturn.set(values.orElse(null));
            semaphore.release();

        });

        // Wait for GUI thread stuff to complete (ie semaphore.release(); to be called)
        try {
            semaphore.acquire();
        } catch (InterruptedException ignored) {

        }

        // Return whatever value has been set in the reference
        return toReturn.get();

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
        BrowseVISA                         browse    = new BrowseVISA("Find Instrument");
        Semaphore                          semaphore = new Semaphore(0);

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
            Platform.setImplicitExit(false);
        } catch (InterruptedException ignored) {

        }

    }

    public static void stopGUI() {
        Platform.exit();
    }

}
