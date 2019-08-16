package jisa.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jisa.Util;
import jisa.enums.Icon;

import java.io.IOException;
import java.net.URL;
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
    private ArrayList<Runnable> reseters  = new ArrayList<>();

    /**
     * Creates an element that displays other GUI elements in their own individual tabs.
     *
     * @param title Window title
     * @param toAdd Elements to add
     *
     * @throws IOException
     */
    public Tabs(String title, Element... toAdd) {
        super(title, Tabs.class.getResource("fxml/TabWindow.fxml"));
        this.title = title;
        addAll(toAdd);
    }

    /**
     * Adds an element as a tab.
     *
     * @param element   Element to add
     */
    public void add(Element element) {

        HBox  tab  = new HBox();
        Image icon = element.getIcon();

        tab.setPadding(new Insets(15, 15, 15, 15));
        tab.setSpacing(15);
        tab.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(element.getTitle());
        name.setTextFill(Color.WHITE);

        ImageView imageView = new ImageView();
        imageView.maxWidth(25.0);
        imageView.maxHeight(25.0);
        imageView.setFitHeight(25.0);
        imageView.setFitWidth(25.0);

        final Image finalInverted;
        if (icon != null) {
            imageView.setImage(icon);
            imageView.setSmooth(true);
            imageView.setPreserveRatio(true);
            finalInverted = Util.invertImage(icon);
            tab.getChildren().add(imageView);
        } else {
            finalInverted = null;
        }

        tab.getChildren().add(name);

        final Image finalIcon = icon;

        Runnable onReset = () -> {

            tab.setStyle("-fx-background-color: transparent;");
            tab.setEffect(null);
            name.setStyle("-fx-text-fill: white;");

            if (finalIcon != null) {
                imageView.setImage(finalIcon);
            }

        };

        Runnable onClick = () -> {

            for (Runnable other : reseters) {
                other.run();
            }

            tab.setStyle("-fx-background-color: white;");
            name.setStyle("-fx-text-fill: #4c4c4c;");

            scrollPane.setContent(element.getPane());

            if (finalIcon != null) {
                imageView.setImage(finalInverted);
            }

        };

        tab.setOnMouseClicked((ae) -> onClick.run());

        tab.setOnMouseEntered((ae) -> {

            if (!tab.getStyle().equals("-fx-background-color: white;")) {
                tab.setStyle("-fx-background-color: rgba(255,255,255, 0.3);");
            }

        });

        tab.setOnMouseExited((ae) -> {

            if (!tab.getStyle().equals("-fx-background-color: white;")) {
                tab.setStyle("-fx-background-color: transparent;");
            }

        });

        sidebar.getChildren().add(tab);
        tabs.add(tab);
        switchers.add(onClick);
        reseters.add(onReset);

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
