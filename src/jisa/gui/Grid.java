package jisa.gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Grid extends JFXWindow implements Element, Container, NotBordered {

    private static final int DEFAULT_NUM_COLS = 3;

    public  GridPane           pane;
    public  ScrollPane         scroll;
    public  BorderPane         border;
    public  ButtonBar          buttonBar;
    private ToolBar            toolBar = null;
    private Stage              stage;
    private int                nCols;
    private int                r       = 0;
    private int                c       = 0;
    private boolean            hGrow   = true;
    private boolean            vGrow   = true;
    private ArrayList<Element> added   = new ArrayList<>();

    /**
     * Creates a Grid element with the given title and number of columns.
     *
     * @param title      Title
     * @param numColumns Number of columns
     */
    public Grid(String title, int numColumns) {

        super(title, Grid.class.getResource("fxml/GridWindow.fxml"));
        nCols = numColumns;

        buttonBar.getButtons().addListener((InvalidationListener) change -> GUI.runNow(() -> {
            boolean show = !buttonBar.getButtons().isEmpty();
            buttonBar.setVisible(show);
            buttonBar.setManaged(show);
        }));

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

    /**
     * Shows the fields as its own window, with an "OK" and "Cancel" button. Does not return until the window has been
     * closed either by clicking "OK" or "Cancel" or closing the window. Returns a boolean indicating whether "OK" was
     * clicked or not.
     *
     * @return Was "OK" clicked?
     */
    public boolean showAndWait() {

        final Semaphore     semaphore = new Semaphore(0);
        final AtomicBoolean result    = new AtomicBoolean(false);

        Button okay   = new Button("OK");
        Button cancel = new Button("Cancel");

        okay.setOnAction(ae -> {
            result.set(true);
            semaphore.release();
        });

        cancel.setOnAction(ae -> {
            result.set(false);
            semaphore.release();
        });

        GUI.runNow(() -> buttonBar.getButtons().addAll(cancel, okay));

        setOnClose(() -> {
            result.set(false);
            semaphore.release();
        });

        show();

        try {
            semaphore.acquire();
        } catch (Exception ignored) {
        }

        close();

        GUI.runNow(() -> buttonBar.getButtons().removeAll(cancel, okay));

        return result.get();

    }

    /**
     * Adds a button to a toolbar at the top of the grid.
     *
     * @param text    Text to display in the button
     * @param onClick Action to perform when clicked
     *
     * @return Newly created button sub-element object
     */
    public jisa.gui.Button addToolbarButton(String text, ClickHandler onClick) {

        Button button = new Button();

        Platform.runLater(() -> {
            if (toolBar == null) {
                toolBar = new ToolBar();
                border.setTop(toolBar);
            }
            button.setText(text);
            button.setOnAction(
                (actionEvent) -> {
                    Thread t = new Thread(() -> {
                        try {
                            onClick.click();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    t.setDaemon(true);
                    t.start();
                }
            );

            toolBar.getItems().add(button);
        });

        return new jisa.gui.Button() {

            @Override
            public boolean isDisabled() {
                return button.isDisabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                GUI.runNow(() -> button.setDisable(disabled));
            }

            @Override
            public boolean isVisible() {
                return button.isVisible();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    button.setVisible(visible);
                    button.setManaged(visible);
                });

            }

            @Override
            public String getText() {
                return button.getText();
            }

            @Override
            public void setText(String text) {
                GUI.runNow(() -> button.setText(text));
            }

            @Override
            public void setOnClick(ClickHandler onClick) {

                GUI.runNow(() -> button.setOnAction(
                    (actionEvent) -> {
                        Thread t = new Thread(() -> {
                            try {
                                onClick.click();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        t.setDaemon(true);
                        t.start();
                    }
                ));

            }

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getItems().remove(button));
            }


        };

    }

    /**
     * Adds a separator to the toolbar at the top of the Grid.
     *
     * @return Newly created separator sub-element object
     */
    public jisa.gui.Separator addToolbarSeparator() {

        Separator separator = new Separator();

        Platform.runLater(() -> {

            if (toolBar == null) {
                toolBar = new ToolBar();
                border.setTop(toolBar);
            }

            toolBar.getItems().add(separator);

        });

        return new jisa.gui.Separator() {

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getItems().remove(separator));
            }

            @Override
            public boolean isVisible() {
                return separator.isVisible();
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> separator.setVisible(visible));
            }


        };

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

    public void scrollToElement(Element element, double spacing) {

        GUI.runNow(() -> {
            scroll.applyCss();
            scroll.layout();
        });

        Node   node          = element.getPane();
        Bounds viewport      = scroll.getViewportBounds();
        double contentHeight = scroll.getContent().localToScene(scroll.getContent().getBoundsInLocal()).getHeight();
        double nodeMinY      = node.localToScene(node.getBoundsInLocal()).getMinY();
        double nodeMaxY      = node.localToScene(node.getBoundsInLocal()).getMaxY();

        double vValueDelta   = 0;
        double vValueCurrent = scroll.getVvalue();

        if (nodeMaxY < 0) {
            vValueDelta = ((nodeMinY - spacing) - viewport.getHeight()) / contentHeight;
        } else if (nodeMinY > viewport.getHeight()) {
            vValueDelta = ((nodeMinY - spacing) + viewport.getHeight() - spacing) / contentHeight;
        }

        scrollTo(vValueCurrent + vValueDelta);

    }

    public void scrollToElement(Element element) { scrollToElement(element, 0.0);}


    /**
     * Adds the given GUI element as a child of this grid, placing it as a panel in the next available space.
     *
     * @param toAdd Element to add
     */
    public void add(Element toAdd) {

        added.add(toAdd);
        addPane(makePane(toAdd));

    }

    protected Pane makePane(Element toAdd) {
        Pane bPane;

        if (toAdd instanceof NotBordered) {

            bPane = ((NotBordered) toAdd).getNoBorderPane(true);

        } else if (toAdd instanceof Connector) {

            bPane = ((Connector) toAdd).pane;

        } else {

            bPane = new BorderPane();

            toAdd.getPane().setStyle("-fx-background-color: transparent;");
            StackPane sPane = new StackPane(toAdd.getPane());
            sPane.setMaxHeight(Double.MAX_VALUE);
            sPane.setMaxWidth(Double.MAX_VALUE);
            sPane.setStyle("-fx-background-color: white;");
            TitledPane tPane = new TitledPane(toAdd.getTitle(), sPane);
            tPane.setCollapsible(false);
            tPane.setMaxHeight(Double.MAX_VALUE);
            tPane.setMaxWidth(Double.MAX_VALUE);
            sPane.setPadding(new Insets(0, 0, 0, 0));
            ((BorderPane) bPane).setCenter(tPane);

        }

        return bPane;

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

    public Pane getPane() {

        return border;

    }

    @Override
    public Pane getNoBorderPane(boolean strip) {

        if (strip) pane.setPadding(new Insets(0, 0, 0, 0));

        return pane;

    }

}
