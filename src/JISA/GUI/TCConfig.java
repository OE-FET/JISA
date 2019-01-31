package JISA.GUI;

import JISA.Control.ConfigStore;
import JISA.Devices.DeviceException;
import JISA.Devices.MSMOTC;
import JISA.Devices.MSTC;
import JISA.Devices.TC;
import JISA.Util;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

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

    private final InstrumentConfig<TC>[] instruments;

    private final static int CHOICE_SINGLE = 0;
    private final static int CHOICE_ZONING = 1;

    private TC.PIDZone[] zones;
    private ConfigStore  config = null;
    private String       key    = null;
    private JSONObject   data   = null;

    public TCConfig(String title, String key, ConfigStore config, ConfigGrid configGrid) {

        this(title, configGrid);
        this.config = config;
        this.key = key;
        load();

    }

    public TCConfig(String title, String key, ConfigStore config, InstrumentConfig<TC>... instruments) {

        this(title, instruments);
        this.config = config;
        this.key = key;
        load();

    }

    public TCConfig(String title, ConfigGrid configGrid) {
        this(title, configGrid.getInstrumentsByType(TC.class));
    }

    public TCConfig(String title, InstrumentConfig<TC>... instruments) {

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

        for (InstrumentConfig<TC> config : instruments) {
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

        TC controller;
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

        } else if (controller instanceof MSMOTC) {

            int nO = ((MSMOTC) controller).getNumOutputs();
            int nS = ((MSMOTC) controller).getNumSensors();
            output.getItems().clear();
            output.getItems().addAll(Util.makeCountingString(0, nO, "Output %d"));
            sensor.getItems().clear();
            sensor.getItems().addAll(Util.makeCountingString(0, nS, "Sensor %d"));
            output.getSelectionModel().select(o);
            sensor.getSelectionModel().select(s);

        } else if (controller instanceof MSTC) {

            int nS = ((MSTC) controller).getNumSensors();
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

    private synchronized void updateZones() {

        zones = new TC.PIDZone[table.getItems().size()];

        for (int i = 0; i < zones.length; i++) {

            ZoneRow row = table.getItems().get(i);

            if (row.getHeat() < 0) {
                zones[i] = new TC.PIDZone(row.getMin(), row.getMax(), row.getP(), row.getI(), row.getD(), row.getRange());
            } else {
                zones[i] = new TC.PIDZone(row.getMin(), row.getMax(), row.getHeat(), row.getRange());
            }

        }

    }

    public TC getTController() throws IOException, DeviceException {

        int index = this.controller.getSelectionModel().getSelectedIndex();

        TC controller;
        if (index < 0 || index >= instruments.length) {
            controller = null;
        } else {
            controller = instruments[index].get();
        }

        TC toReturn = null;

        if (controller == null) {
            return null;
        } else if (controller instanceof MSMOTC) {
            int o = output.getSelectionModel().getSelectedIndex();
            int s = sensor.getSelectionModel().getSelectedIndex();
            ((MSMOTC) controller).useSensor(o, s);
            toReturn = ((MSMOTC) controller).getOutput(o);
        } else if (controller instanceof MSTC) {
            int s = sensor.getSelectionModel().getSelectedIndex();
            ((MSTC) controller).useSensor(s);
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

    private void save() {

        try {

            if (config != null && key != null) {

                data = config.getInstConfig(key);

                if (data == null) {
                    data = new JSONObject();
                    config.saveInstConfig(key, data);
                }

                data.put("controller", controller.getSelectionModel().getSelectedIndex());
                data.put("output", output.getSelectionModel().getSelectedIndex());
                data.put("sensor", sensor.getSelectionModel().getSelectedIndex());
                data.put("pidType", pidType.getSelectionModel().getSelectedIndex());
                data.put("P", pValue.getText());
                data.put("I", iValue.getText());
                data.put("D", dValue.getText());

                JSONArray zoneRows = new JSONArray();

                for (TC.PIDZone zone : zones) {
                    zoneRows.put(zone.toJSON());
                }

                data.put("zones", zoneRows);

                config.save();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void load() {

        data = config.getInstConfig(key);

        if (data == null) {
            save();
        }

        controller.getSelectionModel().select(data.getInt("controller"));
        output.getSelectionModel().select(data.getInt("output"));
        sensor.getSelectionModel().select(data.getInt("sensor"));
        pidType.getSelectionModel().select(data.getInt("pidType"));
        pValue.setText(data.getString("P"));
        iValue.setText(data.getString("I"));
        dValue.setText(data.getString("D"));

        table.getItems().clear();

        JSONArray zoneRows = data.getJSONArray("zones");

        for (int i = 0; i < zoneRows.length(); i++) {

            table.getItems().add(
                    new ZoneRow(new TC.PIDZone(zoneRows.getJSONObject(i)))
            );

        }

        updateZones();

    }

    public static class ZoneRow {

        private double min   = 0;
        private double max   = 100;
        private double p     = 10;
        private double i     = 5;
        private double d     = 0;
        private double range = 100;
        private double heat  = -1;

        public ZoneRow() {

        }

        public ZoneRow(TC.PIDZone zone) {

            min = zone.getMinT();
            max = zone.getMaxT();
            p = zone.getP();
            i = zone.getI();
            d = zone.getD();
            range = zone.getRange();
            heat = zone.isAuto() ? -1 : zone.getPower();

        }

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
