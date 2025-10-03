package jisa.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jisa.Util;

import java.util.LinkedList;
import java.util.List;

/**
 * A GUI container element for displaying multiple other elements as traditional-style tabs.
 */
public class Tabs extends JFXElement implements Container {

    private final List<Element> elements = new LinkedList<>();
    @FXML
    protected     BorderPane    pane;
    @FXML
    protected     TabPane       tabPane;
    @FXML
    protected     ToolBar       toolBar;

    public Tabs(String title) {

        super(title, Tabs.class.getResource("fxml/TabGroup.fxml"));
        GUI.runNow(() -> tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE));
        BorderPane.setMargin(getNode().getCenter(), new Insets(0));

    }

    public Tabs(String title, Element... elements) {
        this(title);
        addAll(elements);
    }

    public Tabs(Element... elements) {
        this("", elements);
    }

    public Pane getTabPane() {
        return new Pane(tabPane);
    }

    @Override
    public synchronized void add(Element element) {

        GUI.runNow(() -> {
            Tab tab = new Tab(element.getTitle(), element.getNode());
            tabPane.getTabs().add(tab);
            elements.add(element);
        });

    }

    @Override
    public synchronized void remove(Element element) {

        GUI.runNow(() -> {

            int index = elements.indexOf(element);

            if (Util.isBetween(index, 0, tabPane.getTabs().size() - 1)) {
                tabPane.getTabs().remove(index);
                elements.remove(element);
            }

        });

    }

    @Override
    public synchronized void clear() {

        GUI.runNow(() -> {
            tabPane.getTabs().clear();
            elements.clear();
        });

    }

    @Override
    public List<Element> getElements() {
        return new LinkedList<>(elements);
    }

    public Side getTabsPosition() {
        return tabPane.getSide();
    }

    /**
     * Sets the side for the tabs to be displayed on.
     *
     * @param side LEFT, RIGHT, TOP or BOTTOM
     */
    public void setTabsPosition(Side side) {
        GUI.runNow(() -> tabPane.setSide(side));
    }

    /**
     * Sets which tab is selected, by specifying its index.
     *
     * @param index Tab index
     */
    public void select(int index) {
        GUI.runNow(() -> tabPane.getSelectionModel().select(index));
    }

    /**
     * Sets which tab is selected, by specifying its corresponding Element object.
     *
     * @param select Element to be selected
     */
    public void select(Element select) {

        int index = elements.indexOf(select);

        if (Util.isBetween(index, 0, tabPane.getTabs().size() - 1)) {
            select(index);
        }

    }

    /**
     * Returns the index of the tab currently selected.
     *
     * @return Tab index
     */
    public int getSelectedIndex() {
        return tabPane.getSelectionModel().getSelectedIndex();
    }

    /**
     * Returns the element that is currently selected.
     *
     * @return Selected element
     */
    public Element getSelectedElement() {
        return elements.get(getSelectedIndex());
    }

    public Node getBorderedNode() {
        BorderPane border = new BorderPane();
        border.setCenter(getNode());
        border.setBackground(Background.EMPTY);
        border.setBorder(new Border(new BorderStroke(Color.web("#c8c8c8"), BorderStrokeStyle.SOLID, new CornerRadii(0.0), new BorderWidths(1.0))));
        return border;
    }

}
