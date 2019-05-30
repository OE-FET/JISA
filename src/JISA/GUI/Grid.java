package JISA.GUI;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.python.antlr.op.Not;

import java.util.ArrayList;
import java.util.List;

public class Grid extends JFXWindow implements Element, Container, NotBordered {

    private static final int DEFAULT_NUM_COLS = 3;

    public  GridPane           pane;
    public  ScrollPane         scroll;
    public  BorderPane         border;
    private ToolBar            toolBar = null;
    private Stage              stage;
    private int                nCols;
    private int                r       = 0;
    private int                c       = 0;
    private boolean            hGrow   = true;
    private boolean            vGrow   = true;
    private ArrayList<Element> added   = new ArrayList<>();

    public Grid(String title, int numColumns) {
        super(title, Grid.class.getResource("FXML/GridWindow.fxml"));
        nCols = numColumns;
    }

    public Grid(String title) {
        this(title, DEFAULT_NUM_COLS);
    }

    public Grid(String title, int numCols, Element... panels) {
        this(title, numCols);
        for (Element g : panels) {
            add(g);
        }
    }

    public Grid(String title, Element... panels) {
        this(title, DEFAULT_NUM_COLS, panels);
    }

    public void addToolbarButton(String text, ClickHandler onClick) {

        Platform.runLater(() -> {
            if (toolBar == null) {
                toolBar = new ToolBar();
                border.setTop(toolBar);
            }

            Button button = new Button();
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

    }

    public void add(Element toAdd) {

        Pane bPane;

        if (toAdd instanceof NotBordered) {
            bPane = ((NotBordered) toAdd).getNoBorderPane(true);
        } else if (toAdd instanceof InstrumentConfig) {
            bPane = ((InstrumentConfig) toAdd).pane;
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
            ((BorderPane) bPane).setCenter(tPane);

        }

        added.add(toAdd);
        addPane(bPane);
    }

    public void setNumColumns(int columns) {
        nCols = columns;
        updateGridding();
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

    }

    @Override
    public List<Element> getElements() {
        return new ArrayList<>(added);
    }

    public int getNumColumns() {
        return nCols;
    }

    public void addPane(Node toAdd) {

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
        if (strip) {
            pane.setPadding(new Insets(0, 0, 0, 0));
        }
        return pane;
    }
}
