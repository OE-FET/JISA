package JISA.GUI;

import JISA.Control.Field;
import JISA.Devices.DeviceException;
import JISA.Devices.MCSMU;
import JISA.Devices.SMU;
import JISA.Devices.TController;

import java.io.IOException;

public class SMUConfig extends Fields {

    private Field<Integer>          smu = addChoice("SMU", "SMU 1", "SMU 2");
    private Field<Integer>          chn = addChoice("Channel", "Channel 0", "Channel 1", "Channel 2", "Channel 3");
    private Field<Integer>          trm = addChoice("Terminals", "Front (NONE)", "Rear (NONE)");
    private Field<Double>           lim = addDoubleField("Current Limit [A]");
    private InstrumentConfig<SMU>[] instruments;

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

        smu.setOnChange(() -> update(false));

    }

    public void update(boolean connect) {

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

}
