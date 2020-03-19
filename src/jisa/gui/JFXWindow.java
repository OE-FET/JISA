package jisa.gui;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import jisa.Util;
import jisa.control.SRunnable;
import jisa.enums.Icon;

import java.io.IOException;
import java.net.URL;

public class JFXWindow implements Element {

    protected Stage  stage;
    protected Image  icon   = null;
    private   double width  = -1;
    private   double height = -1;

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

    public JFXWindow(String title, URL resource) {
        this(title, null, resource);
    }

    protected JFXWindow(String title, Icon icon, URL resource) {

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

    public void setWindowSize(double width, double height) {

        this.width  = width;
        this.height = height;

        GUI.runNow(() -> {
            stage.setWidth(width);
            stage.setHeight(height);
        });

    }

    public void autoSizeWindow() {

        this.width  = -1;
        this.height = -1;

        if (stage.isShowing()) {
            GUI.runNow(() -> stage.sizeToScene());
        }

    }

    /**
     * Shows the window.
     */
    public void show() {

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        GUI.runNow(() -> {

            stage.show();

            if (stage.isMaximized()) {
                return;
            }

            if (width < 0 && height < 0) {
                stage.sizeToScene();
                stage.setHeight(Math.min(stage.getHeight(), bounds.getHeight()));
                stage.setWidth(Math.min(stage.getWidth(), bounds.getWidth()));
            } else {
                stage.setWidth(width);
                stage.setHeight(height);
            }

        });

    }

    /**
     * Hides the window
     */
    public void hide() {
        GUI.runNow(stage::hide);
    }

    /**
     * Closes the window
     */
    public void close() {
        GUI.runNow(stage::close);
    }

    /**
     * Returns whether the element is currently open in its own window.
     *
     * @return Open?
     */
    public boolean isOpen() {
        return stage.isShowing();
    }

    public void autoAdjustSize() {

        GUI.runNow(() -> {

            double minHeight = stage.getMinHeight();
            double minWidth  = stage.getMinWidth();

            stage.setMinHeight(stage.getHeight());
            stage.setMinWidth(stage.getWidth());

            stage.sizeToScene();

            stage.setMinHeight(minHeight);
            stage.setMinWidth(minWidth);

        });

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

    public void setTitle(String title) {
        GUI.runNow(() -> stage.setTitle(title));
    }

    public void setExitOnClose(boolean close) {

        if (close) {

            stage.setOnCloseRequest(a -> {
                GUI.stopGUI();
                System.exit(0);
            });

        } else {
            stage.setOnCloseRequest(a -> {
            });
        }

    }

    public void setOnClose(SRunnable toRun) {

        stage.setOnCloseRequest(a -> {

            try {
                toRun.run();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {

        GUI.runNow(() -> {
            if (icon != null) {
                Util.runRegardless(() -> this.icon = new Image(icon.getImage().openStream()));
                stage.getIcons().add(this.icon);
            } else {
                stage.getIcons().clear();
            }
        });

    }

    public void setIcon(URL icon) {


        GUI.runNow(() -> {
            if (icon != null) {
                Util.runRegardless(() -> this.icon = new Image(icon.openStream()));
                stage.getIcons().add(this.icon);
            } else {
                stage.getIcons().clear();
            }
        });
    }

}
