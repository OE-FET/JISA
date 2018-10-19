package JISA.GUI;

import JISA.GUI.FXML.InputWindow;
import JISA.GUI.Gridable;
import JISA.Util;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.LinkedHashMap;

public class Input implements Gridable {

    private Stage                            stage;
    private LinkedHashMap<String, TextField> map = new LinkedHashMap<>();
    private AcceptHandler                    onAccept;
    public  BorderPane                       pane;
    public  VBox                             list;

    public Input(String title, AcceptHandler onAccept) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/InputWindow.fxml"));
            loader.setController(this);
            Parent root  = loader.load();
            Scene  scene = new Scene(root);
            this.onAccept = onAccept;
            GUI.runNow(() -> {
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                this.stage = stage;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addField(String index, String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.control.TextField field = new javafx.scene.control.TextField();
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(150);
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(label, field);
        list.getChildren().add(box);

        map.put(index, field);

    }


    public void addFileSave(String index, String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field  = new TextField();
        Label     label  = new Label(name);
        Button    button = new Button();
        button.setText("Browse...");
        button.setOnAction(actionEvent -> {
            String file = GUI.saveFileSelect();
            if (file != null) {
                field.setText(file);
            }
        });
        field.setMaxWidth(Integer.MAX_VALUE);
        label.setMinWidth(150);
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(label, field, button);
        list.getChildren().add(box);


        map.put(index, field);

    }

    public void addFileOpen(String index, String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field  = new TextField();
        Label     label  = new Label(name);
        Button    button = new Button();
        button.setText("Browse...");
        button.setOnAction(actionEvent -> {
            String file = GUI.openFileSelect();
            if (file != null) {
                field.setText(file);
            }
        });
        field.setMaxWidth(Integer.MAX_VALUE);
        label.setMinWidth(150);
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(label, field, button);
        list.getChildren().add(box);


        map.put(index, field);

    }

    public void setVariables() {

        try {
            onAccept.onAccept(this);
        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public String getTitle() {
        return stage.getTitle();
    }

    public String get(String index) {

        if (map.containsKey(index)) {
            return map.get(index).getText();
        } else {
            return null;
        }

    }

    public int getInt(String index) {
        return Integer.valueOf(get(index));
    }

    public double getDouble(String index) {
        return Double.valueOf(get(index));
    }

    public void show() {
        GUI.runNow(() -> stage.show());
    }

    public void hide() {
        GUI.runNow(() -> stage.hide());
    }

    public void close() {
        GUI.runNow(() -> stage.close());
    }

    public interface AcceptHandler {
        void onAccept(Input input) throws Exception;
    }

}
