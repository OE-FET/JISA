package jisa.gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Grid extends JFXElement implements Element, Container {

    private static final int DEFAULT_NUM_COLS = 3;

    public        GridPane           pane;
    public        ScrollPane         scroll;
    private       Stage              stage;
    private       int                nCols;
    private       int                r     = 0;
    private       int                c     = 0;
    private       boolean            hGrow = true;
    private       boolean            vGrow = true;
    private final ArrayList<Element> added = new ArrayList<>();

    /**
     * Creates a Grid element with the given title and number of columns.
     *
     * @param title      Title
     * @param numColumns Number of columns
     */
    public Grid(String title, int numColumns) {

        super(title, Grid.class.getResource("fxml/GridWindow.fxml"));
        BorderPane.setMargin(getNode().getCenter(), Insets.EMPTY);
        scroll.setPadding(new Insets(GUI.SPACING));
        nCols = numColumns;
        pane.setVgap(GUI.SPACING);
        pane.setHgap(GUI.SPACING);

    }

    /**
     * Creates a Grid element with the given title and the default number (3) of columns.
     *
     * @param title Title
     */
    public Grid(String title) {

        this(title, DEFAULT_NUM_COLS);

    }

    /**
     * Creates a Grid element with the given title, number of columns and adds the given elements to it as children.
     *
     * @param title    Title
     * @param numCols  Number of columns
     * @param children Children to add
     */
    public Grid(String title, int numCols, Element... children) {

        this(title, numCols);

        for (Element g : children) {
            add(g);
        }

    }

    /**
     * Creates a Grid element with the given title, 3 columns and adds the given elements to it as children.
     *
     * @param title    Title
     * @param children Children to add
     */
    public Grid(String title, Element... children) {

        this(title, DEFAULT_NUM_COLS, children);

    }

    public Grid(int numColumns, Element... children) {

        this("", numColumns, children);

    }

    public Grid(Element... panels) {

        this("", panels);

    }

    public void autoAdjustSize() {

        super.autoAdjustSize();

    }

    public void scrollToEnd() {
        scrollTo(1.0);
    }

    public void scrollToTop() {
        scrollTo(0.0);
    }

    public void scrollTo(double percentage) {

        GUI.runNow(() -> {

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(scroll.vvalueProperty(), scroll.getVvalue())),
                new KeyFrame(Duration.millis(250), new KeyValue(scroll.vvalueProperty(), percentage))
            );

            scroll.applyCss();
            scroll.layout();

            timeline.play();

        });

    }


    /**
     * Adds the given GUI element as a child of this grid, placing it as a panel in the next available space.
     *
     * @param toAdd Element to add
     */
    public void add(Element toAdd) {

        added.add(toAdd);
        addPane(toAdd.getBorderedNode());

    }

    public Node getBorderedNode() {

        BorderPane border = new BorderPane();
        border.setTop(getNode().getTop());
        border.setBottom(getNode().getBottom());
        border.setCenter(((ScrollPane) getNode().getCenter()).getContent());

        return border;
    }

    protected void addPane(Node toAdd) {

        GUI.runNow(() -> {

            pane.add(toAdd, c, r);

            GridPane.setHgrow(toAdd, hGrow ? Priority.ALWAYS : Priority.NEVER);
            GridPane.setVgrow(toAdd, vGrow ? Priority.ALWAYS : Priority.NEVER);

            c++;
            if (c >= nCols) {
                c = 0;
                r++;
            }

        });

    }

    private void updateGridding() {

        GUI.runNow(() -> {

            r = 0;
            c = 0;

            for (Node node : pane.getChildren()) {

                GridPane.setRowIndex(node, r);
                GridPane.setColumnIndex(node, c);

                c++;

                if (c >= nCols) {

                    c = 0;
                    r++;

                }

            }

        });

    }

    public void remove(Element toRemove) {

        GUI.runNow(() -> {

            int index = added.indexOf(toRemove);

            if (index < 0) {
                return;
            }

            added.remove(index);
            Node node = pane.getChildren().remove(index);

        });

        updateGridding();

    }

    public void slideOutElement(Element toRemove) {

        int index = added.indexOf(toRemove);

        if (index < 0) {
            return;
        }

        Timeline animation = new Timeline();
        Pane     child     = (Pane) pane.getChildren().get(index);

        animation.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(child.maxWidthProperty(), child.getWidth()), new KeyValue(child.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(250), new KeyValue(child.maxWidthProperty(), 0.0), new KeyValue(child.opacityProperty(), 0.0))
        );

        animation.setOnFinished(event -> {
            remove(toRemove);
            child.setOpacity(1.0);
            child.setMaxWidth(Region.USE_PREF_SIZE);
        });

        Platform.runLater(animation::playFromStart);

    }

    public void slideInElement(Element toAdd) {

        Timeline animation = new Timeline();
        Pane     child     = (Pane) toAdd.getBorderedNode();

        child.setMaxWidth(0.0);
        child.setOpacity(0.0);

        animation.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(child.maxWidthProperty(), 0.0), new KeyValue(child.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(250), new KeyValue(child.maxWidthProperty(), Region.USE_PREF_SIZE), new KeyValue(child.opacityProperty(), 1.0))
        );

        added.add(toAdd);
        addPane(child);

        Platform.runLater(animation::playFromStart);

    }

    public void clear() {

        GUI.runNow(() -> {

            added.clear();
            pane.getChildren().clear();

        });

        c = 0;
        r = 0;

    }

    @Override
    public List<Element> getElements() {
        return new ArrayList<>(added);
    }

    public int getNumColumns() {
        return nCols;
    }

    public void setNumColumns(int columns) {

        nCols = columns;
        updateGridding();
    }

    public void setGrowth(boolean horizontal, boolean vertical) {

        GUI.runNow(() -> {

            hGrow = horizontal;
            vGrow = vertical;

            for (Node node : pane.getChildren()) {

                GridPane.setHgrow(node, hGrow ? Priority.ALWAYS : Priority.NEVER);
                GridPane.setVgrow(node, vGrow ? Priority.ALWAYS : Priority.NEVER);

            }

        });

    }

    public void setGrowth(Element element, boolean horizontal, boolean vertical) {

        GUI.runNow(() -> {

            int index = added.indexOf(element);
            if (index != -1) {
                GridPane.setHgrow(pane.getChildren().get(index), horizontal ? Priority.ALWAYS : Priority.NEVER);
                GridPane.setVgrow(pane.getChildren().get(index), vertical ? Priority.ALWAYS : Priority.NEVER);
            }

        });

    }

}
