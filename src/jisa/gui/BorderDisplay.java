package jisa.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class BorderDisplay extends JFXElement {

    @FXML
    protected BorderPane pane;
    private Element left   = null;
    private Element right  = null;
    private Element centre = null;
    private Element top    = null;
    private Element bottom = null;

    public BorderDisplay(String title) {
        super(title, BorderDisplay.class.getResource("fxml/Sidebar.fxml"));
        BorderPane.setMargin(getNode().getCenter(), new Insets(0));
        pane.setPadding(new Insets(GUI.SPACING / 2));
    }

    public void setCentreElement(Element centre) {

        Node bordered = centre.getBorderedNode();

        GUI.runNow(() -> {
            pane.setCenter(bordered);
            BorderPane.setMargin(bordered, new Insets(GUI.SPACING / 2));
        });

    }


    public void setLeftElement(Element left) {

        Node bordered = left.getBorderedNode();

        GUI.runNow(() -> {
            pane.setLeft(bordered);
            BorderPane.setMargin(bordered, new Insets(GUI.SPACING / 2));
        });

    }

    public void setRightElement(Element right) {

        Node bordered = right.getBorderedNode();

        GUI.runNow(() -> {
            pane.setRight(bordered);
            BorderPane.setMargin(bordered, new Insets(GUI.SPACING / 2));
        });

    }

    public void setTopElement(Element top) {

        Node bordered = top.getBorderedNode();

        GUI.runNow(() -> {
            pane.setTop(bordered);
            BorderPane.setMargin(bordered, new Insets(GUI.SPACING / 2));
        });

    }

    public void setBottomElement(Element bottom) {

        Node bordered = bottom.getBorderedNode();

        GUI.runNow(() -> {
            pane.setBottom(bordered);
            BorderPane.setMargin(bordered, new Insets(GUI.SPACING / 2));
        });

    }

    public Element getLeftElement() {
        return left;
    }

    public Element getRightElement() {
        return right;
    }

    public Element getCentreElement() {
        return centre;
    }

    public Element getTopElement() {
        return top;
    }

    public Element getBottomElement() {
        return bottom;
    }

    public Node getBorderedNode() {
        return getNode();
    }

}
