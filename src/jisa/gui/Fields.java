package jisa.gui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Separator;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jisa.Util;
import jisa.control.ConfigBlock;
import jisa.control.SRunnable;
import jisa.gui.fields.DoubleField;
import jisa.gui.fields.StringField;
import jisa.gui.fields.TimeField;
import jisa.maths.Range;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Fields extends JFXElement implements Element, Iterable<Field<?>> {

    public        BorderPane     pane;
    public        GridPane       list;
    private final List<Field<?>> fields = new LinkedList<>();
    private       int            rows   = 0;

    /**
     * Creates a input fields group for user-input.
     *
     * @param title Title of the window/grid-element.
     */
    public Fields(String title) {

        super(title, Fields.class.getResource("fxml/InputWindow.fxml"));
        BorderPane.setMargin(getNode().getCenter(), new Insets(15.0));

    }

    @SuppressWarnings("unchecked")
    public void linkToConfig(ConfigBlock config) {

        loadFromConfig(config);
        Util.addShutdownHook(() -> writeToConfig(config));

    }

    public void writeToConfig(ConfigBlock block) {

        for (Field field : fields) {

            Class type = field.get().getClass();

            if (type == Double.class) {
                block.doubleValue(field.getText()).set(field.getValue());
            } else if (type == Integer.class) {
                block.intValue(field.getText()).set(field.getValue());
            } else if (type == Boolean.class) {
                block.booleanValue(field.getText()).set(field.getValue());
            } else if (type == String.class) {
                block.stringValue(field.getText()).set(field.getValue());
            } else if (type == Range.class) {

                Range<Double> range = (Range<Double>) field.getValue();
                ConfigBlock   sub   = block.subBlock(field.getText());
                sub.clear();

                for (int i = 0; i < range.size(); i++) {
                    sub.doubleValue(String.valueOf(i)).set(range.get(i));
                }

                field.writeOtherDefaults(sub);

            }

        }

        block.save();

    }

    public void loadFromConfig(ConfigBlock block) {

        for (Field field : fields) {

            if (!block.hasValue(field.getText())) {
                continue;
            }

            Class type = field.get().getClass();

            if (type == Double.class) {
                field.set(block.doubleValue(field.getText()).get());
            } else if (type == Integer.class) {
                field.set(block.intValue(field.getText()).get());
            } else if (type == Boolean.class) {
                field.set(block.booleanValue(field.getText()).get());
            } else if (type == String.class) {
                field.set(block.stringValue(field.getText()).get());
            } else if (type == Range.class && block.hasBlock(field.getText())) {

                ConfigBlock  sub    = block.subBlock(field.getText());
                List<Double> values = new LinkedList<>();
                for (int i = 0; sub.hasValue(String.valueOf(i)); i++) {
                    values.add(sub.doubleValue(String.valueOf(i)).getOrDefault(0.0));
                }

                field.set(Range.manual(values.toArray(Double[]::new)));

                field.loadOtherDefaults(sub);

            }

        }

    }

    public void updateGridding() {

        GUI.runNow(() -> {

            int shift   = 0;
            int lastRow = 0;

            for (Node n : list.getChildren()) {

                int row = GridPane.getRowIndex(n);

                shift += Math.max(0, (row - lastRow) - 1);

                GridPane.setRowIndex(n, row - shift);

                lastRow = row;

            }

        });

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

        StringField f = new StringField(label, field) {

            @Override
            public void remove() {
                GUI.runNow(() -> {
                    list.getChildren().removeAll(label, field);
                    updateGridding();
                });
            }

        };

        fields.add(f);
        return f;

    }

    public Field<Range<Double>> addDoubleRange(String name, Range<Double> defaultValues) {
        return addDoubleRange(name, defaultValues, 0.0, 10.0, 11, 1.0, 2);
    }

    public Field<Range<Double>> addDoubleRange(String name, Range<Double> defaultValues, double min, double max, int count, double dStep, int dOrder) {

        Label                             label     = new Label(name);
        TableView<ObservableList<Double>> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setMinHeight(150.0);
        tableView.setPrefWidth(75.0);


        TableColumn<ObservableList<Double>, Double> column = new TableColumn(name);
        column.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().get(0)));

        tableView.getColumns().add(column);

        MenuButton addButton = new MenuButton("✚");
        Button     remButton = new Button("✕");
        Button     mUpButton = new Button("▲");
        Button     mDnButton = new Button("▼");
        Button     clrButton = new Button("Clear");

        remButton.setFont(Font.font(remButton.getFont().getFamily(), FontWeight.BOLD, remButton.getFont().getSize()));

        MenuItem addManual      = new MenuItem("Single Value...");
        MenuItem addManualList  = new MenuItem("Comma-Separated List...");
        MenuItem addLinear      = new MenuItem("Linear Range...");
        MenuItem addStep        = new MenuItem("Equal Space Range...");
        MenuItem addPoly        = new MenuItem("Polynomial Range...");
        MenuItem addGeometric   = new MenuItem("Geometric Range...");
        MenuItem addExponential = new MenuItem("Exponential Range...");

        Fields        manualResponse = new Fields("Add Single Value");
        Field<Double> manualValue    = manualResponse.addDoubleField("Value");

        Fields         linearResponse = new Fields("Add Linear Range");
        Field<Double>  linearStart    = linearResponse.addDoubleField("Start", min);
        Field<Double>  linearStop     = linearResponse.addDoubleField("Stop", max);
        Field<Integer> linearSteps    = linearResponse.addIntegerField("No. Steps", count);
        Field<Boolean> linearSym      = linearResponse.addCheckBox("Symmetrical", false);

        Fields         stepResponse = new Fields("Add Equal Space Range");
        Field<Double>  stepStart    = stepResponse.addDoubleField("Start", min);
        Field<Double>  stepStop     = stepResponse.addDoubleField("Stop", max);
        Field<Double>  stepStep     = stepResponse.addDoubleField("Step Size", dStep);
        Field<Boolean> stepSym      = stepResponse.addCheckBox("Symmetrical", false);

        Fields         polyResponse = new Fields("Add Polynomial Range");
        Field<Double>  polyStart    = polyResponse.addDoubleField("Start", min);
        Field<Double>  polyStop     = polyResponse.addDoubleField("Stop", max);
        Field<Integer> polySteps    = polyResponse.addIntegerField("No. Steps", count);
        Field<Integer> polyOrder    = polyResponse.addIntegerField("Order", dOrder);
        Field<Boolean> polySym      = polyResponse.addCheckBox("Symmetrical", false);

        Fields         geomResponse = new Fields("Add Geometric Range");
        Field<Double>  geomStart    = geomResponse.addDoubleField("Start", min);
        Field<Double>  geomStop     = geomResponse.addDoubleField("Stop", max);
        Field<Double>  geomStep     = geomResponse.addDoubleField("Factor", dStep);
        Field<Boolean> geomSym      = geomResponse.addCheckBox("Symmetrical", false);

        Fields         expResponse = new Fields("Add Geometric Range");
        Field<Double>  expStart    = expResponse.addDoubleField("Start", min);
        Field<Double>  expStop     = expResponse.addDoubleField("Stop", max);
        Field<Integer> expSteps    = expResponse.addIntegerField("No. Steps", count);
        Field<Boolean> expSym      = expResponse.addCheckBox("Symmetrical", false);

        addManual.setOnAction(e -> Util.runAsync(() -> {

            if (manualResponse.showAsConfirmation()) {
                tableView.getItems().add(FXCollections.observableArrayList(manualValue.get()));
            }

        }));

        addManualList.setOnAction(e -> Util.runAsync(() -> {

            String[] raw = GUI.inputWindow("Add", "Add Values", "Please enter your values as a comma-separated list:", "Values");

            if (raw != null) {
                tableView.getItems().addAll(
                    Arrays.stream(raw[0].split(","))
                          .map(String::trim)
                          .map(v -> FXCollections.observableArrayList(Double.parseDouble(v)))
                          .collect(Collectors.toList())
                );
            }

        }));

        addLinear.setOnAction(e -> Util.runAsync(() -> {

            if (linearResponse.showAsConfirmation()) {

                Range<Double> range = Range.linear(linearStart.get(), linearStop.get(), linearSteps.get());

                if (linearSym.get()) {
                    range = range.mirror();
                }

                tableView.getItems().addAll(
                    range.stream().map(FXCollections::observableArrayList).collect(Collectors.toList())
                );

            }

        }));

        addStep.setOnAction(e -> Util.runAsync(() -> {

            if (stepResponse.showAsConfirmation()) {

                Range<Double> range = Range.step(stepStart.get(), stepStop.get(), stepStep.get());

                if (stepSym.get()) {
                    range = range.mirror();
                }

                tableView.getItems().addAll(
                    range.stream().map(FXCollections::observableArrayList).collect(Collectors.toList())
                );

            }

        }));

        addPoly.setOnAction(e -> Util.runAsync(() -> {

            if (polyResponse.showAsConfirmation()) {

                Range<Double> range = Range.polynomial(polyStart.get(), polyStop.get(), polySteps.get(), polyOrder.get());

                if (polySym.get()) {
                    range = range.mirror();
                }

                tableView.getItems().addAll(
                    range.stream().map(FXCollections::observableArrayList).collect(Collectors.toList())
                );

            }

        }));

        addGeometric.setOnAction(e -> Util.runAsync(() -> {

            if (geomResponse.showAsConfirmation()) {

                Range<Double> range = Range.geometric(geomStart.get(), geomStop.get(), geomStep.get());

                if (geomSym.get()) {
                    range = range.mirror();
                }

                tableView.getItems().addAll(
                    range.stream().map(FXCollections::observableArrayList).collect(Collectors.toList())
                );
            }

        }));

        addExponential.setOnAction(e -> Util.runAsync(() -> {

            if (expResponse.showAsConfirmation()) {

                Range<Double> range = Range.exponential(expStart.get(), expStop.get(), expSteps.get());

                if (expSym.get()) {
                    range = range.mirror();
                }

                tableView.getItems().addAll(
                    range.stream().map(FXCollections::observableArrayList).collect(Collectors.toList())
                );

            }

        }));

        remButton.setOnAction(e -> {

            int index = tableView.getSelectionModel().getSelectedIndex();

            if (index > -1) {
                tableView.getItems().remove(index);
            }

        });

        mUpButton.setOnAction(e -> {

            int index = tableView.getSelectionModel().getSelectedIndex();

            if (index > 0) {
                ObservableList<Double> toMove = tableView.getItems().get(index);
                tableView.getItems().set(index, tableView.getItems().get(index - 1));
                tableView.getItems().set(index - 1, toMove);
                tableView.getSelectionModel().select(index - 1);
            }

        });

        mDnButton.setOnAction(e -> {

            int index = tableView.getSelectionModel().getSelectedIndex();

            if (index < tableView.getItems().size() - 1 && index > -1) {
                ObservableList<Double> toMove = tableView.getItems().get(index);
                tableView.getItems().set(index, tableView.getItems().get(index + 1));
                tableView.getItems().set(index + 1, toMove);
                tableView.getSelectionModel().select(index + 1);
            }

        });

        clrButton.setOnAction(e -> tableView.getItems().clear());

        addButton.getItems()
                 .addAll(addManual, addManualList, addLinear, addStep, addPoly, addGeometric, addExponential);

        HBox hBox = new HBox(addButton, remButton, mUpButton, mDnButton, clrButton);
        VBox vBox = new VBox(hBox, tableView);

        hBox.setSpacing(5.0);
        vBox.setSpacing(5.0);

        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(vBox, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(vBox, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);
        GridPane.setValignment(label, VPos.TOP);

        GUI.runNow(() -> list.addRow(rows++, label, vBox));

        Field<Range<Double>> f = new Field<>() {

            private InvalidationListener l = null;

            @Override
            public void set(Range<Double> value) {

                GUI.runNow(() -> {

                    tableView.getItems().clear();
                    tableView.getItems().addAll(
                        Stream.of(value.array()).map(FXCollections::observableArrayList).collect(Collectors.toList())
                    );

                });

            }

            @Override
            public Range<Double> get() {
                return Range.manual(tableView.getItems().stream().map(i -> i.get(0)).toArray(Double[]::new));
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (l != null) {
                    tableView.getItems().removeListener(l);
                }

                l = l -> SRunnable.start(onChange);
                tableView.getItems().addListener(l);

            }

            @Override
            public void editValues(String... values) {

                if (values.length != tableView.getColumns().size()) {
                    return;
                }

                GUI.runNow(() -> {
                    for (int i = 0; i < values.length; i++) {
                        tableView.getColumns().get(i).setText(values[i]);
                    }
                });

            }

            @Override
            public boolean isDisabled() {
                return tableView.isDisabled();
            }

            @Override
            public boolean isVisible() {
                return vBox.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    list.getChildren().removeAll(label, vBox);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public void setDisabled(boolean disabled) {

                GUI.runNow(() -> {
                    tableView.setDisable(disabled);
                    addButton.setDisable(disabled);
                    remButton.setDisable(disabled);
                    mUpButton.setDisable(disabled);
                    mDnButton.setDisable(disabled);
                });

            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    vBox.setVisible(visible);
                    label.setManaged(visible);
                    vBox.setManaged(visible);
                });
            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }

            public void writeOtherDefaults(ConfigBlock config) {
                manualResponse.writeToConfig(config.subBlock("Manual Default"));
                linearResponse.writeToConfig(config.subBlock("Linear Default"));
                stepResponse.writeToConfig(config.subBlock("Step Default"));
                polyResponse.writeToConfig(config.subBlock("Polynomial Default"));
                geomResponse.writeToConfig(config.subBlock("Geometric Default"));
                expResponse.writeToConfig(config.subBlock("Exponential Default"));
            }

            public void loadOtherDefaults(ConfigBlock config) {
                manualResponse.loadFromConfig(config.subBlock("Manual Default"));
                linearResponse.loadFromConfig(config.subBlock("Linear Default"));
                stepResponse.loadFromConfig(config.subBlock("Step Default"));
                polyResponse.loadFromConfig(config.subBlock("Polynomial Default"));
                geomResponse.loadFromConfig(config.subBlock("Geometric Default"));
                expResponse.loadFromConfig(config.subBlock("Exponential Default"));
            }

        };

        fields.add(f);
        f.set(defaultValues);
        return f;

    }

    public Field<Integer> addTimeField(String name, int initialValue) {

        TimeInput field = new TimeInput();
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

        TimeField f = new TimeField(label, field) {

            @Override
            public void remove() {
                GUI.runNow(() -> {
                    list.getChildren().removeAll(label, field);
                    updateGridding();
                });
            }

        };

        fields.add(f);
        return f;

    }

    public Field<Integer> addTimeField(String name) {
        return addTimeField(name, 0);
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


        Field<Boolean> f = new Field<>() {

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

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

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
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return field.getText();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    label.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> field.setText(text));
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
            String file = GUI.saveFileSelect(field.getText().isBlank() ? null : field.getText());
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


        Field<String> f = new Field<>() {

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

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

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
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, inner);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    inner.setVisible(visible);
                    inner.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
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

    public Field<Double> addDoubleDisplay(String name, double initialValue) {

        TextField field = new TextField(String.format("%e", initialValue));
        field.setMaxWidth(Integer.MAX_VALUE);
        field.setBackground(Background.EMPTY);
        field.setBorder(Border.EMPTY);
        field.setEditable(false);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        Field<Double> f = new Field<>() {

            private double value = initialValue;
            private ChangeListener<String> list = null;

            @Override
            public void set(Double value) {
                GUI.runNow(() -> field.setText(String.format("%e", value)));
                this.value = value;
            }

            @Override
            public Double get() {
                return value;
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                field.textProperty().removeListener(list);

                list = (observableValue, s, t1) -> new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                field.textProperty().addListener(list);

            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    field.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }


            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    public void clear() {

        GUI.runNow(() -> {
            list.getChildren().clear();
            fields.clear();
            rows = 0;
        });

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
            String file = GUI.directorySelect(field.getText().isBlank() ? null : field.getText());
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


        Field<String> f = new Field<>() {

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

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, inner);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    inner.setVisible(visible);
                    inner.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
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

            String file = GUI.openFileSelect(field.getText().isBlank() ? null : field.getText());

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

        Field<String> f = new Field<>() {

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

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, inner);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    inner.setVisible(visible);
                    inner.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
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

        Field<Integer> f = new Field<>() {

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

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {

                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    field.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
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
     * Adds a text box that only accepts decimal values.
     *
     * @param name         Name of the field
     * @param initialValue Initial value to display
     *
     * @return SetGettable to set or get the value as a decimal
     */
    public Field<Double> addDecimalField(String name, double initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        DecimalField field = new DecimalField();
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

        Field<Double> f = new Field<>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(Double value) {

                field.setText(value.toString());
            }

            @Override
            public Double get() {

                return field.getDecimalValue();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {

                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    field.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
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
     * Adds a text box that only accepts decimal values.
     *
     * @param name Name of the field
     *
     * @return SetGettable to set or get the value as a decimal
     */
    public Field<Double> addDecimalField(String name) {

        return addDecimalField(name, 0.0);
    }

    public Field<List<List<Double>>> addTable(String name, String... columns) {

        Label                             label     = new Label(name);
        TableView<ObservableList<Double>> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setMinHeight(250.0);
        tableView.setPrefWidth(columns.length * 75.0);
        Fields              addNew    = new Fields("Add Row");
        List<Field<Double>> addFields = new LinkedList<>();

        int i = 0;
        for (String colName : columns) {

            final int finalI = i++;

            TableColumn<ObservableList<Double>, Double> column = new TableColumn(colName);
            column.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().get(finalI)));

            tableView.getColumns().add(column);
            addFields.add(addNew.addDoubleField(colName));

        }

        Button addButton = new Button("✚");
        Button remButton = new Button("✕");
        Button edtButton = new Button("✎");
        Button mUpButton = new Button("▲");
        Button mDnButton = new Button("▼");

        addButton.setOnAction(e -> Util.runAsync(() -> {

            if (addNew.showAsConfirmation()) {

                ObservableList<Double> values = FXCollections.observableArrayList(
                    addFields.stream().map(Field::get).collect(Collectors.toList())
                );

                GUI.runNow(() -> tableView.getItems().add(values));

            }

        }));

        remButton.setOnAction(e -> {

            int index = tableView.getSelectionModel().getSelectedIndex();

            if (index > -1) {
                tableView.getItems().remove(index);
            }

        });

        edtButton.setOnAction(e -> Util.runAsync(() -> {

            int index = tableView.getSelectionModel().getSelectedIndex();

            if (index > -1) {

                List<Double> oldValues = tableView.getItems().get(index);

                for (int j = 0; j < oldValues.size(); j++) {
                    addFields.get(j).set(oldValues.get(j));
                }

                if (addNew.showAsConfirmation()) {

                    ObservableList<Double> values = FXCollections.observableArrayList(
                        addFields.stream().map(Field::get).collect(Collectors.toList())
                    );

                    GUI.runNow(() -> tableView.getItems().set(index, values));

                }

            }

        }));

        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1 && tableView.getSelectionModel().getSelectedIndex() > -1) {
                edtButton.getOnAction().handle(null);
            }
        });

        mUpButton.setOnAction(e -> {

            int index = tableView.getSelectionModel().getSelectedIndex();

            if (index > 0) {
                ObservableList<Double> toMove = tableView.getItems().get(index);
                tableView.getItems().set(index, tableView.getItems().get(index - 1));
                tableView.getItems().set(index - 1, toMove);
                tableView.getSelectionModel().select(index - 1);
            }

        });

        mDnButton.setOnAction(e -> {

            int index = tableView.getSelectionModel().getSelectedIndex();

            if (index < tableView.getItems().size() - 1 && index > -1) {
                ObservableList<Double> toMove = tableView.getItems().get(index);
                tableView.getItems().set(index, tableView.getItems().get(index + 1));
                tableView.getItems().set(index + 1, toMove);
                tableView.getSelectionModel().select(index + 1);
            }

        });

        HBox hBox = new HBox(addButton, remButton, edtButton, mUpButton, mDnButton);
        VBox vBox = new VBox(hBox, tableView);

        hBox.setSpacing(5.0);
        vBox.setSpacing(5.0);

        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(vBox, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(vBox, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);
        GridPane.setValignment(label, VPos.TOP);

        GUI.runNow(() -> list.addRow(rows++, label, vBox));

        Field<List<List<Double>>> f = new Field<>() {

            private InvalidationListener l = null;

            @Override
            public void set(List<List<Double>> value) {

                GUI.runNow(() -> {

                    tableView.getItems().clear();
                    tableView.getItems().addAll(
                        value.stream().map(FXCollections::observableArrayList).collect(Collectors.toUnmodifiableList())
                    );

                });

            }

            @Override
            public List<List<Double>> get() {
                return tableView.getItems().stream().map(i -> (List<Double>) i).collect(Collectors.toUnmodifiableList());
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (l != null) {
                    tableView.getItems().removeListener(l);
                }

                l = l -> SRunnable.start(onChange);
                tableView.getItems().addListener(l);

            }

            @Override
            public void editValues(String... values) {

                if (values.length != tableView.getColumns().size()) {
                    return;
                }

                GUI.runNow(() -> {
                    for (int i = 0; i < values.length; i++) {
                        tableView.getColumns().get(i).setText(values[i]);
                    }
                });

            }

            @Override
            public boolean isDisabled() {
                return tableView.isDisabled();
            }

            @Override
            public boolean isVisible() {
                return vBox.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    list.getChildren().removeAll(label, vBox);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public void setDisabled(boolean disabled) {

                GUI.runNow(() -> {
                    tableView.setDisable(disabled);
                    addButton.setDisable(disabled);
                    remButton.setDisable(disabled);
                    mUpButton.setDisable(disabled);
                    mDnButton.setDisable(disabled);
                });

            }


            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    vBox.setVisible(visible);
                    label.setManaged(visible);
                    vBox.setManaged(visible);
                });
            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }

        };

        fields.add(f);
        return f;

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

        Field<Double> f = new DoubleField(label, field) {
            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

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

        Field<Integer> f = new Field<>() {

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

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.getSelectionModel().selectedIndexProperty().addListener(list);
            }

            @Override
            public synchronized void editValues(String... values) {

                GUI.runNow(() -> {

                    int selected = field.getSelectionModel().getSelectedIndex();

                    int min = Math.min(values.length - 1, Math.max(0, selected));
                    if (list != null) {
                        field.getSelectionModel().selectedIndexProperty().removeListener(list);
                        field.setItems(FXCollections.observableArrayList(values));
                        field.getSelectionModel().select(min);
                        field.getSelectionModel().selectedIndexProperty().addListener(list);
                    } else {
                        field.setItems(FXCollections.observableArrayList(values));
                        field.getSelectionModel().select(min);
                    }

                });
            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {

                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    field.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
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


    public jisa.gui.Separator addSeparator() {

        Separator separator = new Separator();
        GUI.runNow(() -> list.addRow(rows++, separator));
        GridPane.setColumnSpan(separator, 2);
        VBox.setVgrow(separator, Priority.ALWAYS);

        return new jisa.gui.Separator() {

            @Override
            public void remove() {
                GUI.runNow(() -> {
                    list.getChildren().remove(separator);
                    updateGridding();
                });
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

    public void setFieldsDisabled(boolean flag) {

        for (Field<?> f : fields) {
            f.setDisabled(flag);
        }

    }

    @Override
    public Iterator<Field<?>> iterator() {
        return fields.iterator();
    }

}
