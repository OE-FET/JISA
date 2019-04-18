package JISA.GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class JFXWindow implements Element {

    protected Stage stage;

    /**
     * Creates a GUI window using the specified FXML file.
     *
     * @param title    Title to display in window title-bar
     * @param fxmlPath Path to FXML file to use
     *
     * @throws IOException Upon error reading from FXML file
     */
    public JFXWindow(String title, String fxmlPath) throws IOException {

        // Make sure the GUI thread has started
        GUI.touch();

        // Create a loader for our FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

        // Tell the loader to link the FXML file to this object
        loader.setController(this);

        // Load our layout from our FXML file as a "Scene":
        Scene scene = new Scene(loader.load());

        // Create the stage (window) and add the layout to it in GUI thread.
        GUI.runNow(() -> {
            stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
        });

    }

    protected JFXWindow(String title, String fxmlPath, boolean ignore) {

        // Make sure the GUI thread has started
        GUI.touch();

        // Create a loader for our FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

        // Tell the loader to link the FXML file to this object
        loader.setController(this);

        // Load our layout from our FXML file as a "Scene":
        try {
            Scene scene = new Scene(loader.load());

            // Create the stage (window) and add the layout to it in GUI thread.
            GUI.runNow(() -> {
                stage = new Stage();
                stage.setScene(scene);
                stage.setTitle(title);
            });
        } catch (IOException ignored) {

        }
    }

    protected JFXWindow(String title, URL resource) {

        // Make sure the GUI thread has started
        GUI.touch();

        // Create a loader for our FXML file
        FXMLLoader loader = new FXMLLoader(resource);

        // Tell the loader to link the FXML file to this object
        loader.setController(this);

        // Load our layout from our FXML file as a "Scene":
        // Create the stage (window) and add the layout to it in GUI thread.
        GUI.runNow(() -> {
            Scene scene = null;
            try {
                scene = new Scene(loader.load());
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
        });
    }

    /**
     * Shows the window.
     */
    public void show() {
        GUI.runNow(() -> stage.show());
    }

    /**
     * Hides the window
     */
    public void hide() {
        GUI.runNow(() -> stage.hide());
    }

    /**
     * Closes the window
     */
    public void close() {
        GUI.runNow(() -> stage.close());
    }

    /**
     * Sets whether the window is maximised or not.
     *
     * @param flag Maximised?
     */
    public void setMaximised(boolean flag) {
        GUI.runNow(() -> stage.setMaximized(flag));
    }

    @Override
    public Pane getPane() {
        return (Pane) stage.getScene().getRoot();
    }

    public String getTitle() {
        return stage.getTitle();
    }

    public void setExitOnClose(boolean close) {

        if (close) {
            stage.setOnCloseRequest((a) -> {
                GUI.stopGUI();
                System.exit(0);
            });
        } else {
            stage.setOnCloseRequest((a) -> {
            });
        }

    }

}
