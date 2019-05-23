package JISA.GUI;

import JISA.Control.ConfigStore;
import JISA.Control.Field;
import JISA.Devices.MSTMeter;
import JISA.Devices.TMeter;
import org.json.JSONObject;

import java.io.IOException;

public class TMeterConfig extends Fields {


    private Field<Integer>             inst   = addChoice("Instrument");
    private Field<Integer>             chn    = addChoice("Sensor", "Sensor 0", "Sensor 1", "Sensor 2", "Sensor 3");
    private InstrumentConfig<TMeter>[] instruments;
    private ConfigStore                config = null;
    private String                     key    = null;
    private JSONObject                 data   = null;

    public TMeterConfig(String title, String key, ConfigStore config, ConfigGrid configGrid) {
        this(title, configGrid);
        this.config = config;
        this.key = key;
        load();
    }

    public TMeterConfig(String title, String key, ConfigStore config, InstrumentConfig<TMeter>... instruments) {
        this(title, instruments);
        this.config = config;
        this.key = key;
        load();
    }

    public TMeterConfig(String title, ConfigGrid configGrid) {
        this(title, configGrid.getInstrumentsByType(TMeter.class));
    }

    public TMeterConfig(String title, InstrumentConfig<TMeter>... instruments) {
        super(title);
        this.instruments = instruments;
        chn.set(0);

        String[] names = new String[instruments.length];

        for (int i = 0; i < instruments.length; i++) {
            names[i] = instruments[i].getTitle();
        }

        inst.editValues(names);
        inst.set(0);

        for (InstrumentConfig<TMeter> config : instruments) {
            config.setOnConnect(() -> update(true));
        }

        update(true);

        inst.setOnChange(() -> update(false));

    }

    public synchronized void update(boolean connect) {

        if (connect) {
            String[] names = new String[instruments.length];

            for (int i = 0; i < instruments.length; i++) {
                names[i] = String.format("%s (%s)", instruments[i].getTitle(), instruments[i].isConnected() ? instruments[i].getDriver().getSimpleName() : "NOT CONNECTED");
            }

            inst.editValues(names);
        }

        int smuI = inst.get();

        TMeter tm;
        if (smuI < 0 || smuI >= instruments.length) {
            tm = null;
        } else {
            tm = instruments[smuI].get();
        }

        if (tm == null) {
            chn.editValues("Sensor 0", "Sensor 1", "Sensor 2", "Sensor 3");
        } else if (tm instanceof MSTMeter) {

            int num = 4;
            try {
                num = ((MSTMeter) tm).getNumSensors();
            } catch (Exception ignored) {}

            String[] channels = new String[num];

            for (int i = 0; i < channels.length; i++) {
                channels[i] = String.format("Sensor %d", i);
            }

            chn.editValues(channels);

        } else {
            chn.editValues("N/A");
        }

    }

    public TMeter getTMeter() {


        int smuI = this.inst.get();

        if (smuI < 0 || smuI >= instruments.length) {
            return null;
        }

        TMeter tm = instruments[smuI].get();

        if (tm == null) {
            return null;
        }

        TMeter toReturn;

        if (tm instanceof MSTMeter) {

            try {
                toReturn = ((MSTMeter) tm).getSensor(chn.get());
            } catch (Exception e) {
                return null;
            }

        } else {
            toReturn = tm;
        }

        return toReturn;

    }

    private void saveCHN() {

        if (config != null && key != null && data != null) {
            data.put("sensor", chn.get());
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

                data.put("inst", inst.get());
                data.put("sensor", chn.get());

                config.save();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {

        if (data == null) {
            data = config.getInstConfig(key);
        }

        if (data == null) {
            save();
        }

        chn.set(data.getInt("sensor"));
        inst.set(data.getInt("inst"));

        chn.setOnChange(this::saveCHN);

        inst.setOnChange(() -> {
            update(false);
            save();
        });

    }
}
