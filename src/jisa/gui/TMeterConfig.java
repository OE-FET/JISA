package jisa.gui;

import jisa.Util;
import jisa.control.ConfigStore;
import jisa.control.Field;
import jisa.control.IConf;
import jisa.devices.MultiSensor;
import jisa.devices.TMeter;
import org.json.JSONObject;

import java.io.IOException;

public class TMeterConfig extends Fields implements IConf<TMeter> {


    private Field<Integer>      inst   = addChoice("Instrument", 0);
    private Field<Integer>      chn    = addChoice("Sensor", 0, "Sensor 0", "Sensor 1", "Sensor 2", "Sensor 3");
    private Field<Double>       rng    = addDoubleField("Range [K]", 500.0);
    private Connector<TMeter>[] instruments;
    private ConfigStore         config = null;
    private String              key    = null;
    private JSONObject          data   = null;

    public TMeterConfig(String title, String key, ConfigStore config, ConnectorGrid connectorGrid) {
        this(title, connectorGrid);
        this.config = config;
        this.key    = key;
        load();
    }

    public TMeterConfig(String title, String key, ConfigStore config, Connector<TMeter>... instruments) {
        this(title, instruments);
        this.config = config;
        this.key    = key;
        load();
    }

    public TMeterConfig(String title, ConnectorGrid connectorGrid) {
        this(title, connectorGrid.getInstrumentsByType(TMeter.class));
    }

    public TMeterConfig(String title, Connector<TMeter>... instruments) {

        super(title);
        this.instruments = instruments;
        chn.set(0);

        String[] names = new String[instruments.length];

        for (int i = 0; i < instruments.length; i++) {
            names[i] = instruments[i].getTitle();
        }

        inst.editValues(names);
        inst.set(0);

        for (Connector<TMeter> config : instruments) {
            config.setOnConnect(() -> update(true));
        }

        update(true);

        inst.setOnChange(() -> update(false));

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

        } else if (tm instanceof MultiSensor) {

            chn.editValues(Util.makeCountingString(0, ((MultiSensor) tm).getNumSensors(), "Sensor %d"));

        } else {

            chn.editValues("N/A");

        }

    }

    public TMeter get() {


        int smuI = this.inst.get();

        if (smuI < 0 || smuI >= instruments.length) {
            return null;
        }

        TMeter tm = instruments[smuI].get();

        if (tm == null) {
            return null;
        }

        TMeter toReturn;

        if (tm instanceof MultiSensor) {

            try {
                toReturn = (TMeter) ((MultiSensor) tm).getSensor(chn.get());
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
                data.put("range", rng.get());

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

        if (data.has("sensor")) chn.set(data.getInt("sensor"));
        if (data.has("inst")) inst.set(data.getInt("inst"));
        if (data.has("range")) rng.set(data.getDouble("range"));

        // Make sure parameters are saved on exit
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));

    }

}
