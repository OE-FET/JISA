package JISA.GUI;

import JISA.Control.ConfigStore;
import JISA.Control.Field;
import JISA.Control.SRunnable;
import JISA.Util;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Fields extends JFXWindow implements Element, Iterable<Field> {

    public  BorderPane                       pane;
    public  GridPane                         list;
    public  ButtonBar                        buttonBar;
    private LinkedHashMap<String, TextField> map    = new LinkedHashMap<>();
    private List<Field>                      fields = new LinkedList<>();
    private ConfigStore                      config = null;
    private String                           tag    = null;
    private int                              rows   = 0;

    /**
     * Creates a input fields group for user-input.
     *
     * @param title Title of the window/grid-element.
     */
    public Fields(String title) {
        super(title, Fields.class.getResource("FXML/InputWindow.fxml"));
    }

    public void loadFromConfig(String tag, ConfigStore configStore) {
        loadFromConfig(tag, configStore, true);
    }

    public void loadFromConfig(String tag, ConfigStore configStore, boolean save) {

        configStore.loadFields(tag, this);

        if (save) {

            this.config = configStore;
            this.tag    = tag;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                try {
                    configStore.saveFields(tag, Fields.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }));

        }

    }

    /**
     * Add a simple text box to the fields group. Accepts any string.
     *
     * @param name         Name of the field
     * @param initialValue Initial value to show in the text-box
     *
     * @return Reference object (SetGettable) to set and get the value of the text-box
     */
    public Field<String> addTextField(String name, String initialValue) {


        TextField field = new TextField(initialValue);
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        Field<String> f = new Field<String>() {

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
                    (new Thread(() -> {
                        try {
                            onChange.run();
                        } catch (Exception e) {
                            Util.exceptionHandler(e);
                        }
                    })).start();
                };
                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }


    /**
     * Add a simple text box to the fields group. Accepts any string.
     *
     * @param name Name of the field
     *
     * @return Reference object (SetGettable) to set and get the value of the text-box
     */
    public Field<String> addTextField(String name) {
        return addTextField(name, "");
    }


    /**
     * Adds a check-box to the fields group. Provides boolean user input.
     *
     * @param name         Name of the field
     * @param initialValue Initial state of the check-box
     *
     * @return Reference object to set and get the value (true or false) of the check-box
     */
    public Field<Boolean> addCheckBox(String name, boolean initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        CheckBox field = new CheckBox();
        field.setText(name);
        field.setSelected(initialValue);
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label();
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));


        Field<Boolean> f = new Field<Boolean>() {

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
                    (new Thread(() -> {
                        try {
                            onChange.run();
                        } catch (Exception e) {
                            Util.exceptionHandler(e);
                        }
                    })).start();
                };

                field.selectedProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a check-box to the fields group. Provides boolean user input.
     *
     * @param name Name of the field
     *
     * @return Reference object to set and get the value (true or false) of the check-box
     */
    public Field<Boolean> addCheckBox(String name) {
        return addCheckBox(name, false);
    }

    /**
     * Adds a text-box with a "browse" button for selecting a file save location.
     *
     * @param name         Name of the field
     * @param initialValue Initial value to display
     *
     * @return SetGettable object that can set or get the selected file path as a String
     */
    public Field<String> addFileSave(String name, String initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field = new TextField(initialValue);
        Label     label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        Button button = new Button("Browse...");
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnAction(actionEvent -> {
            String file = GUI.saveFileSelect();
            if (file != null) {
                field.setText(file);
            }
        });
        field.setMaxWidth(Integer.MAX_VALUE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        HBox inner = new HBox(field, button);
        HBox.setHgrow(field, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.NEVER);
        inner.setSpacing(15);

        GUI.runNow(() -> list.addRow(rows++, label, inner));


        Field<String> f = new Field<String>() {

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
                    (new Thread(() -> {
                        try {
                            onChange.run();
                        } catch (Exception e) {
                            Util.exceptionHandler(e);
                        }
                    })).start();
                };

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
                button.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a text-box with a "browse" button for selecting a file save location.
     *
     * @param name Name of the field
     *
     * @return SetGettable object that can set or get the selected file path as a String
     */
    public Field<String> addFileSave(String name) {
        return addFileSave(name, "");
    }

    public Field<String> addDirectorySelect(String name, String initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field = new TextField(initialValue);
        Label     label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        Button button = new Button("Browse...");
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnAction(actionEvent -> {
            String file = GUI.directorySelect();
            if (file != null) {
                field.setText(file);
            }
        });
        field.setMaxWidth(Integer.MAX_VALUE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        HBox inner = new HBox(field, button);
        HBox.setHgrow(field, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.NEVER);
        inner.setSpacing(15);

        GUI.runNow(() -> list.addRow(rows++, label, inner));


        Field<String> f = new Field<String>() {

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
                    (new Thread(() -> {
                        try {
                            onChange.run();
                        } catch (Exception e) {
                            Util.exceptionHandler(e);
                        }
                    })).start();
                };

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
                button.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    public Field<String> addDirectorySelect(String name) {
        return addDirectorySelect(name, "");
    }

    /**
     * Adds a text-box with a "browse" button for selecting a file for opening.
     *
     * @param name Name of the field
     *
     * @return SetGettable object that can set or get the selected file path as a String
     */
    public Field<String> addFileOpen(String name, String initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field = new TextField(initialValue);
        Label     label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        Button button = new Button("Browse...");
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnAction(actionEvent -> {
            String file = GUI.openFileSelect();
            if (file != null) {
                field.setText(file);
            }
        });
        field.setMaxWidth(Integer.MAX_VALUE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        HBox inner = new HBox(field, button);
        HBox.setHgrow(field, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.NEVER);
        inner.setSpacing(15);

        GUI.runNow(() -> list.addRow(rows++, label, inner));

        Field<String> f = new Field<String>() {

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
                    (new Thread(() -> {
                        try {
                            onChange.run();
                        } catch (Exception e) {
                            Util.exceptionHandler(e);
                        }
                    })).start();
                };

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
                button.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a text-box with a "browse" button for selecting a file for opening.
     *
     * @param name Name of the field
     *
     * @return SetGettable object that can set or get the selected file path as a String
     */
    public Field<String> addFileOpen(String name) {
        return addFileOpen(name, "");
    }

    /**
     * Adds a text box that only accepts integer values.
     *
     * @param name         Name of the field
     * @param initialValue Initial value to display
     *
     * @return SetGettable to set or get the value as an integer
     */
    public Field<Integer> addIntegerField(String name, int initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        IntegerField field = new IntegerField();
        field.setText(String.valueOf(initialValue));
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        Field<Integer> f = new Field<Integer>() {

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
                    (new Thread(() -> {
                        try {
                            onChange.run();
                        } catch (Exception e) {
                            Util.exceptionHandler(e);
                        }
                    })).start();
                };

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a text box that only accepts integer values.
     *
     * @param name Name of the field
     *
     * @return SetGettable to set or get the value as an integer
     */
    public Field<Integer> addIntegerField(String name) {
        return addIntegerField(name, 0);
    }

    /**
     * Adds a text box that only accepts numerical (decimal, floating point) values.
     *
     * @param name         Name of the field
     * @param initialValue Initial value to display
     *
     * @return SetGettable to set or get the value as a double
     */
    public Field<Double> addDoubleField(String name, double initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        DoubleInput field = new DoubleInput();
        field.setValue(initialValue);
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        Field<Double> f = new Field<Double>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(Double value) {
                field.setValue(value);
            }

            @Override
            public Double get() {
                return field.getValue();
            }

            @Override
            public void setOnChange(SRunnable onChange) {
                field.setOnChange((v) -> {
                    (new Thread(() -> {
                        try {
                            onChange.run();
                        } catch (Exception e) {
                            Util.exceptionHandler(e);
                        }
                    })).start();
                });
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {
                return field.disabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                field.disabled(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a text box that only accepts numerical (decimal, floating point) values.
     *
     * @param name Name of the field
     *
     * @return SetGettable to set or get the value as a double
     */
    public Field<Double> addDoubleField(String name) {
        return addDoubleField(name, 0.0);
    }

    /**
     * Adds a drop-down box with the specified choices.
     *
     * @param name         Name of the field
     * @param initialValue Index of option to initially have selected
     * @param options      Array of names for the options
     *
     * @return SetGettable to set and get the selected value, represented as an integer (0 = first option, 1 = second option etc)
     */
    public Field<Integer> addChoice(String name, int initialValue, String... options) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        ChoiceBox<String> field = new ChoiceBox<>();
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        field.getItems().addAll(options);
        field.getSelectionModel().select(initialValue);

        Field<Integer> f = new Field<Integer>() {

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
                    (new Thread(() -> {
                        try {
                            onChange.run();
                        } catch (Exception e) {
                            Util.exceptionHandler(e);
                        }
                    })).start();
                };

                field.getSelectionModel().selectedIndexProperty().addListener(list);
            }

            @Override
            public synchronized void editValues(String... values) {

                GUI.runNow(() -> {

                    int selected = field.getSelectionModel().getSelectedIndex();

                    if (list != null) {
                        field.getSelectionModel().selectedIndexProperty().removeListener(list);
                        field.setItems(FXCollections.observableArrayList(values));
                        field.getSelectionModel().select(Math.min(values.length - 1, Math.max(0, selected)));
                        field.getSelectionModel().selectedIndexProperty().addListener(list);
                    } else {
                        field.setItems(FXCollections.observableArrayList(values));
                        field.getSelectionModel().select(Math.min(values.length - 1, Math.max(0, selected)));
                    }

                });
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

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
        return addChoice(name, 0, options);
    }

    @Override
    public Pane getPane() {
        return pane;
    }


    public void addSeparator() {

        Separator separator = new Separator();
        GUI.runNow(() -> list.addRow(rows++, separator));
        GridPane.setColumnSpan(separator, 2);
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
        GUI.runNow(() -> buttonBar.getButtons().add(button));

    }

    public void setFieldsDisabled(boolean flag) {

        for (Field f : fields) {
            f.setDisabled(flag);
        }

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

        stage.setOnCloseRequest(we -> {
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

    @Override
    public Iterator<Field> iterator() {
        return fields.iterator();
    }
}
