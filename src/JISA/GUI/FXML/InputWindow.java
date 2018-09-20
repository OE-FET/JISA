package JISA.GUI.FXML;

import JISA.GUI.ClickHandler;
import JISA.GUI.Gridable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class InputWindow implements Gridable {

    public  BorderPane                 pane;
    public  VBox                       list;
    public  Label                      title;
    private FileChooser                fileChooser = new FileChooser();
    private HashMap<String, TextField> map         = new HashMap<>();
    private Stage                      stage;
    private boolean                    close;
    private InputHandler               onAccept    = (v) -> {
    };

    public static InputWindow create(String title, boolean closeOnAccept, InputHandler onAccept) {

        try {
            FXMLLoader  loader     = new FXMLLoader(TableWindow.class.getResource("InputWindow.fxml"));
            Parent      root       = loader.load();
            Scene       scene      = new Scene(root);
            InputWindow controller = (InputWindow) loader.getController();
            controller.onAccept = onAccept;
            controller.close = closeOnAccept;
            Platform.runLater(() -> {
                Stage stage = new Stage();
                controller.stage = stage;
                controller.title.setText(title);
                stage.setTitle(title);
                stage.setScene(scene);
            });
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return new InputWindow();
        }

    }

    public void setVariables(ActionEvent actionEvent) {

        Thread t = new Thread(() -> {

            HashMap<String, String> values = new HashMap<>();
            map.forEach((k, f) -> {
                values.put(k, f.getText());
            });

            try {
                onAccept.run(values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t.setDaemon(true);
        t.start();

        if (close) {
            close();
        }

    }


    public void addField(String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field = new TextField();
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label();
        label.setMinWidth(150);
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(label, field);
        list.getChildren().add(box);

        label.setText(name);

        map.put(name, field);

    }

    public void addFileSave(String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field  = new TextField();
        Label     label  = new Label();
        Button    button = new Button();
        button.setText("Browse...");
        button.setOnAction(actionEvent -> {
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                field.setText(file.getAbsolutePath());
            }
        });
        field.setMaxWidth(Integer.MAX_VALUE);
        label.setMinWidth(150);
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(label, field, button);
        list.getChildren().add(box);

        label.setText(name);

        map.put(name, field);

    }

    @Override
    public Pane getPane() {
        return pane;
    }

    public interface InputHandler {

        public void run(HashMap<String, String> values) throws Exception;

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
