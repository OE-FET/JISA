package JISA.GUI;

import JISA.GUI.FXML.GridWindow;
import javafx.scene.layout.Pane;

public class Grid implements Gridable {

    private GridWindow window;

    public Grid(String title) {
        window = GridWindow.create(title);
    }

    public void add(Gridable toAdd) {
        window.addPane(toAdd);
    }

    public void addToolbarButton(String text, ClickHandler onClick) {
        window.addToolbarButton(text, onClick);
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

    @Override
    public Pane getPane() {
        return null;
    }
}
