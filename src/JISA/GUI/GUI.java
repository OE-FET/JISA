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
        GUI.runNow(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(text);
            alert.getDialogPane().setMinWidth(width);
            alert.showAndWait();
        });
    }

    public static String saveFileSelect() {

        AtomicReference<File> file = new AtomicReference<>();

        GUI.runNow(() -> {
            FileChooser chooser = new FileChooser();
            file.set(chooser.showSaveDialog(new Stage()));
        });

        return file.get().getAbsolutePath();

    }

    public static boolean confirmWindow(String title, String header, String text) {

        AtomicReference<Optional<ButtonType>> response = new AtomicReference<>();
        GUI.runNow(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(text);
            response.set(alert.showAndWait());
        });

        return response.get().get() == ButtonType.OK;

    }

    public static String[] inputWindow(String title, String header, String message, String... fields) {

        // Reference to take in returned value from the dialog.
        AtomicReference<String[]> toReturn = new AtomicReference<>();

        // All GUI stuff must be done on the GUI thread.
        GUI.runNow(() -> {

            Dialog<String[]> dialog = new Dialog<>();
            Label            img    = new Label();
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

        });

        // Return whatever value has been set in the reference
        return toReturn.get();

    }

    public static Object create(String fxmlFile) throws IOException {

        FXMLLoader loader     = new FXMLLoader(GUI.class.getResource(fxmlFile));
        Parent     root       = loader.load();
        Object     controller = loader.getController();
        Scene      scene      = new Scene(root);

        GUI.runNow(() -> {
            Stage stage = new Stage();
            stage.setScene(scene);
        });

        return controller;

    }

    public static InstrumentAddress browseVISA() {

        AtomicReference<InstrumentAddress> ref       = new AtomicReference<>();
        BrowseVISA                         browse    = new BrowseVISA("Find Instrument");
        Semaphore                          semaphore = new Semaphore(0);

        GUI.runNow(() -> browse.search((a) -> {
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

    public static void runNow(Runnable toRun) {

        if (Platform.isFxApplicationThread()) {
            toRun.run();
        } else {
            Semaphore s = new Semaphore(0);
            Platform.runLater(() -> {
                toRun.run();
                s.release();
            });
            try {
                s.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
