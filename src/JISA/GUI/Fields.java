package JISA.GUI;

import JISA.Control.Field;
import JISA.Control.SRunnable;
import JISA.Control.SetGettable;
import JISA.Devices.DeviceException;
import JISA.Util;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class Fields extends JFXWindow implements Gridable {

    private LinkedHashMap<String, TextField> map = new LinkedHashMap<>();
    public  BorderPane                       pane;
    public  VBox                             list;
    public  ButtonBar                        buttonBar;

    /**
     * Creates a input fields group for user-input.
     *
     * @param title Title of the window/grid-element.
     */
    public Fields(String title) {
        super(title, Fields.class.getResource("FXML/InputWindow.fxml"));
    }

    /**
     * Add a simple text box to the fields group. Accepts any string.
     *
     * @param name Name of the field
     *
     * @return Reference object (SetGettable) to set and get the value of the text-box
     */
    public Field<String> addTextField(String name) {

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


        return new Field<String>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(String value) {
                field.setText(value);
            }

            @Override
            public String get() {
                return field.getText();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                };
                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }
        };

    }

    /**
     * Adds a check-box to the fields group. Provides boolean user input.
     *
     * @param name Name of the field
     *
     * @return Reference object to set and get the value (true or false) of the check-box
     */
    public Field<Boolean> addCheckBox(String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        CheckBox field = new CheckBox();
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(150);
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(label, field);
        list.getChildren().add(box);


        return new Field<Boolean>() {

            private ChangeListener<Boolean> list = null;

            @Override
            public void set(Boolean value) {
                field.setSelected(value);
            }

            @Override
            public Boolean get() {
                return field.isSelected();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.selectedProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                };

                field.selectedProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }
        };

    }

    /**
     * Adds a text-box with a "browse" button for selecting a file save location.
     *
     * @param name Name of the field
     *
     * @return SetGettable object that can set or get the selected file path as a String
     */
    public Field<String> addFileSave(String name) {

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


        return new Field<String>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(String value) {
                field.setText(value);
            }

            @Override
            public String get() {
                return field.getText();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                };

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
                button.setDisable(disabled);
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }
        };

    }

    /**
     * Adds a text-box with a "browse" button for selecting a file for opening.
     *
     * @param name Name of the field
     *
     * @return SetGettable object that can set or get the selected file path as a String
     */
    public Field<String> addFileOpen(String name) {

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

        return new Field<String>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(String value) {
                field.setText(value);
            }

            @Override
            public String get() {
                return field.getText();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                };

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
                button.setDisable(disabled);
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }
        };

    }

    /**
     * Adds a text box that only accepts integer values.
     *
     * @param name Name of the field
     *
     * @return SetGettable to set or get the value as an integer
     */
    public Field<Integer> addIntegerField(String name) {

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

        return new Field<Integer>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(Integer value) {
                field.setText(value.toString());
            }

            @Override
            public Integer get() {
                return field.getIntValue();
            }

            @Override
            public void setOnChange(SRunnable onChange) {
                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                };

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }
        };

    }

    /**
     * Adds a text box that only accepts numerical (decimal, floating point) values.
     *
     * @param name Name of the field
     *
     * @return SetGettable to set or get the value as a double
     */
    public Field<Double> addDoubleField(String name) {

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

        return new Field<Double>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(Double value) {
                field.setText(value.toString());
            }

            @Override
            public Double get() {
                return field.getDoubleValue();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                };

                field.textProperty().addListener(list);

            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }
        };

    }

    /**
     * Adds a drop-down box with the specified choices.
     *
     * @param name    Name of the field
     * @param options Array of names for the options
     *
     * @return SetGettable to set and get the selected value, represented as an integer (0 = first option, 1 = second option etc)
     */
    public Field<Integer> addChoice(String name, String... options) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        ChoiceBox<String> field = new ChoiceBox<>();
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(150);
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(label, field);
        list.getChildren().add(box);

        field.getItems().addAll(options);

        return new Field<Integer>() {

            private ChangeListener<Number> list = null;

            @Override
            public void set(Integer value) {
                field.getSelectionModel().select(value);
            }

            @Override
            public Integer get() {
                return field.getSelectionModel().getSelectedIndex();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.getSelectionModel().selectedIndexProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                };

                field.getSelectionModel().selectedIndexProperty().addListener(list);
            }

            @Override
            public synchronized void editValues(String... values) {

                int selected = field.getSelectionModel().getSelectedIndex();

                if (list != null) {
                    field.getSelectionModel().selectedIndexProperty().removeListener(list);
                    field.setItems(FXCollections.observableArrayList(values));
                    field.getSelectionModel().select(Math.min(values.length-1, Math.max(0,selected)));
                    field.getSelectionModel().selectedIndexProperty().addListener(list);
                } else {
                    field.setItems(FXCollections.observableArrayList(values));
                    field.getSelectionModel().select(Math.min(values.length-1, Math.max(0,selected)));
                }
            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }
        };
    }

    @Override
    public Pane getPane() {
        return pane;
    }


    public void addSeparator() {

        Separator separator = new Separator();
        list.getChildren().add(separator);
        VBox.setVgrow(separator, Priority.ALWAYS);

    }

    /**
     * Add a button to the bottom of the fields group.
     *
     * @param text    Text to display in the button
     * @param onClick Action to perform when clicked
     */
    public void addButton(String text, ClickHandler onClick) {

        Button button = new Button(text);

        button.setOnAction((ae) -> {
            (new Thread(() -> {
                try {
                    onClick.click();
                } catch (Exception e) {
                    Util.exceptionHandler(e);
                }
            })).start();
        });
        buttonBar.getButtons().add(button);

    }

}
