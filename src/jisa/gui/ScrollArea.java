package jisa.gui;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;

public class ScrollArea extends JFXElement {

    protected final ScrollPane scrollPane;

    public ScrollArea(String title, Element content) {

        super(title);

        GUI.touch();
        scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(true);
        setContent(content);
        BorderPane.setMargin(scrollPane, Insets.EMPTY);
        scrollPane.setBorder(Border.EMPTY);
        scrollPane.setBackground(Background.EMPTY);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        setCentreNode(scrollPane);
        getNode().setPadding(Insets.EMPTY);

    }

    public ScrollArea(String title) {
        this(title, null);
    }

    public ScrollArea(Element content) {
        this(content.getTitle(), content);
    }

    public void setContent(Element content) {
        GUI.runNow(() -> scrollPane.setContent(content.getNode()));
    }

}
