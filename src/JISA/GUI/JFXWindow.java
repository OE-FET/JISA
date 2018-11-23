package JISA.GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class JFXWindow {

    protected Stage stage;

    public JFXWindow(String title, String fxmlPath) throws IOException {

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

    public void show() {
        GUI.runNow(() -> {
            stage.show();
        });
    }

    public void hide() {
        GUI.runNow(() -> {
            stage.hide();
        });
    }

    public void close() {
        GUI.runNow(() -> {
            stage.close();
        });
    }

    public void setMaximised(boolean flag) {
        GUI.runNow(() -> {
            stage.setMaximized(flag);
        });
    }

}
