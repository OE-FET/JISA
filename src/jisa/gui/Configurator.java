package jisa.gui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import jisa.Util;
import jisa.control.ConfigBlock;
import jisa.control.ConfigStore;
import jisa.control.IConf;
import jisa.devices.*;
import jisa.enums.Terminals;
import org.json.JSONObject;

import java.io.IOException;

public abstract class Configurator<I extends Instrument> extends Fields implements IConf<I> {

    private   JSONObject     data       = null;
    protected Connector<I>[] instruments;
    protected Field<Boolean> enabled    = addCheckBox("Enabled", true);
    protected Field<Integer> instrument = addChoice("Instrument");

    public Configurator(String title, ConfigBlock configBlock, Connector<I>... instruments) {
        super(title);
        this.instruments = instruments;

        makeFields();

        connect();

        for (Connector<I> config : this.instruments) {
            config.setOnConnect(this::connect);
        }

        if (configBlock != null) linkConfig(configBlock);

        connect();

        instrument.setOnChange(this::updateGUI);
        enabled.setOnChange(this::updateGUI);

        updateGUI();

    }

    public Configurator(String title, Connector<I>... instruments) {
        this(title, null, instruments);
    }

    protected void connect() {

        String[] names = new String[instruments.length];

        for (int i = 0; i < instruments.length; i++) {
            names[i] = String.format(
                "%s (%s)",
                instruments[i].getTitle(),
                instruments[i].isConnected() ? instruments[i].getDriver().getSimpleName() : "NOT CONNECTED"
            );
        }

        instrument.editValues(names);
        updateGUI();

    }

    protected abstract void makeFields();

    protected abstract void update();

    protected void updateGUI() {
        
        if (!enabled.get()) {
            setFieldsDisabled(true);
            enabled.setDisabled(false);
        } else {
            setFieldsDisabled(false);
            update();
        }
        
    }

    public static class SMU extends Configurator<jisa.devices.SMU> {

        private Field<Integer> chn;
        private Field<Integer> trm;
        private Field<Double>  vlm;
        private Field<Double>  ilm;
        private Field<Boolean> var;
        private Field<Double>  vrn;
        private Field<Boolean> iar;
        private Field<Double>  irn;
        private Field<Boolean> fpp;

        public SMU(String title, ConfigBlock config, Connector<jisa.devices.SMU>... instruments) {
            super(title, config, instruments);
        }

        public SMU(String title, Connector<jisa.devices.SMU>... instruments) {
            this(title, null, instruments);
        }

        @Override
        protected void makeFields() {

            chn = addChoice("Channel", 0, Util.makeCountingString(0, 4, "Channel %d"));
            trm = addChoice("Terminals", 0, "Front (NONE)", "Rear (NONE)");

            addSeparator();

            vlm = addDoubleField("Voltage Limit [V]", 200.0);
            ilm = addDoubleField("Current Limit [A]", 0.02);

            addSeparator();

            var = addCheckBox("Auto Voltage Range", true);
            vrn = addDoubleField("Voltage Range [V]", 20.0);
            iar = addCheckBox("Auto Current Range", true);
            irn = addDoubleField("Current Range [A]", 200e-3);

            var.setOnChange(this::updateGUI);
            iar.setOnChange(this::updateGUI);

            addSeparator();

            fpp = addCheckBox("Four-Wire Measurements", false);

        }

        public SMU(String title, ConfigBlock config, ConnectorGrid grid) {
            this(title, config, grid.getInstrumentsByType(jisa.devices.SMU.class));
        }

        public SMU(String title, ConnectorGrid grid) {
            this(title, grid.getInstrumentsByType(jisa.devices.SMU.class));
        }

        @Override
        protected void update() {

            int              n   = instrument.get();
            jisa.devices.SMU smu = null;

            if (Util.isValidIndex(n, instruments)) {
                smu = instruments[n].get();
            }

            chn.setDisabled(false);

            fpp.setDisabled(smu instanceof SPA);

            if (smu == null) {
                chn.editValues("Channel 0", "Channel 1", "Channel 2", "Channel 3");
            } else if (smu instanceof MultiChannel) {
                chn.editValues(Util.makeCountingString(0, ((MultiChannel) smu).getNumChannels(), "Channel %d"));
            } else {
                chn.editValues("N/A");
                chn.setDisabled(true);
            }


            try {

                if (smu != null) {
                    trm.editValues(
                        String.format("Front (%s)", smu.getTerminalType(Terminals.FRONT).name()),
                        String.format("Rear (%s)", smu.getTerminalType(Terminals.REAR).name())
                    );
                } else {
                    trm.editValues(
                        "Front (NONE)",
                        "Rear (NONE)"
                    );
                }
            } catch (Exception e) {
                trm.editValues(
                    "Front (UNKNOWN)",
                    "Rear (UNKNOWN)"
                );
            }


            vrn.setDisabled(var.get());
            irn.setDisabled(iar.get());


        }

        @Override
        public jisa.devices.SMU get() {

            if (!enabled.get()) {
                return null;
            }

            int n = instrument.get();

            if (!Util.isBetween(n, 0, instruments.length - 1)) {
                return null;
            }

            jisa.devices.SMU smu = instruments[n].get();

            if (smu == null) {
                return null;
            }

            jisa.devices.SMU toReturn;

            if (smu instanceof MultiChannel) {

                try {
                    toReturn = (jisa.devices.SMU) ((MultiChannel) smu).getChannel(chn.get());
                } catch (Exception e) {
                    return null;
                }

            } else {
                toReturn = smu;
            }

            try {

                toReturn.setLimits(vlm.get(), ilm.get());
                toReturn.setTerminals(Terminals.values()[trm.get()]);

                if (var.get()) {
                    toReturn.useAutoVoltageRange();
                } else {
                    toReturn.setVoltageRange(vrn.get());
                }

                if (iar.get()) {
                    toReturn.useAutoCurrentRange();
                } else {
                    toReturn.setCurrentRange(irn.get());
                }

                toReturn.useFourProbe(fpp.get());

            } catch (DeviceException | IOException e) {
                return null;
            }

            return toReturn;
        }

    }

    public static class VMeter extends Configurator<jisa.devices.VMeter> {

        private Field<Integer> channel;
        private Field<Integer> terminals;
        private Field<Boolean> var;
        private Field<Double>  vrn;
        private Field<Boolean> iar;
        private Field<Double>  irn;
        private Field<Boolean> fourPP;
        private Field<Boolean> manI;
        private Field<Double>  iValue;

        public VMeter(String title, ConfigBlock config, Connector<jisa.devices.VMeter>... instruments) {
            super(title, config, instruments);
            var.setOnChange(() -> vrn.setDisabled(var.get()));
            iar.setOnChange(() -> irn.setDisabled(iar.isDisabled() || iar.get()));
        }

        public VMeter(String title, Connector<jisa.devices.VMeter>... instruments) {
            this(title, null, instruments);
        }

        @Override
        protected void makeFields() {

            channel   = addChoice("Channel", Util.makeCountingString(0, 4, "Channel %d"));
            terminals = addChoice("Terminals", "Front (NONE)", "Rear (NONE)");

            addSeparator();

            var = addCheckBox("Auto Voltage Range", true);
            vrn = addDoubleField("Voltage Range [V]", 20.0);
            vrn.setDisabled(true);

            iar = addCheckBox("Auto Current Range", false);
            irn = addDoubleField("Current Range [A]", 100e-3);

            addSeparator();
            manI   = addCheckBox("Set Current (SMU)", true);
            iValue = addDoubleField("Manual Current [A]", 0.0);

            addSeparator();

            fourPP = addCheckBox("Four-Wire Measurements");
            manI.setOnChange(this::updateGUI);

        }

        public VMeter(String title, ConfigBlock config, ConnectorGrid grid) {
            this(title, config, grid.getInstrumentsByType(jisa.devices.VMeter.class));
        }

        public VMeter(String title, ConnectorGrid grid) {
            this(title, grid.getInstrumentsByType(jisa.devices.VMeter.class));
        }

        @Override
        protected void update() {

            int n = instrument.get();

            jisa.devices.VMeter vMeter = null;

            if (Util.isBetween(n, 0, instruments.length - 1)) {
                vMeter = instruments[n].get();
            }

            channel.setDisabled(false);

            if (vMeter == null) {

                channel.editValues(Util.makeCountingString(0, 4, "Channel %d"));
                terminals.editValues("Front (NONE)", "Rear (NONE)");

            } else {

                String front;
                String rear;

                try {
                    front = vMeter.getTerminalType(Terminals.FRONT).name();
                } catch (Exception e) {
                    front = "NONE";
                }

                try {
                    rear = vMeter.getTerminalType(Terminals.REAR).name();
                } catch (Exception e) {
                    rear = "NONE";
                }

                terminals.editValues(String.format("Front (%s)", front), String.format("Rear (%s)", rear));

            }

            if (vMeter instanceof MultiChannel) {
                channel.editValues(Util.makeCountingString(0, ((MultiChannel) vMeter).getNumChannels(), "Channel %d"));
            } else {
                channel.editValues("N/A");
                channel.setDisabled(true);
            }

            if (vMeter instanceof IMeter) {
                iar.setDisabled(false);
                irn.setDisabled(iar.get());
            } else {
                iar.setDisabled(true);
                irn.setDisabled(true);
            }

            if (vMeter instanceof ISource) {
                manI.setDisabled(false);
                iValue.setDisabled(!manI.get());
            } else {
                manI.setDisabled(true);
                iValue.setDisabled(true);
            }

            fourPP.setDisabled(!(vMeter instanceof jisa.devices.SMU));
            iValue.setDisabled(!manI.get() || manI.isDisabled());


        }


        @Override
        public jisa.devices.VMeter get() {

            if (!enabled.get()) {
                return null;
            }

            try {

                int n = instrument.get();

                if (!Util.isValidIndex(n, instruments)) {
                    return null;
                }

                jisa.devices.VMeter vMeter = instruments[n].get();

                if (vMeter == null) {
                    return null;
                }

                vMeter.setTerminals(Terminals.values()[terminals.get()]);

                if (var.get()) {
                    vMeter.useAutoVoltageRange();
                } else {
                    vMeter.setVoltageRange(vrn.get());
                }

                // Check for multi-channel meter
                if (vMeter instanceof MultiChannel) {
                    vMeter = (jisa.devices.VMeter) ((MultiChannel) vMeter).getChannel(channel.get());
                }

                // Check for current sourcing capability
                if (vMeter instanceof ISource && manI.get()) {
                    ((ISource) vMeter).setCurrent(iValue.get());
                }

                // Check for current measuring capability
                if (vMeter instanceof IMeter) {

                    if (iar.get()) {
                        ((IMeter) vMeter).useAutoCurrentRange();
                    } else {
                        ((IMeter) vMeter).setCurrentRange(irn.get());
                    }

                }

                // Check if SMU
                if (vMeter instanceof jisa.devices.SMU) {
                    ((jisa.devices.SMU) vMeter).useFourProbe(fourPP.get());
                }

                return vMeter;

            } catch (Exception e) {

                return null;

            }

        }

    }

    public static class TMeter extends Configurator<jisa.devices.TMeter> {

        private Field<Integer> chn;
        private Field<Double>  rng;

        public TMeter(String title, ConfigBlock config, Connector<jisa.devices.TMeter>... instruments) {
            super(title, config, instruments);
        }

        public TMeter(String title, Connector<jisa.devices.TMeter>... instruments) {
            super(title, instruments);
        }

        @Override
        protected void makeFields() {
            chn = addChoice("Sensor", 0, "Sensor 0", "Sensor 1", "Sensor 2", "Sensor 3");
            rng = addDoubleField("Range [K]", 500.0);
        }

        public TMeter(String title, ConfigBlock config, ConnectorGrid grid) {
            this(title, config, grid.getInstrumentsByType(jisa.devices.TMeter.class));
        }

        public TMeter(String title, ConnectorGrid grid) {
            this(title, grid.getInstrumentsByType(jisa.devices.TMeter.class));
        }

        @Override
        protected void update() {

            int n = instrument.get();

            jisa.devices.TMeter tm;
            if (!Util.isBetween(n, 0, instruments.length - 1)) {

                tm = null;

            } else {

                tm = instruments[n].get();

            }

            if (tm == null) {

                chn.editValues("Sensor 0", "Sensor 1", "Sensor 2", "Sensor 3");
                chn.setDisabled(false);

            } else if (tm instanceof MultiSensor) {

                chn.editValues(Util.makeCountingString(0, ((MultiSensor) tm).getNumSensors(), "Sensor %d"));
                chn.setDisabled(false);

            } else {

                chn.editValues("N/A");
                chn.setDisabled(true);

            }



        }

        @Override
        public jisa.devices.TMeter get() {

            if (!enabled.get()) {
                return null;
            }

            int n = instrument.get();

            if (!Util.isBetween(n, 0, instruments.length - 1)) {
                return null;
            }

            jisa.devices.TMeter tm = instruments[n].get();

            if (tm == null) {
                return null;
            }

            jisa.devices.TMeter toReturn;

            if (tm instanceof MultiSensor) {

                try {
                    toReturn = (jisa.devices.TMeter) ((MultiSensor) tm).getSensor(chn.get());
                } catch (Exception e) {
                    return null;
                }

            } else {
                toReturn = tm;
            }

            try {
                toReturn.setTemperatureRange(rng.get());
            } catch (Exception e) {
                return toReturn;
            }

            return toReturn;

        }

    }

    public static class TC extends Configurator<jisa.devices.TC> {

        private Field<Integer> output;
        private Field<Integer> sensor;
        private Field<Double>  pValue;
        private Field<Double>  iValue;
        private Field<Double>  dValue;

        public TC(String title, ConfigBlock config, Connector<jisa.devices.TC>... instruments) {
            super(title, config, instruments);
        }

        public TC(String title, Connector<jisa.devices.TC>... instruments) {
            super(title, instruments);
        }

        @Override
        protected void makeFields() {

            output = addChoice("Output", 0, Util.makeCountingString(0, 4, "Output %d"));
            sensor = addChoice("Sensor", 0, Util.makeCountingString(0, 4, "Sensor %d"));

            addSeparator();

            pValue = addDoubleField("P", 20.0);
            iValue = addDoubleField("I", 10.0);
            dValue = addDoubleField("D", 0.0);

        }

        @Override
        protected void update() {

            int n = instrument.get();

            jisa.devices.TC tc;

            if (!Util.isBetween(n, 0, instruments.length - 1)) {
                tc = null;
            } else {
                tc = instruments[n].get();
            }

            if (tc == null) {
                output.editValues(Util.makeCountingString(0, 4, "Output %d"));
                sensor.editValues(Util.makeCountingString(0, 4, "Sensor %d"));
                output.setDisabled(false);
                sensor.setDisabled(false);
                return;
            }

            if (tc instanceof MultiOutput) {
                output.editValues(Util.makeCountingString(0, ((MultiOutput) tc).getNumOutputs(), "Output %d"));
                output.setDisabled(false);
            } else {
                output.editValues("N/A");
                output.setDisabled(true);
            }

            if (tc instanceof MultiSensor) {
                sensor.editValues(Util.makeCountingString(0, ((MultiSensor) tc).getNumSensors(), "Sensor %d"));
                sensor.setDisabled(false);
            } else {
                sensor.editValues("N/A");
                sensor.setDisabled(true);
            }

            try {
                pValue.set(tc.getPValue());
                iValue.set(tc.getIValue());
                dValue.set(tc.getDValue());
            } catch (Exception ignored) {
            }



        }

        public TC(String title, ConfigBlock config, ConnectorGrid grid) {
            this(title, config, grid.getInstrumentsByType(jisa.devices.TC.class));
        }

        public TC(String title, ConnectorGrid grid) {
            this(title, grid.getInstrumentsByType(jisa.devices.TC.class));
        }

        @Override
        public jisa.devices.TC get() {

            if (!enabled.get()) {
                return null;
            }

            try {

                int n = instrument.get();

                if (!Util.isBetween(n, 0, instruments.length - 1)) {
                    return null;
                }

                jisa.devices.TC tc = instruments[n].get();

                if (tc == null) {
                    return null;
                }

                if (tc instanceof MultiOutput) {
                    tc = (jisa.devices.TC) ((MultiOutput) tc).getOutput(output.get());
                }

                if (tc instanceof MSTC) {
                    ((MSTC) tc).useSensor(sensor.get());
                }

                tc.setPValue(pValue.get());
                tc.setIValue(iValue.get());
                tc.setDValue(dValue.get());

                return tc;

            } catch (Exception e) {
                return null;
            }

        }

    }

}
