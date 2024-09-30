package jisa.gui;

import com.sun.glass.ui.GlassRobot;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.enums.Icon;
import jisa.experiment.MeasurementOld;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GUI {

    public static final double SPACING = 10.0;

    private static final FileChooser      FILE_CHOOSER      = new FileChooser();
    private static final DirectoryChooser DIRECTORY_CHOOSER = new DirectoryChooser();

    /*
     * When first accessing the GUI class, all needed JavaFx native libraries must be extracted and added to
     * the java library path
     */
    static {
        JavaFX.launch();
    }

    /**
     * Dummy method used to initiate first accessing of the GUI thread early.
     */
    public static void touch() {

    }

    public static URL getFXML(String name) {
        return GUI.class.getResource(String.format("fxml/%s.fxml", name));
    }

    public static <T extends Instrument> T connectAndConfigure(Class<T> driver, Address address) {

        Constructor<T> constructor;

        try {
            constructor = driver.getConstructor(Address.class);
        } catch (NoSuchMethodException e) {
            GUI.errorAlert("That driver does not have a valid constructor.");
            return null;
        }

        T instrument;
        try {
            instrument = constructor.newInstance(address);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            GUI.showException(e);
            return null;
        }

        return instrument;

    }

    public static <T extends Instrument> T connectAndConfigure(Class<T> type) {

        Connector<Instrument> connector    = new Connector<>("Connection", Instrument.class);
        Configurator<T>       configurator = new Configurator<>("Configuration", type);


        var window = new Grid("Instrument Connection", 1, connector, configurator);

        window.setGrowth(true, false);
        window.setWindowSize(800, 1024);

        connector.getConnection().addChangeListener(() -> configurator.setConnection(connector.getConnection()));

        if (window.showAsConfirmation()) {

            try {
                return configurator.getConfiguration().get();
            } catch (IOException | DeviceException e) {
                e.printStackTrace();
                GUI.showException(e);
            }

        }

        return null;

    }

    public static <T extends Instrument> T connectAndConfigure(KClass<T> type) {
        return connectAndConfigure(JvmClassMappingKt.getJavaClass(type));
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

            Screen      screen = getCurrentScreen();
            Rectangle2D bounds = screen.getVisualBounds();

            alert.getDialogPane().getScene().getWindow().setOnShown(e -> {

                double w = alert.getDialogPane().getScene().getWindow().getWidth();
                double h = alert.getDialogPane().getScene().getWindow().getHeight();

                alert.setX(((bounds.getMinX() + bounds.getMaxX()) / 2) - (w / 2));
                alert.setY(((bounds.getMinY() + bounds.getMaxY()) / 2) - (h / 2));

            });

            alert.showAndWait();

        });

    }

    public static void showException(Throwable e) {

        ListDisplay<StackTraceElement> stackTrace = new ListDisplay<>("Stack Trace");

        for (StackTraceElement ste : e.getStackTrace()) {
            stackTrace.add(ste, String.format("%s.%s", ste.getClassName(), ste.getMethodName()), String.format("%s:%d", ste.getFileName(), ste.getLineNumber()), Icon.DATA.getBlackImage());
        }

        GUI.runNow(() -> {

            VBox box = new VBox(new Label(e.getMessage()), stackTrace.getBorderedNode());
            box.setSpacing(15.0);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception Encountered");
            alert.setHeaderText(e.getClass().getSimpleName());
            alert.getDialogPane().setContent(box);
            alert.getDialogPane().setMinWidth(600.0);
            alert.getDialogPane().setMinHeight(400.0);
            alert.setResizable(true);

            Screen      screen = getCurrentScreen();
            Rectangle2D bounds = screen.getVisualBounds();

            alert.getDialogPane().getScene().getWindow().setOnShown(ev -> {

                double w = alert.getDialogPane().getScene().getWindow().getWidth();
                double h = alert.getDialogPane().getScene().getWindow().getHeight();

                alert.setX(((bounds.getMinX() + bounds.getMaxX()) / 2) - (w / 2));
                alert.setY(((bounds.getMinY() + bounds.getMaxY()) / 2) - (h / 2));

            });

            alert.showAndWait();

        });

    }

    /**
     * Opens a file-select dialogue box for choosing a file path to write to.
     *
     * @return Selected file path, null if cancelled
     */
    public static String saveFileSelect(String startPath) {

        AtomicReference<File> file = new AtomicReference<>();

        if (startPath != null) {

            File start = new File(startPath);

            if (start.exists()) {
                FILE_CHOOSER.setInitialDirectory(start);
            }

        }


        Rectangle2D screen = getCurrentScreen().getVisualBounds();

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

    public static String saveFileSelect() {
        return saveFileSelect(null);
    }

    /**
     * Opens a file-select dialogue box for choosing a file path to write to.
     *
     * @return Selected file path, null if cancelled
     */
    public static String directorySelect(String startPath) {

        AtomicReference<File> file = new AtomicReference<>();

        if (startPath != null) {

            File start = new File(startPath);

            if (start.exists()) {
                DIRECTORY_CHOOSER.setInitialDirectory(start);
            }

        }

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

    public static String directorySelect() {
        return directorySelect(null);
    }

    /**
     * Opens a file-select dialogue box for choosing an already existing file to open.
     *
     * @return Selected file path, null if cancelled
     */
    public static String openFileSelect(String startPath) {

        AtomicReference<File> file = new AtomicReference<>();

        if (startPath != null) {

            File start = new File(startPath);

            if (start.exists()) {
                FILE_CHOOSER.setInitialDirectory(start.isDirectory() ? start : start.getParentFile());
            }

        }

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

    public static String openFileSelect() {
        return openFileSelect(null);
    }

    /**
     * Opens a file-select dialogue box for choosing an already existing file to open.
     *
     * @return Selected file path, null if cancelled
     */
    public static List<String> openFileMultipleSelect(String startPath) {

        AtomicReference<List<File>> file = new AtomicReference<>();

        if (startPath != null) {

            File start = new File(startPath);

            if (start.exists()) {
                FILE_CHOOSER.setInitialDirectory(start.isDirectory() ? start : start.getParentFile());
            }

        }

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

    public static List<String> openFileMultipleSelect() {
        return openFileMultipleSelect(null);
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

            Screen      screen = getCurrentScreen();
            Rectangle2D bounds = screen.getVisualBounds();

            alert.getDialogPane().getScene().getWindow().setOnShown(e -> {

                double w = alert.getDialogPane().getScene().getWindow().getWidth();
                double h = alert.getDialogPane().getScene().getWindow().getHeight();

                alert.setX(((bounds.getMinX() + bounds.getMaxX()) / 2) - (w / 2));
                alert.setY(((bounds.getMinY() + bounds.getMaxY()) / 2) - (h / 2));

            });

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

            Util.runAsync(() -> GUI.runNow(() -> tFields.stream().findFirst().ifPresent(Node::requestFocus)));

            Screen      screen = getCurrentScreen();
            Rectangle2D bounds = screen.getVisualBounds();

            dialog.getDialogPane().getScene().getWindow().setOnShown(e -> {

                double w = dialog.getDialogPane().getScene().getWindow().getWidth();
                double h = dialog.getDialogPane().getScene().getWindow().getHeight();

                dialog.setX(((bounds.getMinX() + bounds.getMaxX()) / 2) - (w / 2));
                dialog.setY(((bounds.getMinY() + bounds.getMaxY()) / 2) - (h / 2));

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
            list.setSpacing(10);
            list.setPadding(new Insets(10));
            list.getChildren().add(new Label(message));

            int i = 0;
            for (String option : options) {

                final int index = i;

                Button button = new Button(option);
                button.setPadding(new Insets(7.5));
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

            Screen      screen = getCurrentScreen();
            Rectangle2D bounds = screen.getVisualBounds();

            dialog.getDialogPane().getScene().getWindow().setOnShown(e -> {

                double w = dialog.getDialogPane().getScene().getWindow().getWidth();
                double h = dialog.getDialogPane().getScene().getWindow().getHeight();

                dialog.setX(((bounds.getMinX() + bounds.getMaxX()) / 2) - (w / 2));
                dialog.setY(((bounds.getMinY() + bounds.getMaxY()) / 2) - (h / 2));

            });

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

    public static Screen getCurrentScreen() {

        AtomicReference<Screen> screen = new AtomicReference<>();

        GUI.runNow(() -> {
            GlassRobot robot = com.sun.glass.ui.Application.GetApplication().createRobot();
            screen.set(Screen.getScreensForRectangle(robot.getMouseX(), robot.getMouseY(), 1, 1).stream().findFirst().orElse(null));
        });

        return screen.get();

    }

    public void runMeasurement(MeasurementOld measurement) {

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
