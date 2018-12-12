package JISA.GUI;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.concurrent.Semaphore;

public class Grid implements Gridable {

    public  GridPane   pane;
    public  BorderPane border;
    private ToolBar    toolBar = null;
    private Stage      stage;
    private int        nCols   = 3;
    private int        r       = 0;
    private int        c       = 0;

    public Grid(String title) {

        try {

            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("FXML/GridWindow.fxml"));
            loader.setController(this);
            Parent root  = loader.load();
            Scene  scene = new Scene(root);

            Semaphore semaphore = new Semaphore(0);

            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                this.stage = stage;
                semaphore.release();
            });

            semaphore.acquire();

        } catch (Exception e) {

        }

    }

    public Grid(String title, Gridable... panels) {
        this(title);
        for (Gridable g : panels) {
            add(g);
        }
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

    public void add(Gridable toAdd) {

        BorderPane bPane     = new BorderPane();
        StackPane  stack     = new StackPane();
        StackPane  container = new StackPane();
        Label      t         = new Label();

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
        bPane.setTop(stack);
        bPane.setCenter(container);

        addPane(bPane);
    }

    public void setNumColumns(int columns) {
        nCols = columns;
        updateGridding();
    }

    private void updateGridding() {

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

    }

    public int getNumColumns() {
        return nCols;
    }

    public void addPane(Node toAdd) {

        pane.add(toAdd, c, r);

        GridPane.setHgrow(toAdd, Priority.ALWAYS);
        GridPane.setVgrow(toAdd, Priority.ALWAYS);

        c++;
        if (c >= nCols) {
            c = 0;
            r++;
        }

    }

    public void show() {
        Platform.runLater(() -> {
            stage.show();
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            stage.hide();
        });
    }

    public void close() {
        Platform.runLater(() -> {
            stage.close();
        });
    }

    public Pane getPane() {
        return border;
    }

    @Override
    public String getTitle() {
        return stage.getTitle();
    }
}
