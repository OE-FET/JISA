package jisa.gui;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import jisa.Util;
import jisa.control.ConfigBlock;
import jisa.control.SRunnable;
import jisa.maths.Range;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Fields extends JFXElement implements Element, Iterable<Field<?>> {

    public  BorderPane                          pane;
    public  GridPane                            list;
    private Map<Field<?>, ConfigBlock.Value<?>> links  = new HashMap<>();
    private List<Field<?>>                      fields = new LinkedList<>();
    private ConfigBlock                         config = null;
    private String                              tag    = null;
    private int                                 rows   = 0;

    /**
     * Creates a input fields group for user-input.
     *
     * @param title Title of the window/grid-element.
     */
    public Fields(String title) {

        super(title, Fields.class.getResource("fxml/InputWindow.fxml"));

        Util.addShutdownHook(() -> {
            if (config != null) {
                links.forEach((field, value) -> value.set(field.get()));
                config.save();
            }
        });

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
            } else if (type == Range.DoubleRange.class) {
                Range.DoubleRange range = (Range.DoubleRange) field.getValue();
                ConfigBlock       sub   = block.subBlock(field.getText());
                sub.stringValue("Type").set(range.getType().toString());
                sub.intValue("Order").set(range.getOrder());
                sub.doubleValue("Start").set(range.get(0));
                sub.doubleValue("Stop").set(range.get(range.size() - 1));
                sub.intValue("Steps").set(range.size());
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
            } else if (type == Range.DoubleRange.class && block.hasBlock(field.getText())) {

                ConfigBlock sub = block.subBlock(field.getText());

                if (sub.hasValue("Type") && sub.hasValue("Order") && sub.hasValue("Start") && sub.hasValue("Stop") && sub.hasValue("Steps")) {
                    field.set(new Range.DoubleRange(
                        Range.linear(sub.doubleValue("Start").get(), sub.doubleValue("Stop").get(), sub.intValue("Steps").get()),
                        Range.Type.valueOf(sub.stringValue("Type").get()),
                        sub.intValue("Order").get()
                    ));
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
                    Fields.this.list.getChildren().removeAll(label, field);
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

    public Field<Range.DoubleRange> addDoubleRange(String name, double defaultMin, double defaultMax, int defaultSteps) {

        AtomicReference<SRunnable> onChange = new AtomicReference<>(() -> {});

        Field<Double>  start = addDoubleField("Start " + name, defaultMin);
        Field<Double>  stop  = addDoubleField("Stop " + name, defaultMax);
        Field<Integer> steps = addIntegerField("No. Steps", defaultSteps);
        Field<Integer> type  = addChoice("Scaling", "Linear", "Exponential", "Polynomial");
        Field<Integer> order = addIntegerField("Order", 2);

        order.setVisible(false);
        type.setOnChange(() -> {
            order.setVisible(type.get() == 2 && type.isVisible());
            onChange.get().run();
        });

        Field<Range.DoubleRange> f = new Field<>() {

            @Override
            public void set(Range.DoubleRange value) {

                start.set(value.get(0));
                stop.set(value.get(value.size() - 1));
                steps.set(value.size());

                switch (value.getType()) {

                    case LINEAR:
                        type.set(0);
                        break;

                    case EXPONENTIAL:
                        type.set(1);
                        break;

                    case POLYNOMIAL:
                        type.set(2);

                }

                order.set(value.getOrder());

            }

            @Override
            public Range.DoubleRange get() {

                switch (type.get()) {

                    case 0:
                        return new Range.DoubleRange(Range.linear(start.get(), stop.get(), steps.get()), Range.Type.LINEAR, 0);

                    case 1:
                        return new Range.DoubleRange(Range.exponential(start.get(), stop.get(), steps.get()), Range.Type.EXPONENTIAL, 0);

                    case 2:
                        return new Range.DoubleRange(Range.polynomial(start.get(), stop.get(), steps.get(), order.get()), Range.Type.POLYNOMIAL, order.get());

                    default:
                        return null;

                }

            }

            @Override
            public void setOnChange(SRunnable change) {
                onChange.set(change);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {
                return type.isDisabled();
            }

            @Override
            public boolean isVisible() {
                return type.isVisible();
            }

            @Override
            public void remove() {
                type.remove();
                order.remove();
                start.remove();
                stop.remove();
                steps.remove();
                fields.remove(this);
            }

            @Override
            public String getText() {
                return start.getText().substring(6);
            }

            @Override
            public void setDisabled(boolean disabled) {
                type.setDisabled(disabled);
                order.setDisabled(disabled);
                start.setDisabled(disabled);
                stop.setDisabled(disabled);
                steps.setDisabled(disabled);
            }


            @Override
            public void setVisible(boolean visible) {
                type.setVisible(visible);
                order.setVisible(type.get() == 2 && visible);
                start.setVisible(visible);
                stop.setVisible(visible);
                steps.setVisible(visible);

            }


            @Override
            public void setText(String text) {
                start.setText("Start " + text);
                stop.setText("Stop " + text);
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
            links.clear();
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

        Field<Double> f = new Field<>() {

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

                field.setOnChange((v) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start());
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

                return field.disabled();
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
