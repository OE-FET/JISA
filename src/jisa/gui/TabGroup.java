package jisa.gui;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import jisa.Util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TabGroup extends JFXWindow implements Container {

    public  TabPane       pane;
    private List<Element> elements = new LinkedList<>();

    public TabGroup(String title) {

        super(title, TabGroup.class.getResource("fxml/TabGroup.fxml"));
        GUI.runNow(() -> pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE));

    }

    public TabGroup(String title, Element... elements) {
        this(title);
        addAll(elements);
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

    public void setTabsPosition(Side side) {
        GUI.runNow(() -> pane.setSide(side));
    }

    public Side getTabsPosition() {
        return pane.getSide();
    }

    public void select(int index) {
        GUI.runNow(() -> pane.getSelectionModel().select(index));
    }

    public void select(Element select) {

        int index = elements.indexOf(select);

        if (Util.isBetween(index, 0, pane.getTabs().size()-1)) {
            select(index);
        }

    }

    public int getSelectedIndex() {
        return pane.getSelectionModel().getSelectedIndex();
    }

    public Element getSelectedElement() {
        return elements.get(getSelectedIndex());
    }

}
