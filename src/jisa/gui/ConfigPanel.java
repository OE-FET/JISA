package jisa.gui;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import jisa.devices.Instrument;
import jisa.gui.controls.DoubleInput;
import jisa.gui.controls.IntegerField;
import jisa.gui.controls.TableInput;
import jisa.results.ResultTable;

import java.util.LinkedHashMap;
import java.util.Map;

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

        setCentreNode(grid);

        generateForm();

    }

    protected void generateForm() {

        grid.getChildren().clear();
        parameters.clear();
        row = 0;

        for (Instrument.Parameter parameter : instrument.getAllParameters()) {

            Label    label = new Label(parameter.getName());
            NodeItem item  = createNode(parameter.getDefaultValue(), parameter.getChoices().toArray());

            if (item == null) {
                continue;
            }

            Node node = item.getNode();

            Button update = new Button("Apply");
            update.visibleProperty().addListener(i -> update.setManaged(update.isVisible()));
            update.setMinWidth(Region.USE_PREF_SIZE);
            update.setMaxWidth(Region.USE_PREF_SIZE);
            update.setDisable(true);

            update.setOnAction(event -> {

                update.setDisable(true);
                item.setDisabled(true);

                try {
                    parameter.set(item.getValue());
                } catch (Exception e) {
                    GUI.showException(e);
                }

                parameters.forEach((p, i) -> {

                    try {
                        i.setValue(p.getCurrentValue());
                    } catch (Throwable ignored) { }

                    i.updateLastValue();

                });

                update.setDisable(true);
                item.setDisabled(false);

            });

            item.addListener(i -> update.setDisable(item.getValue().equals(item.getLastValue())));

            label.setMinWidth(Region.USE_PREF_SIZE);

            GridPane.setVgrow(label, Priority.NEVER);
            GridPane.setVgrow(node, Priority.NEVER);
            GridPane.setVgrow(update, Priority.NEVER);
            GridPane.setHgrow(label, Priority.NEVER);
            GridPane.setHgrow(node, Priority.ALWAYS);
            GridPane.setHgrow(update, Priority.NEVER);
            GridPane.setHalignment(label, HPos.RIGHT);
            GridPane.setValignment(label, parameter.getDefaultValue() instanceof ResultTable || node instanceof VBox ? VPos.TOP : VPos.CENTER);
            GridPane.setValignment(update, VPos.TOP);
            GridPane.setMargin(label, new Insets(5, 0, 0, 0));
            GridPane.setMargin(label, new Insets(0, 15, 0, 0));
            GridPane.setMargin(node, new Insets(0, 5, 0, 0));

            addRow(label, node, update);

            parameters.put(parameter, item);

        }

    }

    protected void addRow(Node... children) {
        grid.addRow(row++, children);
    }

    public static <Q> NodeItem<Q> createNode(Q defaultValue, Q... choices) {

        if (defaultValue instanceof Instrument.AutoQuantity) {

            CheckBox checkBox = new CheckBox("Auto");
            NodeItem quantity = createNode(((Instrument.AutoQuantity<?>) defaultValue).getValue(), choices);

            if (quantity == null) {
                return null;
            }

            checkBox.selectedProperty().addListener(i -> quantity.setDisabled(checkBox.isSelected()));
            checkBox.setSelected(((Instrument.AutoQuantity<?>) defaultValue).isAuto());
            checkBox.setAlignment(Pos.CENTER_LEFT);

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

            return new NodeItem<>(choiceBox) {

                @Override
                public Q getValue() {
                    return choiceBox.getValue();
                }

                @Override
                public void setValue(Q value) {
                    choiceBox.setValue(value);
                }

                @Override
                public void setDisabled(boolean disabled) {
                    choiceBox.setDisable(disabled);
                }

                @Override
                public void addListener(InvalidationListener listener) {
                    choiceBox.valueProperty().addListener(listener);
                }

            };

        } else if (defaultValue instanceof Double) {

            DoubleInput doubleField = new DoubleInput();
            doubleField.setValue((Double) defaultValue);

            return (NodeItem<Q>) new NodeItem<Double>(doubleField) {

                @Override
                public Double getValue() {
                    return doubleField.getValue();
                }

                @Override
                public void setValue(Double value) {
                    doubleField.setValue(value);
                }

                @Override
                public void setDisabled(boolean disabled) {
                    doubleField.setDisable(disabled);
                }

                @Override
                public void addListener(InvalidationListener listener) {
                    doubleField.valueProperty().addListener(listener);
                }

            };

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

            return (NodeItem<Q>) new NodeItem<Boolean>(checkBox) {

                @Override
                public Boolean getValue() {
                    return checkBox.isSelected();
                }

                @Override
                public void setValue(Boolean value) {
                    checkBox.setSelected(value);
                }

                @Override
                public void setDisabled(boolean disabled) {
                    checkBox.setDisable(disabled);
                }

                @Override
                public void addListener(InvalidationListener listener) {
                    checkBox.selectedProperty().addListener(listener);
                }

            };

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

            return (NodeItem<Q>) new NodeItem<String>(textField) {

                @Override
                public String getValue() {
                    return textField.getText();
                }

                @Override
                public void setValue(String value) {
                    textField.setText(value);
                }

                @Override
                public void setDisabled(boolean disabled) {
                    textField.setDisable(disabled);
                }

                @Override
                public void addListener(InvalidationListener listener) {
                    textField.textProperty().addListener(listener);
                }

            };

        } else {
            return null;
        }

    }

    protected static abstract class NodeItem<Q> {

        private final Node item;
        private       Q    lastValue = getValue();

        protected NodeItem(Node item) {
            this.item = item;
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

}
