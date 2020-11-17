package jisa.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jisa.Main;
import jisa.addresses.Address;
import jisa.experiment.Measurement;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GUI extends Application {

    public static final double SPACING = 10.0;

    private static final FileChooser      FILE_CHOOSER      = new FileChooser();
    private static final DirectoryChooser DIRECTORY_CHOOSER = new DirectoryChooser();

    /*
     * When first accessing the GUI class, all needed JavaFx native libraries must be extracted and added to
     * the java library path
     */
    static {

        String path = System.getProperty("java.library.path");

        try {

            // Create temporary directory to extract native libraries to
            File tempDir = Files.createTempDirectory("jfx-extracted-").toFile();

            // Read the list of all jfx native libraries contained in this jar
            Scanner nat = new Scanner(Main.class.getResourceAsStream("/native/libraries.txt"));

            // Make sure we clean up when we exit.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                File   directory = new File(tempDir.getAbsolutePath());
                File[] contents  = directory.listFiles();

                if (contents != null) {

                    for (File file : contents) {
                        file.delete();
                    }

                }

                directory.delete();

            }));

            // We only want to extract libraries for the current platform
            String osName = System.getProperty("os.name").toLowerCase();
            String extension;
            String libSep;

            if (osName.contains("win")) {
                extension = ".dll";
                libSep    = ";";
            } else if (osName.contains("mac")) {
                extension = ".dylib";
                libSep    = ":";
            } else {
                extension = ".so";
                libSep    = ":";
            }

            // Run through each library listed in the file, extracting it if it's for this platform
            while (nat.hasNextLine()) {

                String name = nat.nextLine();

                if (name.contains(extension)) {
                    InputStream resource = Main.class.getResourceAsStream("/native/" + name);
                    Files.copy(resource, Paths.get(tempDir.toString(), name));
                    resource.close();
                }

            }

            // Add the temporary directory to the library path list
            path = tempDir.toString() + libSep + path;
            System.setProperty("java.library.path", path);

        } catch (Exception ignored) {
            // If this goes wrong, then continue as planned hoping the there is a copy of JavaFx already installed
        }

        // Start-up the JavaFx GUI thread
        try {
            Thread t = new Thread(() -> Application.launch(App.class));
            t.start();
            Platform.setImplicitExit(false);
        } catch (Exception ignored) {

        }

    }

    /**
     * Dummy method used to initiate first accessing of the GUI thread early.
     */
    public static void touch() {

    }

    /**
     * Displays an error dialogue box, halting the thread until the user closes it.
     *
     * @param title  Window title
     * @param header Header text
     * @param text   Message text
     * @param width  Width of the window, in pixels
     */
    public static void errorAlert(String title, String header, String text, double width) {
        alert(Alert.AlertType.ERROR, title, header, text, width);
    }

    /**
     * Displays an information dialogue box, halting the thread until the user closes it.
     *
     * @param title  Window title
     * @param header Header text
     * @param text   Message text
     * @param width  Width of the window, in pixels
     */
    public static void infoAlert(String title, String header, String text, double width) {
        alert(Alert.AlertType.INFORMATION, title, header, text, width);
    }

    /**
     * Displays a warning dialogue box, halting the thread until the user closes it.
     *
     * @param title  Window title
     * @param header Header text
     * @param text   Message text
     */
    public static void warningAlert(String title, String header, String text) {
        alert(Alert.AlertType.WARNING, title, header, text);
    }

    /**
     * Displays an error dialogue box, halting the thread until the user closes it.
     *
     * @param title  Window title
     * @param header Header text
     * @param text   Message text
     */
    public static void errorAlert(String title, String header, String text) {
        alert(Alert.AlertType.ERROR, title, header, text);
    }

    /**
     * Displays an error dialogue box, halting the thread until the user closes it.
     *
     * @param title Window title and header text
     * @param text  Message text
     */
    public static void errorAlert(String title, String text) {
        errorAlert(title, title, text);
    }

    /**
     * Displays an error dialogue box, halting the thread until the user closes it.
     *
     * @param text Message text
     */
    public static void errorAlert(String text) {
        errorAlert("Error", "Error", text);
    }

    /**
     * Displays an information dialogue box, halting the thread until the user closes it.
     *
     * @param title  Window title
     * @param header Header text
     * @param text   Message text
     */
    public static void infoAlert(String title, String header, String text) {
        alert(Alert.AlertType.INFORMATION, title, header, text);
    }

    /**
     * Displays an information dialogue box, halting the thread until the user closes it.
     *
     * @param title Window title and header text
     * @param text  Message text
     */
    public static void infoAlert(String title, String text) {
        infoAlert(title, title, text);
    }


    /**
     * Displays an information dialogue box, halting the thread until the user closes it.
     *
     * @param text Message text
     */
    public static void infoAlert(String text) {
        infoAlert("Information", "Information", text);
    }

    /**
     * Displays a warning dialogue box, halting the thread until the user closes it.
     *
     * @param title  Window title
     * @param header Header text
     * @param text   Message text
     * @param width  Width of the window, in pixels
     */
    public static void warningAlert(String title, String header, String text, double width) {
        alert(Alert.AlertType.WARNING, title, header, text, width);
    }

    /**
     * Displays a warning dialogue box, halting the thread until the user closes it.
     *
     * @param title Window title and header text
     * @param text  Message text
     */
    public static void warningAlert(String title, String text) {
        warningAlert(title, title, text);
    }

    /**
     * Displays a warning dialogue box, halting the thread until the user closes it.
     *
     * @param text Message text
     */
    public static void warningAlert(String text) {
        warningAlert("Warning", "Warning", text);
    }

    private static void alert(final Alert.AlertType type, final String title, final String header, final String text) {
        alert(type, title, header, text, 400);
    }

    private static void alert(final Alert.AlertType type, final String title, final String header, final String text, final double width) {

        GUI.runNow(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(text);
            alert.getDialogPane().setMinWidth(width);
            alert.setResizable(true);
            alert.showAndWait();
        });

    }

    /**
     * Opens a file-select dialogue box for choosing a file path to write to.
     *
     * @return Selected file path, null if cancelled
     */
    public static String saveFileSelect() {

        AtomicReference<File> file = new AtomicReference<>();

        GUI.runNow(() -> file.set(FILE_CHOOSER.showSaveDialog(new Stage())));

        File chosen = file.get();

        if (chosen == null) {
            return null;
        } else {
            FILE_CHOOSER.setInitialDirectory(chosen.getParentFile());
            DIRECTORY_CHOOSER.setInitialDirectory(chosen.getParentFile());
            return chosen.getAbsolutePath();
        }

    }

    /**
     * Opens a file-select dialogue box for choosing a file path to write to.
     *
     * @return Selected file path, null if cancelled
     */
    public static String directorySelect() {

        AtomicReference<File> file = new AtomicReference<>();
        GUI.runNow(() -> file.set(DIRECTORY_CHOOSER.showDialog(new Stage())));

        File chosen = file.get();

        if (chosen == null) {
            return null;
        } else {
            DIRECTORY_CHOOSER.setInitialDirectory(chosen);
            FILE_CHOOSER.setInitialDirectory(chosen);
            return chosen.getAbsolutePath();
        }

    }

    /**
     * Opens a file-select dialogue box for choosing an already existing file to open.
     *
     * @return Selected file path, null if cancelled
     */
    public static String openFileSelect() {

        AtomicReference<File> file = new AtomicReference<>();
        GUI.runNow(() -> file.set(FILE_CHOOSER.showOpenDialog(new Stage())));

        File chosen = file.get();

        if (chosen == null) {
            return null;
        } else {
            FILE_CHOOSER.setInitialDirectory(chosen.getParentFile());
            DIRECTORY_CHOOSER.setInitialDirectory(chosen.getParentFile());
            return chosen.getAbsolutePath();
        }

    }

    /**
     * Opens a file-select dialogue box for choosing an already existing file to open.
     *
     * @return Selected file path, null if cancelled
     */
    public static List<String> openFileMultipleSelect() {

        AtomicReference<List<File>> file = new AtomicReference<>();

        GUI.runNow(() -> file.set(FILE_CHOOSER.showOpenMultipleDialog(new Stage())));

        List<File> list = file.get();

        if (list == null || list.isEmpty()) {
            return null;
        } else {
            List<String> paths = list.stream().map(File::getAbsolutePath).collect(Collectors.toList());
            FILE_CHOOSER.setInitialDirectory(list.get(0).getParentFile());
            DIRECTORY_CHOOSER.setInitialDirectory(list.get(0).getParentFile());
            return paths;
        }

    }

    /**
     * Opens a confirmation dialogue, returning the users response as a boolean.
     *
     * @param title  Window title
     * @param header Header text
     * @param text   Message text
     *
     * @return Boolean result, true for okay, false for cancel
     */
    public static boolean confirmWindow(String title, String header, String text) {

        AtomicReference<Optional<ButtonType>> response = new AtomicReference<>();
        GUI.runNow(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(text);
            alert.setResizable(true);
            response.set(alert.showAndWait());
        });

        return response.get().get() == ButtonType.OK;

    }

    /**
     * Opens a dialogue box with text-boxes for user-input, returning the user-input values as an array of Strings.
     *
     * @param title   Window title
     * @param header  Header text
     * @param message Message text
     * @param fields  Names of the input-fields
     *
     * @return Array of input values
     */
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
            dialog.setResizable(true);

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

    public static int choiceWindow(String title, String header, String message, String... options) {

        // Reference to take in returned value from the dialog.
        AtomicReference<Integer> toReturn = new AtomicReference<>();

        // All GUI stuff must be done on the GUI thread.
        GUI.runNow(() -> {

            Dialog<Integer> dialog = new Dialog<>();
            Label           img    = new Label();
            img.getStyleClass().addAll("choice-dialog", "dialog-pane");
            dialog.setGraphic(img);
            dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            dialog.getDialogPane().setMinWidth(400);
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            dialog.setResizable(true);

            VBox list = new VBox();
            list.setSpacing(15);
            list.setPadding(new Insets(15, 15, 15, 15));
            list.getChildren().add(new Label(message));

            int i = 0;
            for (String option : options) {

                final int index = i;

                Button button = new Button(option);
                button.setMaxWidth(Integer.MAX_VALUE);
                button.setAlignment(Pos.CENTER_LEFT);
                list.getChildren().add(button);

                button.setOnAction(ae -> {

                    dialog.setResult(index);
                    dialog.hide();
                    dialog.close();

                });

                i++;

            }

            dialog.getDialogPane().setContent(list);

            toReturn.set(dialog.showAndWait().orElse(-1));

        });

        return toReturn.get();

    }

    /**
     * Opens a window listing all instruments detected by VISA etc, allowing the user to select one.
     *
     * @return Address object of the selected instrument
     */
    public static Address browseVISA() {

        return (new VISABrowser("Browse Instruments")).selectAddress();

    }

    public static void waitForExit() {

        try {
            (new Semaphore(0)).acquire();
        } catch (InterruptedException ignored) {

        }

    }

    /**
     * Stops the GUI thread. It cannot be restarted afterwards.
     */
    public static void stopGUI() {
        Platform.exit();
    }

    /**
     * Runs the supplied Runnable on the GUI thread, waits until it has finished.
     *
     * @param toRun Code to run on GUI thread
     */
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

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    public void runMeasurement(Measurement measurement) {

        try {

            measurement.start();

            if (measurement.wasStopped()) {

                warningAlert("Stopped", "Measurement Stopped", "The measurement was stopped before completion.");

            } else {

                infoAlert("Complete", "Measurement Complete", "The measurement completed without error.");

            }

        } catch (Exception e) {

            errorAlert("Error", "Exception Encountered", String.format("There was an error with the measurement:\n%s", e.getMessage()), 600);

        }

    }

}
