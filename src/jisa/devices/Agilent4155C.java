package jisa.devices;

import jisa.addresses.Address;
import jisa.control.*;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.visa.VISADevice;

import java.io.IOException;

import static jisa.devices.Agilent4155C.VoltRange.*;
import static jisa.enums.AMode.*;
import static jisa.enums.Source.*;

public class Agilent4155C extends VISADevice implements SPA {

    public static final String C_RESET = "*RST";
    public static final String C_FLEX  = "US";

    private final boolean[]   states       = {false, false, false, false, false, false};
    private final Source[]    modes        = {VOLTAGE, VOLTAGE, VOLTAGE, VOLTAGE, VOLTAGE, VOLTAGE};
    private final VoltRange[] voltageRange = {AUTO_RANGING, AUTO_RANGING, AUTO_RANGING, AUTO_RANGING, AUTO_RANGING, AUTO_RANGING};
    private final int[]       currentRange = {0, 0, 0, 0, 0, 0};

    private final ReadFilter[] voltageFilters = {
        makeVoltageFilter(0, NONE),
        makeVoltageFilter(0, NONE),
        makeVoltageFilter(0, NONE),
        makeVoltageFilter(0, NONE),
        makeVoltageFilter(0, NONE),
        makeVoltageFilter(5, NONE)
    };

    private final int[]   filterCounts = {1, 1, 1, 1, 1, 1};
    private final AMode[] filterModes  = {NONE, NONE, NONE, NONE, NONE, NONE};

    private final ReadFilter[] currentFilters = {
        makeCurrentFilter(0, NONE),
        makeCurrentFilter(0, NONE),
        makeCurrentFilter(0, NONE),
        makeCurrentFilter(0, NONE),
        makeCurrentFilter(0, NONE),
        makeCurrentFilter(5, NONE)
    };

    private final double[] compliance = {100, 100, 100, 100, 100, 100};
    private final double[] values     = {0, 0, 0, 0, 0, 0};

    public Agilent4155C(Address address) throws IOException, DeviceException {

        super(address);

        setTerminator("\r");
        setReadTerminationCharacter("\r");

        write(C_RESET);
        write(C_FLEX);

        String[] idn = getIDN().split(",");

        if (!idn[1].contains("4155C")) {
            throw new DeviceException("Device at %s is not an Agilent 4155C.", address.toString());
        }

        for (int i = 0; i < getNumChannels(); i++) {
            turnOff(i);
            write("FL %d,0", i + 1);
        }

    }

    @Override
    public double getVoltage(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble("TV? %d,%d", channel + 1, voltageRange[channel]);
    }

    @Override
    public double getCurrent(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble("TI? %d,%d", channel + 1, currentRange[channel]);
    }

    protected double measureVoltage(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble("TV? %d,%d", channel + 1, voltageRange[channel]);
    }

    protected double measureCurrent(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble("TI? %d,%d", channel + 1, currentRange[channel]);
    }

    protected ReadFilter makeVoltageFilter(int channel, AMode type) {

        switch (type) {

            case MEAN_REPEAT:
                return new MeanRepeatFilter(() -> measureVoltage(channel), (c) -> {});

            case MEAN_MOVING:
                return new MeanMovingFilter(() -> measureVoltage(channel), (c) -> {});

            case MEDIAN_REPEAT:
                return new MedianRepeatFilter(() -> measureVoltage(channel), (c) -> {});

            case MEDIAN_MOVING:
                return new MedianMovingFilter(() -> measureVoltage(channel), (c) -> {});

            default:
            case NONE:
                return new BypassFilter(() -> measureVoltage(channel), (c) -> {});

        }

    }

    protected ReadFilter makeCurrentFilter(int channel, AMode type) {

        switch (type) {

            case MEAN_REPEAT:
                return new MeanRepeatFilter(() -> measureCurrent(channel), (c) -> {});

            case MEAN_MOVING:
                return new MeanMovingFilter(() -> measureCurrent(channel), (c) -> {});

            case MEDIAN_REPEAT:
                return new MedianRepeatFilter(() -> measureCurrent(channel), (c) -> {});

            case MEDIAN_MOVING:
                return new MedianMovingFilter(() -> measureCurrent(channel), (c) -> {});

            default:
            case NONE:
                return new BypassFilter(() -> measureCurrent(channel), (c) -> {});

        }

    }

    @Override
    public void setVoltage(int channel, double voltage) throws DeviceException, IOException {
        checkChannel(channel);
        modes[channel]  = VOLTAGE;
        values[channel] = voltage;
        write("DV %d,%d,%e", channel + 1, voltageRange[channel], voltage);
    }

    @Override
    public void setCurrent(int channel, double current) throws DeviceException, IOException {
        checkChannel(channel);
        modes[channel]  = CURRENT;
        values[channel] = current;
        write("DI %d,%d,%e", channel + 1, currentRange[channel], current);
    }

    @Override
    public void turnOn(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        states[channel] = true;
        write("CN %d", channel + 1);
    }

    @Override
    public void turnOff(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        states[channel] = false;
        write("CL %d", channel + 1);
    }

    @Override
    public boolean isOn(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return states[channel];
    }

    @Override
    public void setSource(int channel, Source source) throws DeviceException, IOException {

        checkChannel(channel);
        turnOff(channel);

        switch (source) {

            case VOLTAGE:
                setVoltage(channel, values[channel]);
                break;

            case CURRENT:
                setCurrent(channel, values[channel]);
                break;

        }

    }

    @Override
    public Source getSource(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return modes[channel];
    }

    @Override
    public void setBias(int channel, double level) throws DeviceException, IOException {

        switch (getSource(channel)) {

            case VOLTAGE:
                setVoltage(channel, level);
                break;

            case CURRENT:
                setCurrent(channel, level);
                break;

        }

    }

    @Override
    public double getSourceValue(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            default:
            case VOLTAGE:
                return getVoltage(channel);

            case CURRENT:
                return getCurrent(channel);

        }

    }

    @Override
    public double getMeasureValue(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            default:
            case VOLTAGE:
                return getCurrent(channel);

            case CURRENT:
                return getVoltage(channel);

        }

    }

    @Override
    public int getNumChannels() {
        return 6;
    }

    @Override
    public void useFourProbe(int channel, boolean fourProbes) throws DeviceException, IOException {
        checkChannel(channel);
    }

    @Override
    public boolean isUsingFourProbe(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return false;
    }

    @Override
    public void setAverageMode(int channel, AMode mode) throws DeviceException, IOException {

        checkChannel(channel);

        voltageFilters[channel] = makeVoltageFilter(channel, mode);
        voltageFilters[channel].setCount(filterCounts[channel]);
        voltageFilters[channel].setUp();
        voltageFilters[channel].clear();

        currentFilters[channel] = makeCurrentFilter(channel, mode);
        currentFilters[channel].setCount(filterCounts[channel]);
        currentFilters[channel].setUp();
        currentFilters[channel].clear();

        filterModes[channel] = mode;

    }

    @Override
    public void setAverageCount(int channel, int count) throws DeviceException, IOException {

        checkChannel(channel);

        filterCounts[channel] = count;
        voltageFilters[channel].setCount(filterCounts[channel]);
        voltageFilters[channel].setUp();
        voltageFilters[channel].clear();

        filterCounts[channel] = count;
        currentFilters[channel].setCount(filterCounts[channel]);
        currentFilters[channel].setUp();
        currentFilters[channel].clear();

    }

    @Override
    public int getAverageCount(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return filterCounts[channel];
    }

    @Override
    public AMode getAverageMode(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return filterModes[channel];
    }

    @Override
    public void setSourceRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getSourceRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoSourceRange(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingSource(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setMeasureRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getMeasureRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoMeasureRange(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingMeasure(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setVoltageRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoVoltageRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        voltageRange[channel] = AUTO_RANGING;
    }

    @Override
    public boolean isAutoRangingVoltage(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setCurrentRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoCurrentRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
    }

    @Override
    public boolean isAutoRangingCurrent(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setOutputLimit(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getOutputLimit(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setVoltageLimit(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageLimit(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setCurrentLimit(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentLimit(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setIntegrationTime(int channel, double time) throws DeviceException, IOException {

    }

    @Override
    public double getIntegrationTime(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public TType getTerminalType(int channel, Terminals terminals) throws DeviceException, IOException {
        return TType.BNC;
    }

    @Override
    public void setTerminals(int channel, Terminals terminals) throws DeviceException, IOException {
        checkChannel(channel);
    }

    @Override
    public Terminals getTerminals(int channel) throws DeviceException, IOException {
        return Terminals.FRONT;
    }

    @Override
    public void setOffMode(int channel, OffMode mode) throws DeviceException, IOException {

    }

    @Override
    public OffMode getOffMode(int channel) throws DeviceException, IOException {
        return null;
    }

    protected enum VoltRange {

        AUTO_RANGING(0, -1),
        R_200_mV(-10, 0.2),
        R_2_V(-11, 2.0),
        R_20_V(-12, 20.0),
        R_40_V(-13, 40.0),
        R_100_V(-14, 100.0),
        R_200_V(-15, 200.0);

        private final int    code;
        private final double range;

        VoltRange(int code, double range) {
            this.code  = code;
            this.range = range;
        }

        public static VoltRange fromInt(int code) {

            for (VoltRange range : values()) {

                if (range.toInt() == code) {
                    return range;
                }

            }

            return AUTO_RANGING;

        }

        public static VoltRange fromVoltage(double value) {

            for (VoltRange range : values()) {

                if (range.getRange() >= Math.abs(value)) {
                    return range;
                }

            }

            return AUTO_RANGING;

        }

        public int toInt() {
            return code;
        }

        public double getRange() {
            return range;
        }

    }

    protected enum CurrRange {

        AUTO_RANGING(0, -1),
        R_10_pA(-9, 10e-12),
        R_100_pA(-10, 100e-12),
        R_1_nA(-11, 1e-9),
        R_10_nA(-12, 10e-9);

        private final int    code;
        private final double range;

        CurrRange(int code, double range) {
            this.code  = code;
            this.range = range;
        }

        public static CurrRange fromInt(int code) {

            for (CurrRange range : values()) {

                if (range.toInt() == code) {
                    return range;
                }

            }

            return AUTO_RANGING;

        }

        public static CurrRange fromVoltage(double value) {

            for (CurrRange range : values()) {

                if (range.getRange() >= Math.abs(value)) {
                    return range;
                }

            }

            return AUTO_RANGING;

        }

        public int toInt() {
            return code;
        }

        public double getRange() {
            return range;
        }

    }

}
