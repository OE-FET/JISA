package JISA.GUI;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tabs extends JFXWindow implements Element, Container {

    public  BorderPane          pane;
    public  VBox                sidebar;
    public  ScrollPane          scrollPane;
    private String              title;
    private ArrayList<HBox>     tabs      = new ArrayList<>();
    private ArrayList<Element>  added     = new ArrayList<>();
    private ArrayList<Runnable> switchers = new ArrayList<>();

    /**
     * Creates an element that displays other GUI elements in their own individual tabs.
     *
     * @param title Window title
     * @param toAdd Elements to add
     *
     * @throws IOException
     */
    public Tabs(String title, Element... toAdd) {
        super(title, Tabs.class.getResource("FXML/TabWindow.fxml"));
        this.title = title;
        addAll(toAdd);
    }

    /**
     * Adds an element as a tab.
     *
     * @param element Element to add
     */
    public void add(Element element) {

        HBox tab = new HBox();
        tab.setPadding(new Insets(15, 15, 15, 15));

        Label name = new Label(element.getTitle());
        name.setTextFill(Color.WHITE);

        tab.getChildren().add(name);

        Runnable onClick = () -> {

            for (HBox other : tabs) {
                other.setStyle("-fx-background-color: transparent;");
                other.getChildren().get(0).setStyle("-fx-text-fill: white;");
            }

            tab.setStyle("-fx-background-color: white;");
            name.setStyle("-fx-text-fill: #4c4c4c;");

            scrollPane.setContent(element.getPane());
        };

        tab.setOnMouseClicked((ae) -> onClick.run());

        sidebar.getChildren().add(tab);
        tabs.add(tab);
        switchers.add(onClick);

        if (tabs.size() == 1) {
            onClick.run();
        }

    }

    @Override
    public void remove(Element element) {

        int index = added.indexOf(element);

        if (index > -1) {

            sidebar.getChildren().remove(tabs.get(index));
            tabs.remove(index);
            switchers.remove(index);
            added.remove(index);

        }

    }

    @Override
    public void clear() {
        sidebar.getChildren().clear();
        tabs.clear();
        switchers.clear();
        added.clear();
    }

    @Override
    public List<Element> getElements() {
        return new ArrayList<>(added);
    }

    /**
     * Selects the specified tab.
     *
     * @param pane Pane to select
     */
    public void select(int pane) {

        GUI.runNow(() -> {
            switchers.get(pane).run();
        });

    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
