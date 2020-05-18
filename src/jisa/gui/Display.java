package jisa.gui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class Display extends JFXElement {

    @FXML
    protected TableView<Parameter> table;

    @FXML
    protected BorderPane pane;

    private TableColumn<Parameter, String> name  = new TableColumn<>("Parameter");
    private TableColumn<Parameter, String> value = new TableColumn<>("Value");

    public Display(String title) {

        super(title, Table.class.getResource("fxml/TableWindow.fxml"));

        name.setCellValueFactory(row -> row.getValue().name);
        value.setCellValueFactory(row -> row.getValue().value);

        name.setStyle("-fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");

        table.getColumns().addAll(name, value);
        table.setColumnResizePolicy(resizeFeatures -> true);

        value.prefWidthProperty().bind(table.widthProperty().subtract(name.widthProperty()).subtract(5));

    }

    public Parameter addParameter(String name, Object value) {
        Parameter param = new Parameter(name, value);
        GUI.runNow(() -> table.getItems().add(param));
        return param;
    }

    public static class Parameter {

        private final SimpleObjectProperty<String> name  = new SimpleObjectProperty<>();
        private final SimpleObjectProperty<String> value = new SimpleObjectProperty<>();

        private Parameter(String name, Object value) {
            this.name.set(name);
            this.value.set(value.toString());
        }

        public void setName(String name) {
            GUI.runNow(() -> this.name.set(name));
        }

        public void setValue(Object value) {
            GUI.runNow(() -> this.value.set(value.toString()));
        }

        public String getValue() {
            return value.get();
        }

        public double getDoubleValue() {
            return Double.parseDouble(getValue());
        }

        public int getIntValue() {
            return Integer.parseInt(getValue());
        }

        public boolean getBooleanValue() {
            return getValue().equals("TRUE");
        }

    }

}
