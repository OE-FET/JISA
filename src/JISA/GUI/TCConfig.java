package JISA.GUI;

import JISA.Devices.DeviceException;
import JISA.Devices.MSMOTController;
import JISA.Devices.MSTController;
import JISA.Devices.TController;
import JISA.Util;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;

import java.io.IOException;

public class TCConfig extends JFXWindow {

    public ChoiceBox<String>            controller;
    public ChoiceBox<String>            output;
    public ChoiceBox<String>            sensor;
    public ChoiceBox<String>            pidType;
    public VBox                         autoPID;
    public HBox                         pBox;
    public HBox                         iBox;
    public HBox                         dBox;
    public TableView<ZoneRow>           table;
    public Button                       remove;
    public TableColumn<ZoneRow, Double> minCol;
    public TableColumn<ZoneRow, Double> maxCol;
    public TableColumn<ZoneRow, Double> pCol;
    public TableColumn<ZoneRow, Double> iCol;
    public TableColumn<ZoneRow, Double> dCol;
    public TableColumn<ZoneRow, Double> rangeCol;
    public TableColumn<ZoneRow, Double> heatCol;
    public TextField                    pValue;
    public TextField                    iValue;
    public TextField                    dValue;

    private final InstrumentConfig<TController>[] instruments;

    private final static int CHOICE_SINGLE = 0;
    private final static int CHOICE_ZONING = 1;

    private TController.PIDZone[] zones;

    public TCConfig(String title, InstrumentConfig<TController>... instruments) {

        super(title, TCConfig.class.getResource("FXML/TCConfigWindow.fxml"));

        this.instruments = instruments;

        String[] names = new String[instruments.length];

        for (int i = 0; i < instruments.length; i++) {
            names[i] = instruments[i].getTitle();
        }

        controller.getItems().addAll(names);
        controller.getSelectionModel().select(0);

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);

        pidType.getSelectionModel().selectedIndexProperty().addListener(ae -> typeChange());
        pidType.getSelectionModel().select(CHOICE_SINGLE);
        pValue.setText("20.0");
        iValue.setText("10.0");
        dValue.setText("0.0");

        minCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        minCol.setOnEditCommit(event -> event.getRowValue().setMin(event.getNewValue()));

        maxCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        maxCol.setOnEditCommit(event -> event.getRowValue().setMax(event.getNewValue()));

        pCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        pCol.setOnEditCommit(event -> event.getRowValue().setP(event.getNewValue()));

        iCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        iCol.setOnEditCommit(event -> event.getRowValue().setI(event.getNewValue()));

        dCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        dCol.setOnEditCommit(event -> event.getRowValue().setD(event.getNewValue()));

        rangeCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        rangeCol.setOnEditCommit(event -> event.getRowValue().setRange(event.getNewValue()));

        heatCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        heatCol.setOnEditCommit(event -> event.getRowValue().setHeat(event.getNewValue()));

        for (InstrumentConfig<TController> config : instruments) {
            config.setOnConnect(() -> update(true));
        }
        update(true);

        controller.getSelectionModel().selectedIndexProperty().addListener(event -> update(false));

    }

    private void update(boolean connect) {

        int index = controller.getSelectionModel().getSelectedIndex();

        if (connect) {
            String[] names = new String[instruments.length];

            for (int i = 0; i < instruments.length; i++) {
                names[i] = String.format("%s (%s)", instruments[i].getTitle(), instruments[i].isConnected() ? instruments[i].getDriver().getSimpleName() : "NOT CONNECTED");
            }

            controller.getItems().clear();
            controller.getItems().addAll(names);
            controller.getSelectionModel().select(index);
        }

        TController controller;
        if (index < 0 || index >= instruments.length) {
            controller = null;
        } else {
            controller = instruments[index].get();
        }


        int o = Math.max(0, output.getSelectionModel().getSelectedIndex());
        int s = Math.max(0, sensor.getSelectionModel().getSelectedIndex());

        if (controller == null) {

            output.getItems().clear();
            output.getItems().addAll(Util.makeCountingString(0, 4, "Output %d"));
            sensor.getItems().clear();
            sensor.getItems().addAll(Util.makeCountingString(0, 4, "Sensor %d"));
            output.getSelectionModel().select(o);
            sensor.getSelectionModel().select(s);

        } else if (controller instanceof MSMOTController) {

            int nO = ((MSMOTController) controller).getNumOutputs();
            int nS = ((MSMOTController) controller).getNumSensors();
            output.getItems().clear();
            output.getItems().addAll(Util.makeCountingString(0, nO, "Output %d"));
            sensor.getItems().clear();
            sensor.getItems().addAll(Util.makeCountingString(0, nS, "Sensor %d"));
            output.getSelectionModel().select(o);
            sensor.getSelectionModel().select(s);

        } else if (controller instanceof MSTController) {

            int nS = ((MSTController) controller).getNumSensors();
            output.getItems().clear();
            output.getItems().add("N/A");
            sensor.getItems().clear();
            sensor.getItems().addAll(Util.makeCountingString(0, nS, "Sensor %d"));
            output.getSelectionModel().select(0);
            sensor.getSelectionModel().select(s);

        } else {

            output.getItems().clear();
            sensor.getItems().clear();

            output.getItems().add("N/A");
            sensor.getItems().add("N/A");

            output.getSelectionModel().select(0);
            sensor.getSelectionModel().select(0);

        }

    }

    private void typeChange() {

        switch (pidType.getSelectionModel().getSelectedIndex()) {

            case CHOICE_SINGLE:
                autoPID.setVisible(false);
                autoPID.setManaged(false);
                pBox.setVisible(true);
                pBox.setManaged(true);
                iBox.setVisible(true);
                iBox.setManaged(true);
                dBox.setVisible(true);
                dBox.setManaged(true);
                break;

            case CHOICE_ZONING:
                autoPID.setVisible(true);
                autoPID.setManaged(true);
                pBox.setVisible(false);
                pBox.setManaged(false);
                iBox.setVisible(false);
                iBox.setManaged(false);
                dBox.setVisible(false);
                dBox.setManaged(false);
                break;

        }

    }

    public void addRow() {
        table.getItems().add(
                new ZoneRow()
        );
        updateZones();
    }

    public void removeRow() {
        int row = table.getSelectionModel().getSelectedCells().get(0).getRow();

        if (row >= 0 && row < table.getItems().size()) {
            table.getItems().remove(row);
            updateZones();
        }
    }

    private void updateZones() {

        zones = new TController.PIDZone[table.getItems().size()];

        for (int i = 0; i < zones.length; i++) {

            ZoneRow row = table.getItems().get(i);

            if (row.getHeat() < 0) {
                zones[i] = new TController.PIDZone(row.getMin(), row.getMax(), row.getP(), row.getI(), row.getD(), row.getRange());
            } else {
                zones[i] = new TController.PIDZone(row.getMin(), row.getMax(), row.getHeat(), row.getRange());
            }

        }


        for (TController.PIDZone zone : zones) {

            System.out.printf(
                    "Min: %s K, Max: %s K, P: %s, I: %s, D: %s, R: %s %%, W: %s %%\n",
                    zone.getMinT(),
                    zone.getMaxT(),
                    zone.getP(),
                    zone.getI(),
                    zone.getD(),
                    zone.getRange(),
                    zone.getPower()
            );

        }

    }

    public TController getTController() throws IOException, DeviceException {

        int index = this.controller.getSelectionModel().getSelectedIndex();

        TController controller;
        if (index < 0 || index >= instruments.length) {
            controller = null;
        } else {
            controller = instruments[index].get();
        }

        TController toReturn = null;

        if (controller == null) {
            return null;
        } else if (controller instanceof MSMOTController) {
            int o = output.getSelectionModel().getSelectedIndex();
            int s = sensor.getSelectionModel().getSelectedIndex();
            ((MSMOTController) controller).useSensor(o, s);
            toReturn = ((MSMOTController) controller).getOutput(o);
        } else if (controller instanceof MSTController) {
            int s = sensor.getSelectionModel().getSelectedIndex();
            ((MSTController) controller).useSensor(s);
            toReturn = controller;
        } else {
            toReturn = controller;
        }

        switch (pidType.getSelectionModel().getSelectedIndex()) {

            case CHOICE_SINGLE:
                toReturn.useAutoPID(false);
                toReturn.setPValue(Double.valueOf(pValue.getText()));
                toReturn.setIValue(Double.valueOf(iValue.getText()));
                toReturn.setDValue(Double.valueOf(dValue.getText()));
                break;

            case CHOICE_ZONING:
                toReturn.setAutoPIDZones(zones);
                toReturn.useAutoPID(true);
                break;

        }

        return toReturn;

    }

    public static class ZoneRow {

        private double min   = 0;
        private double max   = 100;
        private double p     = 10;
        private double i     = 5;
        private double d     = 0;
        private double range = 100;
        private double heat  = -1;

        public Double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public Double getMax() {
            return max;
        }

        public void setMax(Double max) {
            this.max = max;
        }

        public Double getP() {
            return p;
        }

        public void setP(Double p) {
            this.p = p;
        }

        public Double getI() {
            return i;
        }

        public void setI(Double i) {
            this.i = i;
        }

        public Double getD() {
            return d;
        }

        public void setD(Double d) {
            this.d = d;
        }

        public Double getRange() {
            return range;
        }

        public void setRange(Double range) {
            this.range = range;
        }

        public Double getHeat() {
            return heat;
        }

        public void setHeat(Double heat) {
            this.heat = heat;
        }
    }

}
