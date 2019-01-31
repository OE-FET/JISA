package JISA.GUI;

import JISA.Control.ConfigStore;
import JISA.Control.Field;
import JISA.Devices.DeviceException;
import JISA.Devices.MCSMU;
import JISA.Devices.SMU;
import org.json.JSONObject;

import java.io.IOException;

public class SMUConfig extends Fields {

    private Field<Integer>          smu    = addChoice("SMU", "SMU 1", "SMU 2");
    private Field<Integer>          chn    = addChoice("Channel", "Channel 0", "Channel 1", "Channel 2", "Channel 3");
    private Field<Integer>          trm    = addChoice("Terminals", "Front (NONE)", "Rear (NONE)");
    private Field<Double>           lim    = addDoubleField("Current Limit [A]");
    private InstrumentConfig<SMU>[] instruments;
    private ConfigStore             config = null;
    private String                  key    = null;
    private JSONObject              data   = null;

    public SMUConfig(String title, String key, ConfigStore config, ConfigGrid configGrid) {
        this(title, configGrid);
        this.config = config;
        this.key = key;
        load();
    }

    public SMUConfig(String title, String key, ConfigStore config, InstrumentConfig<SMU>... instruments) {
        this(title, instruments);
        this.config = config;
        this.key = key;
        load();
    }

    public SMUConfig(String title, ConfigGrid configGrid) {
        this(title, configGrid.getInstrumentsByType(SMU.class));
    }

    public SMUConfig(String title, InstrumentConfig<SMU>... instruments) {
        super(title);
        this.instruments = instruments;
        chn.set(0);
        trm.set(0);
        lim.set(0.02);

        String[] names = new String[instruments.length];

        for (int i = 0; i < instruments.length; i++) {
            names[i] = instruments[i].getTitle();
        }

        smu.editValues(names);
        smu.set(0);

        for (InstrumentConfig<SMU> config : instruments) {
            config.setOnConnect(() -> update(true));
        }
        update(true);

    }

    public synchronized void update(boolean connect) {

        int c    = getChannel();
        int t    = trm.get();
        int smuI = smu.get();

        if (connect) {
            String[] names = new String[instruments.length];

            for (int i = 0; i < instruments.length; i++) {
                names[i] = String.format("%s (%s)", instruments[i].getTitle(), instruments[i].isConnected() ? instruments[i].getDriver().getSimpleName() : "NOT CONNECTED");
            }

            smu.editValues(names);
            smu.set(smuI);
            return;
        }

        SMU smu;
        if (smuI < 0 || smuI >= instruments.length) {
            smu = null;
        } else {
            smu = instruments[smuI].get();
        }

        if (smu == null) {
            chn.editValues("Channel 0", "Channel 1", "Channel 2", "Channel 3");
            chn.set(c);
        } else if (smu instanceof MCSMU) {
            String[] channels = new String[((MCSMU) smu).getNumChannels()];
            for (int i = 0; i < channels.length; i++) {
                channels[i] = String.format("Channel %d", i);
            }
            chn.editValues(channels);
            chn.set(c);
        } else {
            chn.editValues("N/A");
            chn.set(0);
        }

        try {
            if (smu != null) {
                trm.editValues(
                        String.format("Front (%s)", smu.getTerminalType(SMU.Terminals.FRONT).name()),
                        String.format("Rear (%s)", smu.getTerminalType(SMU.Terminals.REAR).name())
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

        trm.set(t);

    }

    public void setValues(int smu, int chn, int trm, double lim) {
        this.smu.set(smu);
        this.chn.set(chn);
        this.trm.set(trm);
        this.lim.set(lim);
    }

    public SMU getSMU() {


        int smuI = this.smu.get();

        if (smuI < 0 || smuI >= instruments.length) {
            return null;
        }

        SMU smu = instruments[smuI].get();

        if (smu == null) {
            return null;
        }

        SMU toReturn;

        if (smu instanceof MCSMU) {

            try {
                toReturn = ((MCSMU) smu).getChannel(getChannel());
            } catch (Exception e) {
                return null;
            }

        } else {
            toReturn = smu;
        }

        try {
            toReturn.setCurrentLimit(getLimit());
            toReturn.setTerminals(getTerminals());
        } catch (DeviceException | IOException e) {
            return null;
        }

        return toReturn;

    }

    public int getChannel() {
        return chn.get();
    }

    public SMU.Terminals getTerminals() {
        return SMU.Terminals.values()[trm.get()];
    }

    public double getLimit() {
        return lim.get();
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
                data.put("limit", lim.get());

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
            data.put("limit", lim.get());
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
        lim.set(data.getDouble("limit"));
        smu.set(data.getInt("smu"));

        chn.setOnChange(this::saveCHN);
        trm.setOnChange(this::saveTRM);
        lim.setOnChange(this::saveLIM);

        smu.setOnChange(() -> {
            update(false);
            save();
        });

    }


}
