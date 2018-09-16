package JISA.GUI;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;

public class GridWindow {

    public  GridPane   pane;
    public  BorderPane border;
    private ToolBar    toolBar = null;
    private Stage      stage;
    private int        nCols   = 3;
    private int        r       = 0;
    private int        c       = 0;

    public static GridWindow create(String title) {

        try {
            FXMLLoader loader     = new FXMLLoader(TableWindow.class.getResource("FXML/GridWindow.fxml"));
            Parent     root       = loader.load();
            Scene      scene      = new Scene(root);
            GridWindow controller = (GridWindow) loader.getController();
            Platform.runLater(() -> {
                Stage stage = new Stage();
                controller.stage = stage;
                stage.setTitle(title);
                stage.setScene(scene);
            });
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return new GridWindow();
        }

    }

    public void addToolbarButton(String text, ClickHandler onClick) {

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

        toolBar.getItems().

                add(button);

    }


    public void addPane(Gridable item) {

        Pane toAdd = item.getPane();

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

}
