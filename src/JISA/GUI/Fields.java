package JISA.GUI;

import JISA.Control.SetGettable;
import JISA.Devices.DeviceException;
import JISA.Util;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

public class Fields implements Gridable {

    private Stage                            stage;
    private LinkedHashMap<String, TextField> map = new LinkedHashMap<>();
    public  BorderPane                       pane;
    public  VBox                             list;
    public  ButtonBar                        buttonBar;

    public Fields(String title) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/InputWindow.fxml"));
            loader.setController(this);
            Parent root  = loader.load();
            Scene  scene = new Scene(root);
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

    public SetGettable<String> addTextField(String name) {

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


        return new SetGettable<String>() {
            @Override
            public void set(String value) throws IOException, DeviceException {
                field.setText(value);
            }

            @Override
            public String get() throws IOException, DeviceException {
                return field.getText();
            }
        };

    }


    public SetGettable<String> addFileSave(String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field  = new TextField();
        Label     label  = new Label(name);
        Button    button = new Button("Browse...");
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


        return new SetGettable<String>() {
            @Override
            public void set(String value) throws IOException, DeviceException {
                field.setText(value);
            }

            @Override
            public String get() throws IOException, DeviceException {
                return field.getText();
            }
        };

    }

    public SetGettable<String> addFileOpen(String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field  = new TextField();
        Label     label  = new Label(name);
        Button    button = new Button("Browse...");
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

        return new SetGettable<String>() {
            @Override
            public void set(String value) throws IOException, DeviceException {
                field.setText(value);
            }

            @Override
            public String get() throws IOException, DeviceException {
                return field.getText();
            }
        };

    }

    public SetGettable<Integer> addIntegerField(String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        IntegerField field = new IntegerField();
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(150);
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(label, field);
        list.getChildren().add(box);

        return new SetGettable<Integer>() {
            @Override
            public void set(Integer value) throws IOException, DeviceException {
                field.setText(value.toString());
            }

            @Override
            public Integer get() throws IOException, DeviceException {
                return Integer.valueOf(field.getText());
            }
        };

    }

    public SetGettable<Double> addDoubleField(String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        DoubleField field = new DoubleField();
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(150);
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(label, field);
        list.getChildren().add(box);

        return new SetGettable<Double>() {
            @Override
            public void set(Double value) throws IOException, DeviceException {
                field.setText(value.toString());
            }

            @Override
            public Double get() throws IOException, DeviceException {
                return field.getDoubleValue();
            }
        };

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

    public void addButton(String text, ClickHandler onClick) {

        Button button = new Button(text);

        button.setOnAction((ae) -> {
            try {
                onClick.click();
            } catch (Exception e) {
                Util.exceptionHandler(e);
            }
        });

        buttonBar.getButtons().add(button);

    }

}
