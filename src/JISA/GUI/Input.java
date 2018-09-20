package JISA.GUI;

import JISA.GUI.FXML.InputWindow;
import JISA.GUI.Gridable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.io.File;

public class Input implements Gridable {

    private InputWindow window;

    public Input(String title, boolean closeOnEnter, InputWindow.InputHandler onEnter) {
        window = InputWindow.create(title, closeOnEnter, onEnter);
    }

    public void addField(String name) {
        window.addField(name);
    }

    public void addFileSave(String name) {
        window.addFileSave(name);
    }

    @Override
    public Pane getPane() {
        return window.getPane();
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    public void close() {
        window.close();
    }
}
