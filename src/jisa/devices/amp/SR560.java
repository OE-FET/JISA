package jisa.devices.amp;

import jisa.addresses.Address;
import jisa.devices.interfaces.VPreAmp;
import jisa.enums.Coupling;
import jisa.enums.Filter;
import jisa.enums.Input;
import jisa.visa.drivers.Connection;
import jisa.visa.VISADevice;

import java.io.IOException;

public class SR560 extends VISADevice implements VPreAmp {

    public static String getDescription() {
        return "Stanford Research Systems SR560";
    }

    private static final String C_LISTEN      = "LALL";
    private static final String C_COUPLING    = "CPLG %d";
    private static final String C_FILTER_MODE = "FLTM %d";
    private static final String C_GAIN        = "GAIN %d";
    private static final String C_H_FREQ      = "HFRQ %d";
    private static final String C_L_FREQ      = "LFRQ %d";
    private static final String C_SOURCE      = "SRCE %d";
    private static final String TERMINATOR    = "\r\n";

    private enum FMode {
        BYPASS(0, 0.0, Filter.NONE),
        LP_06DB(1, 6.0, Filter.LOW_PASS),
        LP_12DB(2, 12.0, Filter.LOW_PASS),
        HP_06DB(3, 6.0, Filter.HIGH_PASS),
        HP_12DB(4, 12.0, Filter.HIGH_PASS),
        BP(5, 12.0, Filter.BAND_PASS);

        private int    mode;
        private double db;
        private Filter fMode;

        static FMode fromParams(Filter mode, double db) {

            switch (mode) {

                case NONE:
                    return BYPASS;

                case LOW_PASS:
                    return db <= 6.0 ? LP_06DB : LP_12DB;

                case HIGH_PASS:
                    return db <= 6.0 ? HP_06DB : HP_12DB;

                case BAND_PASS:
                    return BP;

            }

            return BYPASS;

        }

        FMode(int mode, double db, Filter fMode) {
            this.mode = mode;
            this.db = db;
            this.fMode = fMode;
        }

        public int toInt() {
            return mode;
        }

        public double getDB() {
            return db;
        }

        public Filter getMode() {
            return fMode;
        }

    }

    private enum Gain {

        G_1(0, 1.0),
        G_2(1, 2.0),
        G_5(2, 5.0),
        G_10(3, 10.0),
        G_20(4, 20.0),
        G_50(5, 50.0),
        G_100(6, 100.0),
        G_200(7, 200.0),
        G_500(8, 500.0),
        G_1K(9, 1e3),
        G_2K(10, 2e3),
        G_5K(11, 5e3),
        G_10K(12, 10e3),
        G_20K(13, 20e3),
        G_50K(14, 50e3);

        private int    mode;
        private double gain;

        public static Gain fromDouble(double gain) {

            Gain[] values = values();
            Gain   match  = G_50K;

            for (Gain g : values) {
                if (g.getGain() >= gain && g.getGain() < match.getGain()) {
                    match = g;
                }
            }

            return match;

        }

        Gain(int mode, double gain) {
            this.mode = mode;
            this.gain = gain;
        }

        public int toInt() {
            return mode;
        }

        public double getGain() {
            return gain;
        }

    }

    private enum Freq {

        F_0_03_HZ(0, 0.03),
        F_0_10_HZ(1, 0.10),
        F_0_30_HZ(2, 0.30),
        F_1_HZ(3, 1.00),
        F_3_HZ(4, 3.00),
        F_10_HZ(5, 10.0),
        F_30_HZ(6, 30.0),
        F_100_HZ(7, 100.0),
        F_300_HZ(8, 300.0),
        F_1K_HZ(9, 1e3),
        F_3K_HZ(10, 3e3),
        F_10K_HZ(11, 10e3),
        F_30K_HZ(12, 30e3),
        F_100K_HZ(13, 100e3),
        F_300K_HZ(14, 300e3),
        F_1M_HZ(15, 1e6);

        private int    mode;
        private double frequency;

        public static Freq fromDouble(double freq) {

            Freq[] values = values();
            Freq   match  = F_1M_HZ;

            for (Freq f : values) {
                if (Math.abs(f.getFrequency() - freq) < Math.abs(match.getFrequency() - freq)) {
                    match = f;
                }
            }

            return match;

        }

        Freq(int m, double f) {
            mode = m;
            frequency = f;
        }

        public int toInt() {
            return mode;
        }

        public double getFrequency() {
            return frequency;
        }

    }

    private Coupling couplingMode = null;
    private FMode    filterMode   = FMode.BYPASS;
    private Gain     gainMode     = null;
    private Input    sourceMode   = null;
    private Freq     highFreq     = null;
    private Freq     lowFreq      = null;

    public SR560(Address address) throws IOException {

        super(address);

        setSerialParameters(9600, 8, Connection.Parity.NONE, Connection.StopBits.TWO, Connection.Flow.NONE);

        setWriteTerminator(TERMINATOR);

        // Tell device to listen
        write(C_LISTEN);

        // Set default parameters
        setGain(1.0);
        setInput(Input.A);
        setCoupling(Coupling.GROUND);
        setFilterMode(Filter.NONE);
        setFilterHighFrequency(1.0);
        setFilterLowFrequency(1.0);

    }

    public String getIDN() {
        return "Stanford Research SR560";
    }

    @Override
    public void setGain(double gain) throws IOException {
        Gain mode = Gain.fromDouble(gain);
        write(C_GAIN, mode.toInt());
        gainMode = mode;
    }

    @Override
    public void setInput(Input source) throws IOException {

        switch (source) {

            case A:
                write(C_SOURCE, 0);
                break;

            case B:
                write(C_SOURCE, 2);
                break;

            case DIFF:
                write(C_SOURCE, 1);
                break;

        }

        sourceMode = source;

    }

    @Override
    public void setCoupling(Coupling mode) throws IOException {

        switch (mode) {

            case GROUND:
                write(C_COUPLING, 0);
                break;

            case DC:
                write(C_COUPLING, 1);
                break;

            case AC:
                write(C_COUPLING, 2);
                break;

        }

        couplingMode = mode;

    }

    @Override
    public void setFilterMode(Filter mode) throws IOException {

        FMode m = FMode.fromParams(mode, filterMode.getDB());
        write(C_FILTER_MODE, m.toInt());
        filterMode = m;

    }

    @Override
    public void setFilterRollOff(double dbLevel) throws IOException {

        FMode m = FMode.fromParams(filterMode.getMode(), dbLevel);
        write(C_FILTER_MODE, m.toInt());
        filterMode = m;

    }

    @Override
    public void setFilterHighFrequency(double frequency) throws IOException {

        frequency = Math.min(frequency, 10e3);
        Freq f = Freq.fromDouble(frequency);
        write(C_H_FREQ, f.toInt());
        highFreq = f;

    }

    @Override
    public void setFilterLowFrequency(double frequency) throws IOException {
        Freq f = Freq.fromDouble(frequency);
        write(C_L_FREQ, f.toInt());
        lowFreq = f;
    }

    @Override
    public double getGain() {
        return gainMode.getGain();
    }

    @Override
    public Input getInput() {
        return sourceMode;
    }

    @Override
    public Coupling getCoupling() {
        return couplingMode;
    }

    @Override
    public Filter getFilterMode() {
        return filterMode.getMode();
    }

    @Override
    public double getFilterRollOff() {
        return filterMode.getDB();
    }

    @Override
    public double getFilterHighFrequency() {
        return highFreq.getFrequency();
    }

    @Override
    public double getFilterLowFrequency() {
        return lowFreq.getFrequency();
    }
}
