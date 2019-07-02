package JISA.GUI;

import JISA.Control.ConfigStore;
import JISA.Control.Field;
import JISA.Devices.MCSMU;
import JISA.Devices.SMU;
import JISA.Devices.VMeter;
import JISA.Util;
import org.json.JSONObject;

public class VMeterConfig extends Fields {

    private ConfigStore config = null;
    private String      key    = null;

    private InstrumentConfig<VMeter>[] instruments;

    private Field<Integer> choice  = addChoice("Instrument");
    private Field<Integer> channel = addChoice("Channel", Util.makeCountingString(0, 4, "Channel %d"));
    private Field<Boolean> zero    = addCheckBox("Zero Current (SMU)", true);

    public VMeterConfig(String title, InstrumentConfig<VMeter>... instruments) {

        super(title);
        this.instruments = instruments;

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
        }

        if (vMeter instanceof MCSMU) {
            channel.editValues(Util.makeCountingString(0, ((MCSMU) vMeter).getNumChannels(), "Channel %d"));
        } else {
            channel.editValues("N/A");
        }

        if (vMeter instanceof SMU) {
            zero.setDisabled(false);
        } else {
            zero.setDisabled(true);
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

            if (vMeter instanceof MCSMU) {
                vMeter = ((MCSMU) vMeter).getChannel(channel.get());
            }

            if (vMeter instanceof SMU) {
                vMeter = zero.get() ? ((SMU) vMeter).asVoltmeter() : vMeter;
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

            choice.set(data.getInt("choice"));
            channel.set(data.getInt("channel"));
            zero.set(data.getBoolean("zero"));

            choice.setOnChange(() -> {
                update(false);
                save();
            });

            channel.setOnChange(this::save);
            zero.setOnChange(this::save);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
