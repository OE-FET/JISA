package JISA.GUI;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Grid extends JFXWindow implements Element, Container {

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

        if (toAdd instanceof Grid) {
            ((Grid) toAdd).pane.setPadding(new Insets(0, 0, 0, 0));
            bPane = ((Grid) toAdd).pane;
        } else {

            bPane = new BorderPane();
            StackPane stack     = new StackPane();
            StackPane container = new StackPane();
            Label     t         = new Label();

            stack.setPadding(new Insets(10, 10, 10, 10));
            stack.setAlignment(Pos.CENTER_LEFT);
            stack.setStyle("-fx-background-color: #4c4c4c; -fx-background-radius: 5px 5px 0 0;");
            bPane.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
            bPane.setEffect(new DropShadow(10, new Color(0, 0, 0, 0.25)));
            t.setFont(new Font("System Bold", 14));
            t.setTextFill(Color.WHITE);
            t.setText(toAdd.getTitle());
            stack.getChildren().add(t);
            container.setPadding(new Insets(15, 15, 15, 15));
            toAdd.getPane().setStyle("-fx-background-color: transparent;");
            container.getChildren().add(toAdd.getPane());
            ((BorderPane) bPane).setTop(stack);
            ((BorderPane) bPane).setCenter(container);

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

}
