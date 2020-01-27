package jisa.devices;

import jisa.Util;
import jisa.addresses.Address;
import jisa.control.*;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.visa.VISADevice;

import java.io.IOException;

import static jisa.enums.AMode.*;
import static jisa.enums.Source.*;

public class Agilent4155C extends VISADevice implements SPA {

    public static final String C_RESET = "*RST";
    public static final String C_FLEX  = "US";

    private final boolean[]   states       = {false, false, false, false, false, false};
    private final Source[]    modes        = {VOLTAGE, VOLTAGE, VOLTAGE, VOLTAGE, VOLTAGE, VOLTAGE};
    private final VoltRange[] voltageRange = {VoltRange.AUTO_RANGING, VoltRange.AUTO_RANGING, VoltRange.AUTO_RANGING, VoltRange.AUTO_RANGING, VoltRange.AUTO_RANGING, VoltRange.AUTO_RANGING};
    private final CurrRange[] currentRange = {CurrRange.AUTO_RANGING, CurrRange.AUTO_RANGING, CurrRange.AUTO_RANGING, CurrRange.AUTO_RANGING, CurrRange.AUTO_RANGING, CurrRange.AUTO_RANGING};
    private final double[]    currentComp  = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
    private final double[]    voltageComp  = {40, 40, 40, 40, 40, 40};
    private final double[]    intTimes     = {0.02, 0.02, 0.02, 0.02, 0.02, 0.02};
    private       double      lastIntTime  = 0.02;

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
        updateIntTime(channel);
        return voltageFilters[channel].getValue();
    }

    @Override
    public double getCurrent(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        updateIntTime(channel);
        return currentFilters[channel].getValue();
    }

    protected void updateIntTime(int channel) throws IOException {

        if (lastIntTime != intTimes[channel]) {
            lastIntTime = intTimes[channel];
            int mode = lastIntTime <= 10.16e-3 ? 1 : 3;
            write("SIT %d,%e", mode, lastIntTime);
            write("SLI %d", mode);
        }

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
        write("DV %d,%d,%e,%e", channel + 1, voltageRange[channel], voltage, currentComp[channel]);
    }

    @Override
    public void setCurrent(int channel, double current) throws DeviceException, IOException {
        checkChannel(channel);
        modes[channel]  = CURRENT;
        values[channel] = current;
        write("DI %d,%d,%e,%e", channel + 1, currentRange[channel], current, voltageComp[channel]);
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

        switch (getSource(channel)) {

            case VOLTAGE:
                setVoltageRange(channel, value);
                break;

            case CURRENT:
                setCurrentRange(channel, value);
                break;

        }

    }

    @Override
    public double getSourceRange(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            default:
            case VOLTAGE:
                return getVoltageRange(channel);

            case CURRENT:
                return getCurrentRange(channel);

        }

    }

    @Override
    public void useAutoSourceRange(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            case VOLTAGE:
                useAutoVoltageRange(channel);
                break;

            case CURRENT:
                useAutoCurrentRange(channel);
                break;

        }

    }

    @Override
    public boolean isAutoRangingSource(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            default:
            case VOLTAGE:
                return isAutoRangingVoltage(channel);

            case CURRENT:
                return isAutoRangingCurrent(channel);

        }

    }

    @Override
    public void setMeasureRange(int channel, double value) throws DeviceException, IOException {

        switch (getSource(channel)) {

            case VOLTAGE:
                setCurrentRange(channel, value);
                break;

            case CURRENT:
                setVoltageRange(channel, value);
                break;

        }

    }

    @Override
    public double getMeasureRange(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            default:
            case VOLTAGE:
                return getCurrentRange(channel);

            case CURRENT:
                return getVoltageRange(channel);

        }

    }

    @Override
    public void useAutoMeasureRange(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            case VOLTAGE:
                useAutoCurrentRange(channel);
                break;

            case CURRENT:
                useAutoVoltageRange(channel);
                break;

        }

    }

    @Override
    public boolean isAutoRangingMeasure(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            default:
            case VOLTAGE:
                return isAutoRangingCurrent(channel);

            case CURRENT:
                return isAutoRangingVoltage(channel);

        }

    }

    @Override
    public void setVoltageRange(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        voltageRange[channel] = VoltRange.fromVoltage(value);
    }

    @Override
    public double getVoltageRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return voltageRange[channel].getRange();
    }

    @Override
    public void useAutoVoltageRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        voltageRange[channel] = VoltRange.AUTO_RANGING;
    }

    @Override
    public boolean isAutoRangingVoltage(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return voltageRange[channel] == VoltRange.AUTO_RANGING;
    }

    @Override
    public void setCurrentRange(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        currentRange[channel] = CurrRange.fromCurrent(value);
    }

    @Override
    public double getCurrentRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return currentRange[channel].getRange();
    }

    @Override
    public void useAutoCurrentRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        currentRange[channel] = CurrRange.AUTO_RANGING;
    }

    @Override
    public boolean isAutoRangingCurrent(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return currentRange[channel] == CurrRange.AUTO_RANGING;
    }

    @Override
    public void setOutputLimit(int channel, double value) throws DeviceException, IOException {

        switch (getSource(channel)) {

            case VOLTAGE:
                setCurrentLimit(channel, value);
                break;

            case CURRENT:
                setVoltageLimit(channel, value);
                break;

        }

    }

    @Override
    public double getOutputLimit(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            default:
            case VOLTAGE:
                return getCurrentLimit(channel);

            case CURRENT:
                return getVoltageLimit(channel);

        }

    }

    @Override
    public void setVoltageLimit(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        voltageComp[channel] = value;
    }

    @Override
    public double getVoltageLimit(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return voltageComp[channel];
    }

    @Override
    public void setCurrentLimit(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        currentComp[channel] = value;
    }

    @Override
    public double getCurrentLimit(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return currentComp[channel];
    }

    @Override
    public void setIntegrationTime(int channel, double time) throws DeviceException, IOException {
        checkChannel(channel);
        intTimes[channel] = time;
    }

    @Override
    public double getIntegrationTime(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return intTimes[channel];
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
        checkChannel(channel);
        Util.errLog.println("Agilent 4155C does not support different off-modes.");
    }

    @Override
    public OffMode getOffMode(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return OffMode.NORMAL;
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
        R_10_nA(-12, 10e-9),
        R_100_nA(-13, 100e-9),
        R_1_uA(-14, 1e-6),
        R_10_uA(-15, 10e-6),
        R_100_uA(-16, 100e-6),
        R_1_mA(-17, 1e-3),
        R_10_mA(-18, 10e-3),
        R_100_mA(-19, 100e-3),
        R_1_A(-20, 1.0);

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

        public static CurrRange fromCurrent(double value) {

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
