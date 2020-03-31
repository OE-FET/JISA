package jisa.gui;

import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jisa.Util;

import java.util.LinkedList;
import java.util.List;

/**
 * A GUI container element for displaying multiple other elements as traditional-style tabs.
 */
public class Tabs extends JFXWindow implements Container, NotBordered {

    public  TabPane       pane;
    private List<Element> elements = new LinkedList<>();

    public Tabs(String title) {

        super(title, Tabs.class.getResource("fxml/TabGroup.fxml"));
        GUI.runNow(() -> pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE));

    }

    public Tabs(String title, Element... elements) {
        this(title);
        addAll(elements);
    }

    public Pane getPane() {
        return new Pane(pane);
    }

    @Override
    public synchronized void add(Element element) {

        GUI.runNow(() -> {
            Tab tab = new Tab(element.getTitle(), element.getPane());
            pane.getTabs().add(tab);
            elements.add(element);
        });

    }

    @Override
    public synchronized void remove(Element element) {

        GUI.runNow(() -> {

            int index = elements.indexOf(element);

            if (Util.isBetween(index, 0, pane.getTabs().size() - 1)) {
                pane.getTabs().remove(index);
                elements.remove(element);
            }

        });

    }

    @Override
    public synchronized void clear() {

        GUI.runNow(() -> {
            pane.getTabs().clear();
            elements.clear();
        });

    }

    @Override
    public List<Element> getElements() {
        return new LinkedList<>(elements);
    }

    public Side getTabsPosition() {
        return pane.getSide();
    }

    /**
     * Sets the side for the tabs to be displayed on.
     *
     * @param side LEFT, RIGHT, TOP or BOTTOM
     */
    public void setTabsPosition(Side side) {
        GUI.runNow(() -> pane.setSide(side));
    }

    /**
     * Sets which tab is selected, by specifying its index.
     *
     * @param index Tab index
     */
    public void select(int index) {
        GUI.runNow(() -> pane.getSelectionModel().select(index));
    }

    /**
     * Sets which tab is selected, by specifying its corresponding Element object.
     *
     * @param select Element to be selected
     */
    public void select(Element select) {

        int index = elements.indexOf(select);

        if (Util.isBetween(index, 0, pane.getTabs().size() - 1)) {
            select(index);
        }

    }

    /**
     * Returns the index of the tab currently selected.
     *
     * @return Tab index
     */
    public int getSelectedIndex() {
        return pane.getSelectionModel().getSelectedIndex();
    }

    /**
     * Returns the element that is currently selected.
     *
     * @return Selected element
     */
    public Element getSelectedElement() {
        return elements.get(getSelectedIndex());
    }

    @Override
    public Pane getNoBorderPane(boolean stripPadding) {

        BorderPane parent = new BorderPane(pane);
        parent.setStyle("-fx-background-color: transparent");
        parent.setBorder(new Border(new BorderStroke(Color.web("#c8c8c8"), BorderStrokeStyle.SOLID, new CornerRadii(0.0), new BorderWidths(1.0))));

        if (stripPadding) {
            parent.setPadding(new Insets(0,0,0,0));
        }

        return parent;

    }
}
