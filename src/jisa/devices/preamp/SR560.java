package jisa.devices.preamp;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.enums.Filter;
import jisa.visa.VISADevice;
import jisa.visa.connections.SerialConnection;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
    private static final String C_INVERT      = "INVT %d";
    private static final String TERMINATOR    = "\r\n";

    private static final int FILTER_NONE  = 0;
    private static final int FILTER_LP_6  = 1;
    private static final int FILTER_LP_12 = 2;
    private static final int FILTER_HP_6  = 3;
    private static final int FILTER_HP_12 = 4;
    private static final int FILTER_BP_12 = 5;

    public static class Inputs {

        public static final Input<Integer> A    = new Input<>(0, false, 100e6, "A");
        public static final Input<Integer> B    = new Input<>(2, false, 100e6, "B");
        public static final Input<Integer> DIFF = new Input<>(1, true, 100e6, "A-B");

        public static final List<Input<Integer>> ALL = List.of(A, B, DIFF);

    }

    private Coupling couplingMode = null;
    private Gain     gainMode     = null;
    private Input    sourceMode   = null;
    private double   rollOff      = 12;
    private double   highFreq     = 0.0;
    private double   lowFreq      = 0.0;
    private boolean  inverting    = false;

    public SR560(Address address) throws IOException, DeviceException {

        super(address);

        configSerial(serial -> serial.setSerialParameters(9600, 7, SerialConnection.Parity.NONE, 2));

        setWriteTerminator(TERMINATOR);

        // Tell device to listen
        write(C_LISTEN);

        // Set default parameters
        setGain(1.0);
        setInput(Inputs.A);
        setCoupling(Coupling.GROUND);
        setFilter(0.0, 0.0, 12.0);
        setInverting(false);

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
    public void setInput(Input source) throws IOException, DeviceException {

        if (!Inputs.ALL.contains(source)) {
            throw new DeviceException("That is not a valid input for this SR560!");
        }

        write(C_SOURCE, ((Input<Integer>) source).getValue());

        sourceMode = source;

    }

    @Override
    public List<Input<Integer>> getInputs() throws IOException, DeviceException {
        return Inputs.ALL;
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
    public void setHighPassFrequency(double frequency) throws IOException {
        setFilter(lowFreq, frequency, rollOff);
    }

    @Override
    public double getHighPassFrequency() {
        return highFreq;
    }

    @Override
    public void setLowPassFrequency(double frequency) throws IOException {
        setFilter(frequency, highFreq, rollOff);
    }

    @Override
    public double getLowPassFrequency() {
        return lowFreq;
    }

    protected void setFilter(double lowFreq, double highFreq, double rollOff) throws IOException {

        FMode mode;

        if (lowFreq == 0 && highFreq == 0) {
            mode = FMode.BYPASS;
        } else if (lowFreq == 0) {
            mode = FMode.fromParams(Filter.HIGH_PASS, rollOff);
        } else if (highFreq == 0) {
            mode = FMode.fromParams(Filter.LOW_PASS, rollOff);
        } else {
            mode = FMode.fromParams(Filter.BAND_PASS, rollOff);
        }

        Freq low  = Freq.fromDouble(lowFreq);
        Freq high = Freq.fromDouble(highFreq);

        write(C_FILTER_MODE, mode.toInt());
        this.rollOff = mode.getDB();

        if (highFreq > 0) {
            write(C_L_FREQ, low.toInt());
            this.highFreq = high.getFrequency();
        } else {
            this.highFreq = 0;
        }

        if (lowFreq > 0) {
            write(C_H_FREQ, high.toInt());
            this.lowFreq = low.getFrequency();
        } else {
            this.lowFreq = 0;
        }

    }

    @Override
    public void setFilterRollOff(double dbPerOct) throws IOException {
        setFilter(lowFreq, highFreq, dbPerOct);
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
    public double getFilterRollOff() {
        return rollOff;
    }

    @Override
    public boolean isInverting() {
        return inverting;
    }

    @Override
    public void setInverting(boolean inverting) throws IOException {
        write(C_INVERT, inverting ? 1 : 0);
        this.inverting = inverting;
    }

    private enum FMode {

        BYPASS(0, 0.0, Filter.NONE),
        LP_06DB(1, 6.0, Filter.LOW_PASS),
        LP_12DB(2, 12.0, Filter.LOW_PASS),
        HP_06DB(3, 6.0, Filter.HIGH_PASS),
        HP_12DB(4, 12.0, Filter.HIGH_PASS),
        BP(5, 12.0, Filter.BAND_PASS);

        private final int    mode;
        private final double db;
        private final Filter fMode;

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
            this.mode  = mode;
            this.db    = db;
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

        private final int    mode;
        private final double gain;

        public static Gain fromDouble(double gain) {
            return Arrays.stream(values()).filter(g -> g.getGain() >= gain).min(Comparator.comparingDouble(Gain::getGain)).orElse(G_1);
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

        private final int    mode;
        private final double frequency;

        public static Freq fromDouble(double freq) {
            return Arrays.stream(values()).min(Comparator.comparingDouble(f -> Math.abs(f.frequency - freq))).orElse(F_0_03_HZ);
        }

        Freq(int m, double f) {
            mode      = m;
            frequency = f;
        }

        public int toInt() {
            return mode;
        }

        public double getFrequency() {
            return frequency;
        }

    }

}
