package jisa.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class BorderDisplay extends JFXWindow implements NotBordered {

    @FXML protected BorderPane pane;

    public BorderDisplay(String title) {
        super(title, BorderDisplay.class.getResource("fxml/Sidebar.fxml"));
        pane.setPadding(new Insets(7.5));
    }

    public void setCentre(Element centre) {

        Pane bordered = centre.getBorderedPane();

        GUI.runNow(() -> {
            pane.setCenter(bordered);
            BorderPane.setMargin(bordered, new Insets(7.5));
        });

    }

    public void setLeft(Element left) {

        Pane bordered = left.getBorderedPane();

        GUI.runNow(() -> {
            pane.setLeft(bordered);
            BorderPane.setMargin(bordered, new Insets(7.5));
        });

    }

    public void setRight(Element right) {

        Pane bordered = right.getBorderedPane();

        GUI.runNow(() -> {
            pane.setRight(bordered);
            BorderPane.setMargin(bordered, new Insets(7.5));
        });

    }

    public void setTop(Element top) {

        Pane bordered = top.getBorderedPane();

        GUI.runNow(() -> {
            pane.setTop(bordered);
            BorderPane.setMargin(bordered, new Insets(7.5));
        });

    }

    public void setBottom(Element bottom) {

        Pane bordered = bottom.getBorderedPane();

        GUI.runNow(() -> {
            pane.setBottom(bordered);
            BorderPane.setMargin(bordered, new Insets(7.5));
        });

    }

    @Override
    public Pane getNoBorderPane(boolean stripPadding) {
        return getPane();
    }
}
