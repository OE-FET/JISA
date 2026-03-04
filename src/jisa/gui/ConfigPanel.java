package jisa.gui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jisa.devices.Instrument;
import jisa.gui.controls.DoubleInput;
import jisa.gui.controls.IntegerField;
import jisa.gui.controls.TableInput;
import jisa.results.ResultTable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GUI element for configuring instrument parameters.
 * @param <I>
 */
public class ConfigPanel<I extends Instrument> extends JFXElement {

    private final I                                   instrument;
    private final GridPane                            grid;
    private final Map<Instrument.Parameter, NodeItem> parameters = new LinkedHashMap<>();

    private int row = 0;

    public ConfigPanel(String title, I instrument) {

        super(title);

        this.instrument = instrument;
        this.grid       = new GridPane();

        grid.setHgap(0);
        grid.setVgap(15);

        BorderPane.setMargin(getNode().getCenter(), new Insets(15.0));

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(true);
        scrollPane.setBorder(Border.EMPTY);
        scrollPane.setBackground(Background.EMPTY);
        scrollPane.setStyle("-fx-background: rgba(255,255,255,0); -fx-background-color: rgba(255,255,255,0);");
        grid.setPadding(new Insets(GUI.SPACING));
        setCentreNode(scrollPane);

        BorderPane.setMargin(scrollPane, Insets.EMPTY);

        generateForm();

        addToolbarButton("Apply", () -> {

            parameters.forEach((p, i) -> {

                try {
                    p.set(i.getValue());
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            });

            parameters.forEach((p, i) -> {

                try {
                    i.setValue(p.getCurrentValue());
                    i.updateLastValue();
                    i.setValue(p.getCurrentValue());
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            });

        });

    }

    public ConfigPanel(I instrument) {
        this(instrument.getName(), instrument);
    }

    protected void generateForm() {

        grid.getChildren().clear();
        parameters.clear();
        row = 0;

        for (Instrument.Parameter parameter : instrument.getAllParameters()) {

            Label    label = new Label(parameter.getName());
            NodeItem item  = createNode(parameter.getDefaultValue(), parameter.getChoices().toArray());
            Button   set   = new Button("Set");

            set.setMinWidth(Button.USE_PREF_SIZE);

            set.setOnAction(event -> {

                try {
                    parameter.set(item.getValue());
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                parameters.forEach((p,i) -> {

                    try {
                        i.setValue(p.getCurrentValue());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    i.updateLastValue();
                    label.setTextFill(item.getValue().equals(item.getLastValue()) ? Color.BLACK : Color.BROWN);

                });

            });

            if (item == null) {
                continue;
            }

            Node node = item.getNode();

            item.addListener(i -> label.setTextFill(item.getValue().equals(item.getLastValue()) ? Color.BLACK : Color.BROWN));

            label.setMinWidth(Region.USE_PREF_SIZE);

            GridPane.setVgrow(label, Priority.NEVER);
            GridPane.setVgrow(node, Priority.NEVER);
            GridPane.setHgrow(label, Priority.NEVER);
            GridPane.setHgrow(node, Priority.ALWAYS);
            GridPane.setHalignment(label, HPos.RIGHT);
            GridPane.setValignment(label, parameter.getDefaultValue() instanceof ResultTable || node instanceof VBox ? VPos.TOP : VPos.CENTER);
            GridPane.setMargin(label, new Insets(5, 0, 0, 0));
            GridPane.setMargin(label, new Insets(0, 15, 0, 0));
            GridPane.setMargin(node, new Insets(0, 5, 0, 0));

            addRow(label, node, set);

            parameters.put(parameter, item);

        }

    }

    protected void addRow(Node... children) {
        grid.addRow(row++, children);
    }

    public static <Q> NodeItem<Q> createNode(Q defaultValue, Q... choices) {

        if (defaultValue instanceof Instrument.AutoQuantity) {

            CheckBox checkBox = new CheckBox(((Instrument.AutoQuantity<?>) defaultValue).getAutoText());
            NodeItem quantity = createNode(((Instrument.AutoQuantity<?>) defaultValue).getValue(), choices);

            if (quantity == null) {
                return null;
            }

            checkBox.selectedProperty().addListener(i -> quantity.setDisabled(checkBox.isSelected()));
            checkBox.setSelected(((Instrument.AutoQuantity<?>) defaultValue).isAuto());
            checkBox.setAlignment(Pos.CENTER_LEFT);
            checkBox.setMinWidth(Region.USE_PREF_SIZE);

            HBox.setHgrow(checkBox, Priority.NEVER);
            HBox.setHgrow(quantity.getNode(), Priority.ALWAYS);

            return (NodeItem<Q>) new NodeItem<Instrument.AutoQuantity>(
                quantity.getNode() instanceof TableInput ? new VBox(15.0, checkBox, quantity.getNode()) : new HBox(5, quantity.getNode(), checkBox)
            ) {

                @Override
                public Instrument.AutoQuantity getValue() {
                    return new Instrument.AutoQuantity<>(checkBox.isSelected(), quantity.getValue());
                }

                @Override
                public void setValue(Instrument.AutoQuantity value) {
                    checkBox.setSelected(value.isAuto());
                    quantity.setValue(value.getValue());
                }

                @Override
                public void setDisabled(boolean disabled) {
                    checkBox.setDisable(disabled);
                    quantity.setDisabled(disabled || checkBox.isSelected());
                }

                @Override
                public void addListener(InvalidationListener listener) {
                    checkBox.selectedProperty().addListener(listener);
                    quantity.addListener(listener);
                }

            };

        } else if (defaultValue instanceof Instrument.OptionalQuantity) {

            CheckBox checkBox = new CheckBox("Enabled");
            NodeItem quantity = createNode(((Instrument.OptionalQuantity<?>) defaultValue).getValue(), choices);

            if (quantity == null) {
                return null;
            }

            checkBox.selectedProperty().addListener(i -> quantity.setDisabled(!checkBox.isSelected()));
            checkBox.setSelected(((Instrument.OptionalQuantity<?>) defaultValue).isUsed());
            checkBox.setAlignment(Pos.CENTER_LEFT);
            checkBox.setMinWidth(Region.USE_PREF_SIZE);

            quantity.setDisabled(!checkBox.isSelected());

            HBox.setHgrow(checkBox, Priority.NEVER);
            HBox.setHgrow(quantity.getNode(), Priority.ALWAYS);

            return (NodeItem<Q>) new NodeItem<Instrument.OptionalQuantity>(
                quantity.getNode() instanceof TableInput ? new VBox(15.0, checkBox, quantity.getNode()) : new HBox(5, quantity.getNode(), checkBox)
            ) {

                @Override
                public Instrument.OptionalQuantity getValue() {
                    return new Instrument.OptionalQuantity<>(checkBox.isSelected(), quantity.getValue());
                }

                @Override
                public void setValue(Instrument.OptionalQuantity value) {
                    checkBox.setSelected(value.isUsed());
                    quantity.setValue(value.getValue());
                }

                @Override
                public void setDisabled(boolean disabled) {
                    checkBox.setDisable(disabled);
                    quantity.setDisabled(disabled || !checkBox.isSelected());
                }

                @Override
                public void addListener(InvalidationListener listener) {
                    checkBox.selectedProperty().addListener(listener);
                    quantity.addListener(listener);
                }

            };

        } else if (choices.length > 0) {

            ChoiceBox<Q> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(choices));
            choiceBox.setValue(defaultValue);

            return new BasicNodeItem<>(choiceBox, choiceBox.valueProperty());

        } else if (defaultValue instanceof Double) {

            DoubleInput doubleField = new DoubleInput();
            doubleField.setValue((Double) defaultValue);

            return (NodeItem<Q>) new BasicNodeItem<>(doubleField, doubleField.valueProperty());

        } else if (defaultValue instanceof Integer) {

            IntegerField integerField = new IntegerField();
            integerField.setText(String.format("%d", (int) defaultValue));

            return (NodeItem<Q>) new NodeItem<Integer>(integerField) {

                @Override
                public Integer getValue() {
                    return integerField.getIntValue();
                }

                @Override
                public void setValue(Integer value) {
                    integerField.setText(String.format("%d", value));
                }

                @Override
                public void setDisabled(boolean disabled) {
                    integerField.setDisable(disabled);
                }

                @Override
                public void addListener(InvalidationListener listener) {
                    integerField.textProperty().addListener(listener);
                }

            };


        } else if (defaultValue instanceof Boolean) {

            CheckBox checkBox = new CheckBox();
            checkBox.setSelected((Boolean) defaultValue);

            return (NodeItem<Q>) new BasicNodeItem<>(checkBox, checkBox.selectedProperty());

        } else if (defaultValue instanceof ResultTable) {

            TableInput tableInput = new TableInput((ResultTable) defaultValue);

            return (NodeItem<Q>) new NodeItem<ResultTable>(tableInput) {

                @Override
                public ResultTable getValue() {
                    return tableInput.getContents();
                }

                @Override
                public void setValue(ResultTable value) {
                    tableInput.setContents(value);
                }

                @Override
                public void setDisabled(boolean disabled) {
                    tableInput.setDisable(disabled);
                }

                @Override
                public void addListener(InvalidationListener listener) {
                    tableInput.getTableView().getItems().addListener(listener);
                }

            };

        } else if (defaultValue instanceof String) {

            TextField textField = new TextField();
            textField.setText((String) defaultValue);

            return (NodeItem<Q>) new BasicNodeItem<>(textField, textField.textProperty());

        } else {
            return null;
        }

    }

    public I getInstrument() {
        return instrument;
    }

    public static abstract class NodeItem<Q> {

        private final Node item;
        private       Q    lastValue;

        protected NodeItem(Node item) {
            this.item      = item;
            this.lastValue = getValue();
        }

        protected NodeItem(Node item, Q lastValue) {
            this.item      = item;
            this.lastValue = lastValue;
        }

        public Node getNode() {
            return item;
        }

        public Q getLastValue() {
            return lastValue;
        }

        public void updateLastValue() {
            lastValue = getValue();
        }

        public abstract Q getValue();

        public abstract void setValue(Q value);

        public abstract void setDisabled(boolean disabled);

        public abstract void addListener(InvalidationListener listener);

    }

    public static class BasicNodeItem<Q> extends NodeItem<Q> {

        private final Property<Q> property;

        protected BasicNodeItem(Node item, Property<Q> property) {
            super(item, property.getValue());
            this.property = property;
        }

        @Override
        public Q getValue() {
            return property.getValue();
        }

        @Override
        public void setValue(Q value) {
            property.setValue(value);
        }

        @Override
        public void setDisabled(boolean disabled) {
            getNode().setDisable(disabled);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            property.addListener(listener);
        }

    }

}
