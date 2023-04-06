package jisa.gui;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

import java.util.List;
import java.util.stream.Collectors;

public class Legend extends TilePane {

    private static final int GAP = 5;

    // -------------- PRIVATE FIELDS ------------------------------------------

    private final ListChangeListener<Legend.LegendItem> itemsListener = c -> {
        List<Label> labels = getItems().stream()
                                       .map(i -> i.label)
                                       .collect(Collectors.toList());
        getChildren().setAll(labels);
        if(isVisible()) requestLayout();
    };

    // -------------- PUBLIC PROPERTIES ----------------------------------------
    /** The legend items should be laid out vertically in columns rather than
     * horizontally in rows
     */
    private final BooleanProperty vertical = new BooleanPropertyBase(false) {
        @Override protected void invalidated() {
            setOrientation(get() ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        }

        @Override
        public Object getBean() {
            return Legend.this;
        }

        @Override
        public String getName() {
            return "vertical";
        }
    };
    public final boolean isVertical() { return vertical.get(); }
    public final void setVertical(boolean value) { vertical.set(value); }
    public final BooleanProperty verticalProperty() { return vertical; }

    /** The legend items to display in this legend */
    private final ObjectProperty<ObservableList<Legend.LegendItem>> items = new ObjectPropertyBase<ObservableList<Legend.LegendItem>>() {
        ObservableList<Legend.LegendItem> oldItems = null;
        @Override protected void invalidated() {
            if (oldItems != null) oldItems.removeListener(itemsListener);
            getChildren().clear();
            ObservableList<Legend.LegendItem> newItems = get();
            if (newItems != null) {
                newItems.addListener(itemsListener);
                List<Label> labels = newItems.stream()
                                             .map(i -> i.label)
                                             .collect(Collectors.toList());
                getChildren().addAll(labels);
            }
            oldItems = newItems;
            requestLayout();
        }

        @Override
        public Object getBean() {
            return Legend.this;
        }

        @Override
        public String getName() {
            return "items";
        }
    };
    public final void setItems(ObservableList<Legend.LegendItem> value)            {itemsProperty().set(value);}
    public final ObservableList<Legend.LegendItem> getItems()                      { return items.get();}
    public final ObjectProperty<ObservableList<Legend.LegendItem>> itemsProperty() {return items;}

    // -------------- PROTECTED PROPERTIES ------------------------------------

    // -------------- CONSTRUCTORS ----------------------------------------------

    public Legend() {
        super(GAP, GAP);
        setTileAlignment(Pos.CENTER_LEFT);
        setItems(FXCollections.observableArrayList());
        getStyleClass().setAll("chart-legend");
    }

    // -------------- METHODS ---------------------------------------------------

    @Override
    protected double computePrefWidth(double forHeight) {
        // Legend prefWidth is zero if there are no legend items
        return (getItems().size() > 0) ? super.computePrefWidth(forHeight) : 0;
    }

    @Override
    protected double computePrefHeight(double forWidth) {
        // Legend prefHeight is zero if there are no legend items
        return (getItems().size() > 0) ? super.computePrefHeight(forWidth) : 0;
    }

    /** A item to be displayed on a Legend */
    public static class LegendItem {

        /** Label used to represent the legend item */
        private final Label label = new Label();

        /** The item text */
        private final StringProperty text = new StringPropertyBase() {
            @Override protected void invalidated() {
                label.setText(get());
            }

            @Override
            public Object getBean() {
                return Legend.LegendItem.this;
            }

            @Override
            public String getName() {
                return "text";
            }
        };
        public final String getText() { return text.getValue(); }
        public final void setText(String value) { text.setValue(value); }
        public final StringProperty textProperty() { return text; }

        /** The symbol to use next to the item text, set to null for no symbol. The default is a simple square of symbolFill */
        //new Rectangle(8,8,null)
        private final ObjectProperty<Node> symbol = new ObjectPropertyBase<Node>(new Region()) {
            @Override protected void invalidated() {
                Node symbol = get();
                if(symbol != null) symbol.getStyleClass().setAll("chart-legend-item-symbol");
                label.setGraphic(symbol);
            }

            @Override
            public Object getBean() {
                return Legend.LegendItem.this;
            }

            @Override
            public String getName() {
                return "symbol";
            }
        };
        public final Node getSymbol() { return symbol.getValue(); }
        public final void setSymbol(Node value) { symbol.setValue(value); }
        public final ObjectProperty<Node> symbolProperty() { return symbol; }

        public LegendItem(String text) {
            setText(text);
            label.getStyleClass().add("chart-legend-item");
            label.setAlignment(Pos.CENTER_LEFT);
            label.setContentDisplay(ContentDisplay.LEFT);
            label.setGraphic(getSymbol());
            getSymbol().getStyleClass().setAll("chart-legend-item-symbol");
        }

        public LegendItem(String text, Node symbol) {
            this(text);
            setSymbol(symbol);
        }
    }
}
