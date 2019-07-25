package jisa.gui;

import jisa.Util;
import jisa.control.ConfigStore;
import jisa.control.Field;
import jisa.control.IConf;
import jisa.devices.DeviceException;
import jisa.devices.MultiChannel;
import jisa.devices.SMU;
import jisa.enums.Terminals;
import org.json.JSONObject;

import java.io.IOException;

public class SMUConfig extends Fields implements IConf<SMU> {

    private Field<Integer>          smu    = addChoice("SMU", "SMU 1", "SMU 2");
    private Field<Integer>          chn    = addChoice("Channel", "Channel 0", "Channel 1", "Channel 2", "Channel 3");
    private Field<Integer>          trm    = addChoice("Terminals", "Front (NONE)", "Rear (NONE)");

    { addSeparator(); }

    private Field<Double>           vlm    = addDoubleField("Voltage Limit [V]");
    private Field<Double>           ilm    = addDoubleField("Current Limit [A]");

    { addSeparator(); }

    private Field<Boolean>          var    = addCheckBox("Auto Voltage Range");
    private Field<Double>           vrn    = addDoubleField("Voltage Range [V]");
    private Field<Boolean>          iar    = addCheckBox("Auto Current Range");
    private Field<Double>           irn    = addDoubleField("Current Range [A]");

    { addSeparator(); }

    private Field<Boolean>   fpp    = addCheckBox("Four-Wire Measurements", false);
    private Connector<SMU>[] instruments;
    private ConfigStore      config = null;
    private String           key    = null;
    private JSONObject       data   = null;

    public SMUConfig(String title, String key, ConfigStore config, ConnectorGrid connectorGrid) {
        this(title, connectorGrid);
        this.config = config;
        this.key    = key;
        load();
    }

    public SMUConfig(String title, String key, ConfigStore config, Connector<SMU>... instruments) {
        this(title, instruments);
        this.config = config;
        this.key    = key;
        load();
    }

    public SMUConfig(String title, ConnectorGrid connectorGrid) {
        this(title, connectorGrid.getInstrumentsByType(SMU.class));
    }

    public SMUConfig(String title, Connector<SMU>... instruments) {

        super(title);
        this.instruments = instruments;
        chn.set(0);
        trm.set(0);
        vlm.set(200.0);
        ilm.set(0.02);
        var.set(true);
        vrn.set(20.0);
        iar.set(true);
        irn.set(100e-3);

        vrn.setDisabled(true);
        irn.setDisabled(true);

        var.setOnChange(() -> vrn.setDisabled(var.get()));
        iar.setOnChange(() -> irn.setDisabled(iar.get()));

        String[] names = new String[instruments.length];

        for (int i = 0; i < instruments.length; i++) {
            names[i] = instruments[i].getTitle();
        }

        smu.editValues(names);
        smu.set(0);

        for (Connector<SMU> config : instruments) {
            config.setOnConnect(() -> update(true));
        }

        update(true);

        smu.setOnChange(() -> update(false));

    }

    public synchronized void update(boolean connect) {

        if (connect) {
            String[] names = new String[instruments.length];

            for (int i = 0; i < instruments.length; i++) {
                names[i] = String.format(
                        "%s (%s)",
                        instruments[i].getTitle(),
                        instruments[i].isConnected() ? instruments[i].getDriver().getSimpleName() : "NOT CONNECTED"
                );
            }

            smu.editValues(names);
        }

        int smuI = smu.get();

        SMU smu;
        if (smuI < 0 || smuI >= instruments.length) {
            smu = null;
        } else {
            smu = instruments[smuI].get();
        }

        chn.setDisabled(false);

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


    }

    public void setValues(int smu, int chn, int trm, double vLim, double iLim) {
        this.smu.set(smu);
        this.chn.set(chn);
        this.trm.set(trm);
        this.vlm.set(vLim);
        this.ilm.set(iLim);
    }

    public SMU get() {

        int smuI = this.smu.get();

        if (smuI < 0 || smuI >= instruments.length) {
            return null;
        }

        SMU smu = instruments[smuI].get();

        if (smu == null) {
            return null;
        }

        SMU toReturn;

        if (smu instanceof MultiChannel) {

            try {
                toReturn = (SMU) ((MultiChannel) smu).getChannel(getChannel());
            } catch (Exception e) {
                return null;
            }

        } else {
            toReturn = smu;
        }

        try {

            toReturn.setLimits(getVoltageLimit(), getCurrentLimit());
            toReturn.setTerminals(getTerminals());

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

    public int getChannel() {
        return chn.get();
    }

    public Terminals getTerminals() {
        return Terminals.values()[trm.get()];
    }

    public double getVoltageLimit() {
        return vlm.get();
    }

    public double getCurrentLimit() {
        return ilm.get();
    }

    private void save() {

        try {

            if (config != null && key != null) {

                if (data == null) {
                    data = config.getInstConfig(key);
                }

                if (data == null) {
                    data = new JSONObject();
                    config.saveInstConfig(key, data);
                }

                data.put("smu", smu.get());
                data.put("channel", chn.get());
                data.put("terminals", trm.get());
                data.put("vimit", vlm.get());
                data.put("limit", ilm.get());
                data.put("var", var.get());
                data.put("vrn", vrn.get());
                data.put("iar", iar.get());
                data.put("irn", irn.get());
                data.put("fpp", fpp.get());

                config.save();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSMU() {
        if (config != null && key != null && data != null) {
            data.put("smu", smu.get());
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveCHN() {
        if (config != null && key != null && data != null) {
            data.put("channel", chn.get());
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveTRM() {
        if (config != null && key != null && data != null) {
            data.put("terminals", trm.get());
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveLIM() {
        if (config != null && key != null && data != null) {
            data.put("limit", ilm.get());
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveVLM() {
        if (config != null && key != null && data != null) {
            data.put("vimit", vlm.get());
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveRanges() {

        if (config != null && key != null && data != null) {

            data.put("var", var.get());
            data.put("vrn", vrn.get());
            data.put("iar", iar.get());
            data.put("irn", irn.get());
            data.put("fpp", fpp.get());

            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void load() {

        if (data == null) {
            data = config.getInstConfig(key);
        }

        if (data == null) {
            save();
        }

        chn.set(data.getInt("channel"));
        trm.set(data.getInt("terminals"));
        vlm.set(data.has("vimit") ? data.getDouble("vimit") : 200.0);
        ilm.set(data.has("limit") ? data.getDouble("limit") : 0.02);
        smu.set(data.getInt("smu"));
        var.set(data.has("var") ? data.getBoolean("var") : true);
        vrn.set(data.has("vrn") ? data.getDouble("vrn") : 20.0);
        iar.set(data.has("iar") ? data.getBoolean("iar") : true);
        irn.set(data.has("irn") ? data.getDouble("irn") : 100e-3);
        fpp.set(data.has("fpp") ? data.getBoolean("fpp") : false);

        chn.setOnChange(this::saveCHN);
        trm.setOnChange(this::saveTRM);
        vlm.setOnChange(this::saveVLM);
        ilm.setOnChange(this::saveLIM);
        vrn.setOnChange(this::saveRanges);
        irn.setOnChange(this::saveRanges);
        fpp.setOnChange(this::saveRanges);

        var.setOnChange(() -> {
            vrn.setDisabled(var.get());
            saveRanges();
        });

        iar.setOnChange(() -> {
            irn.setDisabled(iar.get());
            saveRanges();
        });

        smu.setOnChange(() -> {
            update(false);
            save();
        });

    }


}
