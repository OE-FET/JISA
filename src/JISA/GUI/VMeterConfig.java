package JISA.GUI;

import JISA.Control.ConfigStore;
import JISA.Control.Field;
import JISA.Control.IConf;
import JISA.Devices.MCSMU;
import JISA.Devices.SMU;
import JISA.Devices.VMeter;
import JISA.Enums.Terminals;
import JISA.Util;
import org.json.JSONObject;

public class VMeterConfig extends Fields implements IConf<VMeter> {

    private ConfigStore config = null;
    private String      key    = null;

    private InstrumentConfig<VMeter>[] instruments;

    private Field<Integer> choice    = addChoice("Instrument");
    private Field<Integer> channel   = addChoice("Channel", Util.makeCountingString(0, 4, "Channel %d"));
    private Field<Integer> terminals = addChoice("Terminals", "Front (NONE)", "Rear (NONE)");

    { addSeparator(); }

    private Field<Boolean> var       = addCheckBox("Auto Voltage Range", true);
    private Field<Double>  vrn       = addDoubleField("Voltage Range [V]", 20.0);
    private Field<Boolean> iar       = addCheckBox("Auto Current Range", false);
    private Field<Double>  irn       = addDoubleField("Current Range [A]", 100e-3);

    { addSeparator(); }

    private Field<Boolean> fourPP    = addCheckBox("Four-Wire Measurements");
    private Field<Boolean> zero      = addCheckBox("Zero Current (SMU)", true);

    public VMeterConfig(String title, InstrumentConfig<VMeter>... instruments) {

        super(title);
        this.instruments = instruments;

        vrn.setDisabled(true);
        iar.setDisabled(true);
        irn.setDisabled(true);

        var.setOnChange(() -> vrn.setDisabled(var.get()));
        iar.setOnChange(() -> irn.setDisabled(iar.isDisabled() || iar.get()));

        String[] names = new String[instruments.length];

        for (int i = 0; i < names.length; i++) {
            names[i] = instruments[i].getTitle();
        }

        choice.editValues(names);
        choice.set(0);

        for (InstrumentConfig<VMeter> config : instruments) {
            config.setOnConnect(() -> update(true));
        }

        choice.setOnChange(() -> update(false));

        update(true);

    }

    public VMeterConfig(String title, ConfigGrid grid) {

        this(title, grid.getInstrumentsByType(VMeter.class));

    }

    public VMeterConfig(String title, String key, ConfigStore config, InstrumentConfig<VMeter>... instruments) {

        this(title, instruments);
        this.config = config;
        this.key    = key;
        load();

    }

    public VMeterConfig(String title, String key, ConfigStore config, ConfigGrid grid) {

        this(title, key, config, grid.getInstrumentsByType(VMeter.class));

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

            choice.editValues(names);

        }

        int n = choice.get();

        VMeter vMeter = null;

        if (n >= 0 && n < instruments.length) {
            vMeter = instruments[n].get();
        }

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

        channel.setDisabled(false);

        if (vMeter instanceof MCSMU) {
            channel.editValues(Util.makeCountingString(0, ((MCSMU) vMeter).getNumChannels(), "Channel %d"));
        } else {
            channel.editValues("N/A");
            channel.setDisabled(true);
        }

        if (vMeter instanceof SMU) {
            zero.setDisabled(false);
            fourPP.setDisabled(false);
            iar.setDisabled(false);
            irn.setDisabled(iar.get());
        } else {
            zero.setDisabled(true);
            fourPP.setDisabled(true);
            iar.setDisabled(true);
            irn.setDisabled(true);
        }

    }

    public VMeter get() {

        try {

            int n = choice.get();

            if (n < 0 || n >= instruments.length) {
                return null;
            }

            VMeter vMeter = instruments[n].get();

            if (vMeter == null) {
                return null;
            }

            vMeter.setTerminals(Terminals.values()[terminals.get()]);

            if (vMeter instanceof MCSMU) {
                vMeter = ((MCSMU) vMeter).getChannel(channel.get());
            }

            if (vMeter instanceof SMU) {

                ((SMU) vMeter).useFourProbe(fourPP.get());

                if (zero.get()) ((SMU) vMeter).setCurrent(0.0);

                if (iar.get()) {
                    ((SMU) vMeter).useAutoCurrentRange();
                } else {
                    ((SMU) vMeter).setCurrentRange(irn.get());
                }

            }

            if (var.get()) {
                vMeter.useAutoVoltageRange();
            } else {
                vMeter.setVoltageRange(vrn.get());
            }

            return vMeter;

        } catch (Exception e) {

            return null;

        }

    }

    private void save() {

        if (config != null && key != null) {

            JSONObject data = config.getInstConfig(key);
            data.put("choice", choice.get());
            data.put("channel", channel.get());
            data.put("terminals", terminals.get());
            data.put("var", var.get());
            data.put("vrn", vrn.get());
            data.put("iar", iar.get());
            data.put("irn", irn.get());
            data.put("fourPP", fourPP.get());
            data.put("zero", zero.get());

            try {
                config.save();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void load() {

        try {

            JSONObject data = config.getInstConfig(key);

            if (data == null) {
                data = new JSONObject();
                config.saveInstConfig(key, data);
                save();
            }

            choice.set(data.has("choice") ? data.getInt("choice") : 0);
            channel.set(data.has("channel") ? data.getInt("channel") : 0);
            terminals.set(data.has("terminals") ? data.getInt("terminals") : 0);
            var.set(data.has("var") ? data.getBoolean("var") : true);
            vrn.set(data.has("vrn") ? data.getDouble("vrn") : 20.0);
            iar.set(data.has("iar") ? data.getBoolean("iar") : true);
            irn.set(data.has("irn") ? data.getDouble("irn") : 100e-3);
            fourPP.set(data.has("fourPP") && data.getBoolean("fourPP"));
            zero.set(!data.has("zero") || data.getBoolean("zero"));

            choice.setOnChange(() -> {
                update(false);
                save();
            });

            vrn.setOnChange(this::save);
            irn.setOnChange(this::save);
            var.setOnChange(() -> {
                vrn.setDisabled(var.get());
                save();
            });
            iar.setOnChange(() -> {
                irn.setDisabled(iar.get());
                save();
            });

            channel.setOnChange(this::save);
            zero.setOnChange(this::save);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
