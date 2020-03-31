package jisa.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import jisa.Util;
import jnr.ffi.annotations.In;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Pages extends JFXWindow implements Element, Container {

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
    public Pages(String title, Element... toAdd) {
        super(title, Pages.class.getResource("fxml/TabWindow.fxml"));
        this.title = title;
        addAll(toAdd);
    }

    /**
     * Adds an element as a tab.
     *
     * @param element Element to add
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

        final Pane pane = element.getPane();

        Runnable onClick = () -> {

            reseters.forEach(Runnable::run);

            tab.setStyle("-fx-background-color: white;");
            name.setStyle("-fx-text-fill: #4c4c4c;");

            scrollPane.setContent(pane);

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

        GUI.runNow(() -> sidebar.getChildren().add(tab));
        tabs.add(tab);
        switchers.add(onClick);
        reseters.add(onReset);

        if (tabs.size() == 1) {
            onClick.run();
        }

    }

    public jisa.gui.Separator addSeparator() {

        Separator separator = new Separator();
        separator.setPadding(new Insets(15, 15, 15, 15));

        GUI.runNow(() -> sidebar.getChildren().add(separator));

        return new jisa.gui.Separator.SeparatorWrapper(separator) {

            @Override
            public void remove() {
                GUI.runNow(() -> sidebar.getChildren().remove(separator));
            }

        };

    }

    public jisa.gui.Separator addSeparator(String text) {

        Label label = new Label(text);
        label.setTextFill(Colour.WHITE);

        label.setMaxWidth(Double.MAX_VALUE);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(15, 15, 5, 15));


        Separator separator = new Separator();
        separator.setPadding(new Insets(0, 15, 10, 15));

        GUI.runNow(() -> {
            sidebar.getChildren().addAll(label, separator);
        });

        return new jisa.gui.Separator() {

            @Override
            public void remove() {
                GUI.runNow(() -> sidebar.getChildren().removeAll(label, separator));
            }

            @Override
            public boolean isVisible() {
                return label.isVisible();
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    separator.setVisible(visible);
                    separator.setManaged(visible);
                });
            }

        };

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
     * Selects the specified page, by index.
     *
     * @param index Index of page to select
     */
    public void select(int index) {

        GUI.runNow(() -> switchers.get(index).run());

    }


    /**
     * Selects the specified page, by Element.
     *
     * @param element Element of page to select
     */
    public void select(Element element) {

        if (added.contains(element)) {
            select(added.indexOf(element));
        }

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
