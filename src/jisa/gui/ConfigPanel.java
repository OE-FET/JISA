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
import javafx.scene.control.MenuButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jisa.devices.Instrument;
import jisa.gui.controls.DoubleInput;
import jisa.gui.controls.IntegerField;
import jisa.gui.controls.TableInput;
import jisa.results.ResultTable;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * GUI element for configuring instrument parameters.
 *
 * @param <I>
 */
public class ConfigPanel<I extends Instrument> extends JFXElement {

    public static final List<WeakReference<ConfigPanel<?>>> ALL = new LinkedList<>();

    private final I                                   instrument;
    private final VBox                                list;
    private final Map<String, GridPane>               grids;
    private final Map<Instrument.Parameter, NodeItem> parameters = new LinkedHashMap<>();
    private final List<Entry<?, ?>>                   entries    = new LinkedList<>();

    public static void refreshAll() {

        List<WeakReference<ConfigPanel<?>>>     toRemove = new LinkedList<>();
        Iterator<WeakReference<ConfigPanel<?>>> iterator = ALL.iterator();

        while (iterator.hasNext()) {

            WeakReference<ConfigPanel<?>> ref   = iterator.next();
            ConfigPanel<?>                panel = ref.get();

            if (panel == null) {
                iterator.remove();
            } else {
                panel.refresh();
            }

        }

    }

    public ConfigPanel(String title, I instrument) {

        super(title);
        ALL.add(new WeakReference<>(this));

        this.instrument = instrument;
        this.list       = new VBox();
        this.grids      = new LinkedHashMap<>();

        list.setSpacing(15);

        BorderPane.setMargin(getNode().getCenter(), new Insets(15.0));

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(true);
        scrollPane.setBorder(Border.EMPTY);
        scrollPane.setBackground(Background.EMPTY);
        scrollPane.setStyle("-fx-background: rgba(255,255,255,0); -fx-background-color: rgba(255,255,255,0);");
        list.setPadding(new Insets(GUI.SPACING));
        setCentreNode(scrollPane);

        BorderPane.setMargin(scrollPane, Insets.EMPTY);

        addToolbarButton("Apply All", this::applyAll);

        generateForm();

    }

    public ConfigPanel(I instrument) {
        this(instrument.getName(), instrument);
    }

    protected void reset() {

        list.getChildren().clear();
        grids.clear();
        entries.clear();

        String   group = "General";
        GridPane grid  = new GridPane();

        grid.setHgap(0);
        grid.setVgap(15);
        grid.setPadding(new Insets(GUI.SPACING));
        grid.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        TitledPane pane = new TitledPane(group, grid);

        list.getChildren().add(pane);
        grids.put(group, grid);

    }

    protected void generateForm() {

        try {
            GridPane grid = new GridPane();

            grid.setHgap(5);
            grid.setVgap(15);
            grid.setPadding(new Insets(GUI.SPACING));
            grid.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

            TitledPane pane = new TitledPane("General", grid);

            list.getChildren().add(pane);
            grids.put("General", grid);

        } finally { }

        for (Instrument.Parameter parameter : instrument.getAllParameters()) {

            String group = parameter.isGrouped() ? parameter.getGroup() : "General";

            if (!grids.containsKey(group)) {

                GridPane grid = new GridPane();

                grid.setHgap(5);
                grid.setVgap(15);
                grid.setPadding(new Insets(GUI.SPACING));
                grid.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

                TitledPane pane = new TitledPane(group, grid);

                list.getChildren().add(pane);
                grids.put(group, grid);

            }

            GridPane grid = grids.get(group);
            NodeItem item = createNode(parameter.getDefaultValue(), parameter.getChoices().toArray());

            if (item == null) {
                continue;
            }

            Label      label = new Label(parameter.getName());
            Button     set   = new Button("✓");
            MenuButton error = new SplitMenuButton(new MenuItem("More details..."));

            set.setMinWidth(Region.USE_PREF_SIZE);
            error.setMaxWidth(Double.MAX_VALUE);
            error.setTextFill(Color.RED);
            error.setVisible(false);
            error.setManaged(false);

            Node node        = item.getNode();
            HBox nodeDisplay = new HBox(0, node, error);

            label.setMinWidth(Region.USE_PREF_SIZE);

            GridPane.setVgrow(label, Priority.NEVER);
            GridPane.setVgrow(node, Priority.NEVER);
            GridPane.setHgrow(label, Priority.NEVER);
            HBox.setHgrow(node, Priority.ALWAYS);
            HBox.setHgrow(error, Priority.ALWAYS);
            GridPane.setHgrow(nodeDisplay, Priority.ALWAYS);
            GridPane.setHalignment(label, HPos.RIGHT);
            GridPane.setValignment(label, parameter.getDefaultValue() instanceof ResultTable || node instanceof VBox ? VPos.TOP : VPos.CENTER);
            GridPane.setMargin(label, new Insets(5, 0, 0, 0));
            GridPane.setMargin(label, new Insets(0, 15, 0, 0));
            GridPane.setMargin(node, new Insets(0, 5, 0, 0));
            GridPane.setValignment(set, VPos.TOP);
            GridPane.setValignment(error, VPos.TOP);


            grid.addRow(grid.getRowCount(), label, nodeDisplay, set);

            entries.add(new Entry(item, parameter, label, set, error));

        }

    }

    public void refresh() {

        for (Entry<?, ?> entry : entries) {

            if (!entry.isChanged()) {
                entry.update();
            }

        }

    }

    public void applyAll() {

        boolean triggered = instrument.beforeApplyParameters();

        for (Entry<?, ?> entry : entries) {
            entry.set(false);
        }

        refreshAll();

        instrument.afterApplyParameters(triggered);

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
            choiceBox.setMaxWidth(Double.MAX_VALUE);

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

    public class Entry<Q, N extends NodeItem<Q>> {

        private final N                       nodeItem;
        private final Instrument.Parameter<Q> parameter;
        private final Label                   text;
        private final Button                  setButton;
        private final MenuButton              errorButton;

        public Entry(N nodeItem, Instrument.Parameter<Q> parameter, Label text, Button setButton, MenuButton errorButton) {

            this.nodeItem    = nodeItem;
            this.parameter   = parameter;
            this.text        = text;
            this.setButton   = setButton;
            this.errorButton = errorButton;

            setButton.setDisable(!isChanged());

            setButton.setOnAction(event -> set(true));

            nodeItem.getNode().setOnKeyReleased(event -> {

                if (event.getCode() == KeyCode.ENTER) {
                    setButton.getOnAction().handle(null);
                }

            });

            nodeItem.addListener(i -> {

                if (isChanged()) {
                    setButton.setDisable(false);
                    text.setTextFill(Color.BROWN);
                } else {
                    setButton.setDisable(true);
                    text.setTextFill(Color.BLACK);
                }

            });

            errorButton.setOnAction(ev -> {
                errorButton.setVisible(false);
                errorButton.setManaged(false);
                nodeItem.getNode().setVisible(true);
                nodeItem.getNode().setManaged(true);
            });

        }

        public void set(boolean individual) {

            boolean triggered = individual && instrument.beforeApplyParameters();

            try {

                parameter.set(nodeItem.getValue());

            } catch (Throwable e) {

                errorButton.getItems().get(0).setOnAction(event -> GUI.showException(e));

                nodeItem.getNode().setVisible(false);
                nodeItem.getNode().setManaged(false);
                errorButton.setText("Error: " + e.getMessage());
                errorButton.setVisible(true);
                errorButton.setManaged(true);

            }

            update();

            if (individual) {
                instrument.afterApplyParameters(triggered);
                refreshAll();
            }

        }

        public void update() {

            try {
                nodeItem.setValue(parameter.getCurrentValue());
                updateLastValue();
            } catch (Throwable ignored) { }

        }

        public void updateLastValue() {
            nodeItem.updateLastValue();
            setButton.setDisable(true);
            text.setTextFill(Color.BLACK);
        }

        public boolean isChanged() {

            if (nodeItem.getValue() == null) {
                return false;
            }

            return !nodeItem.getValue().equals(nodeItem.getLastValue());
        }

        public N getNodeItem() {
            return nodeItem;
        }

        public Instrument.Parameter<Q> getParameter() {
            return parameter;
        }

        public Label getText() {
            return text;
        }

        public Button getSetButton() {
            return setButton;
        }

        public MenuButton getErrorButton() {
            return errorButton;
        }

    }

}
