package JISA.GUI;

import javafx.scene.layout.Pane;

public interface Element {

    Pane getPane();

    String getTitle();

    default void setVisible(boolean visible) {
        getPane().setVisible(visible);
        getPane().setManaged(visible);
    }

    default boolean isVisible() {
        return getPane().isVisible();
    }

}
