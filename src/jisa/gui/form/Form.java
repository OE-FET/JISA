package jisa.gui.form;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import jisa.Util;
import jisa.control.ConfigBlock;
import jisa.control.SRunnable;
import jisa.gui.Element;
import jisa.gui.GUI;
import jisa.gui.JFXElement;
import jisa.gui.controls.*;
import jisa.maths.Range;
import jisa.maths.functions.GFunction;
import jisa.results.Column;
import jisa.results.ResultList;
import jisa.results.ResultTable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class Form extends JFXElement implements Element, Iterable<Field<?>> {

    public        BorderPane      pane;
    public        GridPane        list;
    private final List<Field<?>>  fields   = new LinkedList<>();
    public final  ExecutorService executor = Executors.newSingleThreadExecutor();
    private       int             rows     = 0;

    private static <A, B> Property<B> mappedBind(Property<A> property, GFunction<B, A> ab, GFunction<A, B> ba) {

        Property<B> mapped = new SimpleObjectProperty<>(ab.value(property.getValue()));

        property.addListener((observable, oldValue, newValue) -> {

            B value = ab.value(newValue);

            if (!mapped.getValue().equals(value)) {
                mapped.setValue(value);
            }

        });

        mapped.addListener((observable, oldValue, newValue) -> {

            A value = ba.value(newValue);

            if (!property.getValue().equals(value)) {
                property.setValue(value);
            }

        });

        return mapped;

    }

    /**
     * Creates a input fields group for user-input.
     *
     * @param title Title of the window/grid-element.
     */
    public Form(String title) {

        super(title, GUI.getFXML("InputWindow"));
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

            } else if (ResultTable.class.isAssignableFrom(type)) {

                block.stringValue(field.getText()).set(((TableField) field).getValue().getCSV());

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

            } else if (ResultTable.class.isAssignableFrom(type) && block.hasBlock(field.getText())) {
                try {
                    field.set(ResultList.fromCSVString(block.stringValue(field.getText()).get()));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
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


        TextField     control = new TextField(initialValue);
        Label         label   = new Label(name);
        Field<String> field   = new BasicField<>(this, label, control, control.textProperty());

        control.setMaxWidth(Integer.MAX_VALUE);
        label.setMinWidth(Region.USE_PREF_SIZE);

        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(control, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, control));

        fields.add(field);
        return field;

    }

    public Field<Range<Double>> addDoubleRange(String name, Range<Double> defaultValues) {
        return addDoubleRange(name, defaultValues, 0.0, 10.0, 11, 1.0, 2);
    }

    public Field<Range<Double>> addDoubleRange(String name, Range<Double> defaultValues, double min, double max, int count, double dStep, int dOrder) {

        Label      label = new Label(name);
        RangeInput table = new RangeInput(name, min, max, count, dStep, dOrder);

        GUI.runNow(() -> list.addRow(rows++, label, table));

        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(table, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(table, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);
        GridPane.setValignment(label, VPos.TOP);

        List<Field.Listener<Range<Double>>> listeners = new LinkedList<>();

        table.tableView.getItems().addListener((InvalidationListener) i -> {

            Range<Double> value = table.getRange();

            synchronized (listeners) {

                for (Field.Listener<Range<Double>> l : listeners) {

                    executor.submit(() -> l.valueChanged(value));

                }

            }

        });

        Field<Range<Double>> f = new Field<>() {

            @Override
            public void set(Range<Double> value) {
                GUI.runNow(() -> table.setRange(value));
            }

            @Override
            public Range<Double> get() {
                return table.getRange();
            }

            @Override
            public Listener<Range<Double>> addChangeListener(Listener<Range<Double>> onChange) {

                synchronized (listeners) {
                    listeners.add(onChange);
                }

                return onChange;

            }

            @Override
            public void removeChangeListener(Listener<Range<Double>> onChange) {

                synchronized (listeners) {
                    listeners.remove(onChange);
                }

            }

            @Override
            public boolean isDisabled() {
                return table.disabled();
            }

            @Override
            public boolean isVisible() {
                return table.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    list.getChildren().removeAll(label, table);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public void setDisabled(boolean disabled) {
                GUI.runNow(() -> table.disabled(disabled));
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    table.setVisible(visible);
                    label.setManaged(visible);
                    table.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }

            public void writeOtherDefaults(ConfigBlock config) {
                table.manualResponse.writeToConfig(config.subBlock("Manual Default"));
                table.linearResponse.writeToConfig(config.subBlock("Linear Default"));
                table.stepResponse.writeToConfig(config.subBlock("Step Default"));
                table.polyResponse.writeToConfig(config.subBlock("Polynomial Default"));
                table.geomResponse.writeToConfig(config.subBlock("Geometric Default"));
                table.expResponse.writeToConfig(config.subBlock("Exponential Default"));
            }

            public void loadOtherDefaults(ConfigBlock config) {
                table.manualResponse.loadFromConfig(config.subBlock("Manual Default"));
                table.linearResponse.loadFromConfig(config.subBlock("Linear Default"));
                table.stepResponse.loadFromConfig(config.subBlock("Step Default"));
                table.polyResponse.loadFromConfig(config.subBlock("Polynomial Default"));
                table.geomResponse.loadFromConfig(config.subBlock("Geometric Default"));
                table.expResponse.loadFromConfig(config.subBlock("Exponential Default"));
            }

        };

        fields.add(f);
        f.set(defaultValues);
        return f;

    }

    public Field<Integer> addTimeField(String name, int initialValue) {

        TimeInput control = new TimeInput();
        Label     label   = new Label(name);

        control.setValue(initialValue);
        control.setMaxWidth(Integer.MAX_VALUE);
        label.setMinWidth(Region.USE_PREF_SIZE);

        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(control, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, control));

        Property<Integer> time = new SimpleObjectProperty<>(initialValue);

        time.addListener((observable, oldValue, newValue) -> {

            if (newValue != control.getValue()) {
                control.setValue(newValue);
            }

        });

        control.setOnChange(value -> {

            if (!Objects.equals(value, time.getValue())) {
                time.setValue(value);
            }

        });

        Field<Integer> field = new BasicField<>(this, label, control, time);

        fields.add(field);
        return field;

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

        CheckBox control = new CheckBox();
        Label    label   = new Label("");

        control.setText(name);
        control.setSelected(initialValue);
        control.setMaxWidth(Integer.MAX_VALUE);
        GridPane.setVgrow(control, Priority.NEVER);
        GridPane.setHgrow(control, Priority.ALWAYS);

        GUI.runNow(() -> list.addRow(rows++, label, control));

        Field<Boolean> field = new BasicField<>(this, control, control, control.selectedProperty(), label);

        fields.add(field);
        return field;

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

        TextField control = new TextField(initialValue);
        Label     label   = new Label(name);
        Button    button  = new Button("Browse...");

        button.setMinWidth(Region.USE_PREF_SIZE);
        label.setMinWidth(Region.USE_PREF_SIZE);

        button.setOnAction(actionEvent -> {
            String file = GUI.saveFileSelect(control.getText().isBlank() ? null : control.getText());
            if (file != null) {
                control.setText(file);
            }
        });

        control.setMaxWidth(Integer.MAX_VALUE);

        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(control, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        HBox inner = new HBox(control, button);
        inner.setSpacing(15);

        HBox.setHgrow(control, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.NEVER);

        GUI.runNow(() -> list.addRow(rows++, label, inner));


        Field<String> field = new BasicField<>(this, label, inner, control.textProperty());

        fields.add(field);

        return field;

    }

    public Field<Double> addDoubleDisplay(String name, double initialValue) {

        TextField control = new TextField(String.format("%e", initialValue));
        control.setMaxWidth(Integer.MAX_VALUE);
        control.setBackground(Background.EMPTY);
        control.setBorder(Border.EMPTY);
        control.setEditable(false);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(control, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, control));

        Field<Double> field = new BasicField<>(
            this, label, control,
            mappedBind(control.textProperty(), Double::parseDouble, v -> String.format("%e", v))
        );

        fields.add(field);
        return field;

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

        TextField control = new TextField(initialValue);
        Label     label   = new Label(name);
        Button    button  = new Button("Browse...");

        label.setMinWidth(Region.USE_PREF_SIZE);
        button.setMinWidth(Region.USE_PREF_SIZE);

        button.setOnAction(actionEvent -> {

            String file = GUI.directorySelect(control.getText().isBlank() ? null : control.getText());

            if (file != null) {
                control.setText(file);
            }

        });

        control.setMaxWidth(Integer.MAX_VALUE);

        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(control, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        HBox inner = new HBox(control, button);
        inner.setSpacing(15);

        HBox.setHgrow(control, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.NEVER);

        GUI.runNow(() -> list.addRow(rows++, label, inner));

        Field<String> field = new BasicField<>(this, label, inner, control.textProperty());

        fields.add(field);
        return field;

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

        TextField control = new TextField(initialValue);
        Label     label   = new Label(name);
        Button    button  = new Button("Browse...");

        label.setMinWidth(Region.USE_PREF_SIZE);
        button.setMinWidth(Region.USE_PREF_SIZE);

        button.setOnAction(actionEvent -> {

            String file = GUI.openFileSelect(control.getText().isBlank() ? null : control.getText());

            if (file != null) {
                control.setText(file);
            }

        });

        control.setMaxWidth(Integer.MAX_VALUE);

        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(control, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        HBox inner = new HBox(control, button);
        inner.setSpacing(15);

        HBox.setHgrow(control, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.NEVER);

        GUI.runNow(() -> list.addRow(rows++, label, inner));

        Field<String> field = new BasicField<>(this, label, inner, control.textProperty());

        fields.add(field);
        return field;

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

        Field<Integer> f = new BasicField<>(
            this, label, field,
            mappedBind(field.textProperty(), Integer::parseInt, Object::toString)
        );

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

        DecimalField field = new DecimalField();
        Label        label = new Label(name);

        field.setText(String.valueOf(initialValue));
        field.setMaxWidth(Integer.MAX_VALUE);
        label.setMinWidth(Region.USE_PREF_SIZE);

        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        Field<Double> f = new BasicField<>(
            this, label, field,
            mappedBind(field.textProperty(), Double::parseDouble, d -> String.format("%f", d))
        );

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

    public TableField addTable(String name, Column... columns) {

        Label      label   = new Label(name);
        TableInput control = new TableInput(columns);

        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(control, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);
        GridPane.setValignment(label, VPos.TOP);

        GUI.runNow(() -> list.addRow(rows++, label, control));

        TableField field = new TableInputField(this, label, control);

        fields.add(field);
        return field;

    }

    public TableField addTable(String name, String... columns) {
        return addTable(name, Stream.of(columns).map(Column::ofDoubles).toArray(Column[]::new));
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

        HBox        box     = new HBox();
        DoubleInput control = new DoubleInput();
        Label       label   = new Label(name);

        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);
        control.setValue(initialValue);
        control.setMaxWidth(Integer.MAX_VALUE);
        label.setMinWidth(Region.USE_PREF_SIZE);

        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(control, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, control));

        Field<Double> field = new BasicField<>(this, label, control, control.valueProperty());

        fields.add(field);
        return field;

    }

    public jisa.gui.Button addButton(String name, SRunnable onClick) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button(name);

        button.setOnAction(e -> SRunnable.start(onClick));

        GridPane.setVgrow(button, Priority.NEVER);
        GridPane.setHgrow(button, Priority.NEVER);
        GridPane.setHalignment(button, HPos.RIGHT);

        GUI.runNow(() -> list.add(button, 1, rows++));

        return new jisa.gui.Button() {

            @Override
            public boolean isDisabled() {
                return button.isDisabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                GUI.runNow(() -> button.setDisable(disabled));
            }

            @Override
            public boolean isVisible() {
                return button.isVisible();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    button.setVisible(visible);
                    button.setManaged(visible);
                });

            }

            @Override
            public String getText() {
                return button.getText();
            }

            @Override
            public void setText(String text) {
                GUI.runNow(() -> button.setText(text));
            }

            @Override
            public void setOnClick(SRunnable onClick) {
                button.setOnAction(e -> SRunnable.start(onClick));
            }

            @Override
            public void remove() {
                GUI.runNow(() -> {
                    list.getChildren().remove(button);
                    updateGridding();
                });

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

        ChoiceBox<String> control = new ChoiceBox<>();
        Label             label   = new Label(name);

        control.setMaxWidth(Integer.MAX_VALUE);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(control, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, control));

        control.getItems().addAll(options);
        control.getSelectionModel().select(initialValue);

        Property<Integer> index = new SimpleObjectProperty<>(control.getSelectionModel().getSelectedIndex());


        index.addListener((observable, oldValue, newValue) -> {

            int current = control.getSelectionModel().getSelectedIndex();

            if (newValue != current) {
                control.getSelectionModel().select(newValue);
            }

        });

        control.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {

            int current = index.getValue();

            if (newValue.intValue() != current) {
                index.setValue(newValue.intValue());
            }

        });

        Field<Integer> field = new BasicField<>(this, label, control, index);

        fields.add(field);
        return field;

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
