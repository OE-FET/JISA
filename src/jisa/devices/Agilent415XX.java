package jisa.devices;

import jisa.addresses.Address;
import jisa.control.*;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.visa.VISADevice;

import java.io.IOException;

import static jisa.devices.Agilent415XX.AgilentRange.AUTO_RANGING;
import static jisa.devices.SMU.OffMode.HIGH_IMPEDANCE;
import static jisa.devices.SMU.OffMode.ZERO;
import static jisa.enums.AMode.*;
import static jisa.enums.Source.*;

public abstract class Agilent415XX extends VISADevice implements SPA {

    public static final String C_RESET = "*RST";
    public static final String C_FLEX  = "US";
    public static final String C_FMT   = "FMT 2";

    private final boolean[] states = {false, false, false, false, false, false};
    private final Source[]  modes  = {VOLTAGE, VOLTAGE, VOLTAGE, VOLTAGE, VOLTAGE, VOLTAGE};

    private final AgilentRange[] voltageRange = {AUTO_RANGING, AUTO_RANGING, AUTO_RANGING, AUTO_RANGING, AUTO_RANGING, AUTO_RANGING};
    private final AgilentRange[] currentRange = {AUTO_RANGING, AUTO_RANGING, AUTO_RANGING, AUTO_RANGING, AUTO_RANGING, AUTO_RANGING};

    private final OffMode[] offModes = {HIGH_IMPEDANCE, HIGH_IMPEDANCE, HIGH_IMPEDANCE, HIGH_IMPEDANCE, HIGH_IMPEDANCE, HIGH_IMPEDANCE};

    private final double[] currentComp = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
    private final double[] voltageComp = {40, 40, 40, 40, 40, 40};

    private final double[] intTimes    = {0.02, 0.02, 0.02, 0.02, 0.02, 0.02};
    private       double   lastIntTime = -1;

    private final ReadFilter[] voltageFilters = {
        makeVoltageFilter(0, NONE),
        makeVoltageFilter(1, NONE),
        makeVoltageFilter(2, NONE),
        makeVoltageFilter(3, NONE),
        makeVoltageFilter(4, NONE),
        makeVoltageFilter(5, NONE)
    };

    private final int[]   filterCounts = {1, 1, 1, 1, 1, 1};
    private final AMode[] filterModes  = {NONE, NONE, NONE, NONE, NONE, NONE};

    private final ReadFilter[] currentFilters = {
        makeCurrentFilter(0, NONE),
        makeCurrentFilter(1, NONE),
        makeCurrentFilter(2, NONE),
        makeCurrentFilter(3, NONE),
        makeCurrentFilter(4, NONE),
        makeCurrentFilter(5, NONE)
    };

    private final double[] compliance = {100, 100, 100, 100, 100, 100};
    private final double[] values     = {0, 0, 0, 0, 0, 0};

    public Agilent415XX(Address address) throws IOException, DeviceException {

        super(address);

        setWriteTerminator("\r");
        setReadTerminator("\r");

        write(C_RESET);
        write(C_FLEX);
        write(C_FMT);

        String[] idn = getIDN().split(",");

        try {
            if (!idn[1].contains("415")) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new DeviceException("Device at %s is not an Agilent 415XX series.", address.toString());
        }

        for (int i = 0; i < getNumChannels(); i++) {
            turnOff(i);
            write("FL %d,0", i + 1);
        }

    }

    public String getChannelName(int channel) {
        try {
            checkChannel(channel);
            return String.format("SMU %d", channel + 1);
        } catch (Exception e) {
            return "Unknown Channel";
        }
    }

    public String getChannelName() {
        return "Agilent SPA";
    }

    protected abstract AgilentRange rangeFromVoltage(int channel, double voltage);

    protected abstract AgilentRange rangeFromCurrent(int channel, double current);

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

            if (lastIntTime <= 10.16e-3) {
                write("SIT 1,%e", lastIntTime);
                write("SLI 1");
            } else if (lastIntTime >= 16.7e-3) {
                write("SIT 3,%e", lastIntTime);
                write("SLI 3");
            } else {
                write("SIT 1,10.16E-3");
                write("SIT 3,%e", 2.0 * lastIntTime - 10.16e-3);
                write("SLI 2");                                   // Basically one power-line cycle
            }

            write("SIT %d,%e", mode, lastIntTime);
            write("SLI %d", mode);
        }

    }

    protected double measureVoltage(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        if (states[channel]) {
            return queryDouble("TV? %d,%d", channel + 1, voltageRange[channel].toInt());
        } else {
            return 0.0;
        }
    }

    protected double measureCurrent(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        if (states[channel]) {
            return queryDouble("TI? %d,%d", channel + 1, currentRange[channel].toInt());
        } else {
            return 0.0;
        }
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

    private double getVoltComp(int channel, double range) {
        return Math.min(voltageComp[channel], rangeFromCurrent(channel, range).getCompliance());
    }

    private double getCurrComp(int channel, double range) {
        return Math.min(currentComp[channel], rangeFromVoltage(channel, range).getCompliance());
    }

    @Override
    public void setVoltage(int channel, double voltage) throws DeviceException, IOException {
        checkChannel(channel);
        modes[channel]  = VOLTAGE;
        values[channel] = voltage;
        if (states[channel]) {
            write("DV %d,%d,%e,%e", channel + 1, voltageRange[channel].toInt(), voltage, getCurrComp(channel, voltage));
        }
    }

    @Override
    public void setCurrent(int channel, double current) throws DeviceException, IOException {
        checkChannel(channel);
        modes[channel]  = CURRENT;
        values[channel] = current;
        if (states[channel]) {
            write("DI %d,%d,%e,%e", channel + 1, currentRange[channel].toInt(), current, getVoltComp(channel, current));
        }
    }

    @Override
    public void turnOn(int channel) throws DeviceException, IOException {

        checkChannel(channel);
        write("CN %d", channel + 1);
        states[channel] = true;

        switch (modes[channel]) {

            case VOLTAGE:
                setVoltage(channel, values[channel]);
                break;

            case CURRENT:
                setCurrent(channel, values[channel]);
                break;

        }

    }

    @Override
    public void turnOff(int channel) throws DeviceException, IOException {

        checkChannel(channel);

        switch (offModes[channel]) {

            case HIGH_IMPEDANCE:
                write("CL %d", channel + 1);
                break;

            case GUARD:
            case ZERO:
            case NORMAL:
                write("DZ %d", channel + 1);
                break;

        }

        states[channel] = false;

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
    public void setFourProbeEnabled(int channel, boolean fourProbes) throws DeviceException, IOException {
        checkChannel(channel);
    }

    @Override
    public boolean isFourProbeEnabled(int channel) throws DeviceException, IOException {
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
        voltageRange[channel] = rangeFromVoltage(channel, value);
    }

    @Override
    public double getVoltageRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return voltageRange[channel].getRange();
    }

    @Override
    public void useAutoVoltageRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        voltageRange[channel] = AUTO_RANGING;
    }

    @Override
    public boolean isAutoRangingVoltage(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return voltageRange[channel] == AUTO_RANGING;
    }

    @Override
    public void setCurrentRange(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        currentRange[channel] = rangeFromCurrent(channel, value);
    }

    @Override
    public double getCurrentRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return currentRange[channel].getRange();
    }

    @Override
    public void useAutoCurrentRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        currentRange[channel] = AUTO_RANGING;
    }

    @Override
    public boolean isAutoRangingCurrent(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return currentRange[channel] == AUTO_RANGING;
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
        return (terminals == Terminals.REAR) ? TType.TRIAX : TType.NONE;
    }

    @Override
    public void setTerminals(int channel, Terminals terminals) throws DeviceException, IOException {
        checkChannel(channel);
    }

    @Override
    public Terminals getTerminals(int channel) throws DeviceException, IOException {
        return Terminals.REAR;
    }

    @Override
    public void setOffMode(int channel, OffMode mode) throws DeviceException, IOException {

        checkChannel(channel);

        switch (mode) {

            case HIGH_IMPEDANCE:
                offModes[channel] = mode;
                break;

            case GUARD:
            case ZERO:
            case NORMAL:
                offModes[channel] = ZERO;
                break;

        }

        if (!isOn(channel)) {
            write("CN %d", channel + 1);
            turnOff(channel);
        }

    }

    @Override
    public OffMode getOffMode(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return offModes[channel];
    }

    public enum UnitType {
        SMU,
        PGU,
        GNDU,
        HPSMU
    }

    protected interface AgilentRange {

        AgilentRange AUTO_RANGING = new AgilentRange() {
            @Override
            public int toInt() {
                return 0;
            }

            @Override
            public double getRange() {
                return -1;
            }

            @Override
            public double getCompliance() {
                return -1;
            }
        };

        int toInt();

        double getRange();

        double getCompliance();

    }


}
