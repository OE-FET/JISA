package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Control.Returnable;
import com.sun.javafx.UnmodifiableArrayList;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class K2600B extends MCSMU {

    private static final String[] CHANNELS                   = {"smua", "smub"};
    private static final String   C_QUERY_VOLT               = "print(%s.measure.v())";
    private static final String   C_QUERY_CURR               = "print(%s.measure.i())";
    private static final String   C_QUERY_FUNC               = "print(%s.source.func)";
    private static final String   C_QUERY_OUTPUT             = "print(%s.source.output)";
    private static final String   C_QUERY_SENSE              = "print(%s.sense)";
    private static final String   C_SET_SOURCE               = "%s.source.func = %s";
    private static final String   C_SET_VOLT                 = "%s.source.levelv = %f";
    private static final String   C_SET_CURR                 = "%s.source.leveli = %f";
    private static final String   C_SET_OUTPUT               = "%s.source.output = %s";
    private static final String   C_SET_SENSE                = "%s.sense = %s";
    private static final String   C_SET_AVG_COUNT            = "%s.measure.filter.count = %d";
    private static final String   C_QUERY_AVG_COUNT          = "print(%s.measure.filter.count)";
    private static final String   C_SET_AVG_MODE             = "%s.measure.filter.type = %s";
    private static final String   C_QUERY_AVG_MODE           = "print(%s.measure.filter.type)";
    private static final String   C_SET_AVG_STATE            = "%s.measure.filter.enable = %s";
    private static final String   C_QUERY_AVG_STATE          = "print(%s.measure.filter.enable)";
    private static final String   C_SET_LIMIT                = "%s.source.limit%s = %f";
    private static final String   C_QUERY_LIMIT              = "print(%s.source.limit%s)";
    private static final String   C_SET_SOURCE_RANGE         = "%s.source.range%s = %f";
    private static final String   C_QUERY_SOURCE_RANGE       = "print(%s.source.range%s)";
    private static final String   C_SET_MEASURE_RANGE        = "%s.measure.range%s = %f";
    private static final String   C_QUERY_MEASURE_RANGE      = "print(%s.measure.range%s)";
    private static final String   C_SET_SOURCE_AUTO_RANGE    = "%s.source.autorange%s = %s";
    private static final String   C_QUERY_SOURCE_AUTO_RANGE  = "print(%s.source.autorange%s)";
    private static final String   C_SET_MEASURE_AUTO_RANGE   = "%s.measure.autorange%s = %s";
    private static final String   C_QUERY_MEASURE_AUTO_RANGE = "print(%s.measure.autorange%s)";
    private static final String   C_SET_NPLC                 = "%s.measure.nplc = %f";
    private static final String   C_QUERY_NPLC               = "print(%s.measure.nplc)";
    private static final String   C_QUERY_LFR                = "print(localnode.linefreq)";
    private static final String   SENSE_LOCAL                = "0";
    private static final String   SENSE_REMOTE               = "1";
    private static final String   OUTPUT_ON                  = "1";
    private static final String   OUTPUT_OFF                 = "0";
    private static final String   FILTER_MOVING_MEAN         = "0";
    private static final String   FILTER_REPEAT_MEAN         = "1";
    private static final String   FILTER_MOVING_MEDIAN       = "2";
    private final        double   LINE_FREQUENCY;

    private AMode[]      filterMode  = {AMode.NONE, AMode.NONE};
    private int[]        filterCount = {1, 1};
    private ReadFilter[] filterV     = {null, null};
    private ReadFilter[] filterI     = {null, null};

    // == FILTERS ======================================================================================================
    private class BlankFilter extends BypassFilter {

        public BlankFilter(int channel, String command, Object... args) {
            super(
                    () -> K2600B.this.queryDouble(command, args),
                    (c) -> {
                        K2600B.this.write(C_SET_AVG_COUNT, CHANNELS[channel], 1);
                        K2600B.this.write(C_SET_AVG_MODE, CHANNELS[channel], FILTER_REPEAT_MEAN);
                        K2600B.this.write(C_SET_AVG_STATE, CHANNELS[channel], OUTPUT_OFF);
                    }
            );
        }
    }

    private class MeanRFilter extends BypassFilter {

        public MeanRFilter(int channel, String command, Object... args) {
            super(
                    () -> K2600B.this.queryDouble(command, args),
                    (c) -> {
                        K2600B.this.write(C_SET_AVG_COUNT, CHANNELS[channel], c);
                        K2600B.this.write(C_SET_AVG_MODE, CHANNELS[channel], FILTER_REPEAT_MEAN);
                        K2600B.this.write(C_SET_AVG_STATE, CHANNELS[channel], OUTPUT_ON);
                    }
            );
        }
    }

    private class MeanMFilter extends BypassFilter {

        public MeanMFilter(int channel, String command, Object... args) {
            super(
                    () -> K2600B.this.queryDouble(command, args),
                    (c) -> {
                        K2600B.this.write(C_SET_AVG_COUNT, CHANNELS[channel], c);
                        K2600B.this.write(C_SET_AVG_MODE, CHANNELS[channel], FILTER_MOVING_MEAN);
                        K2600B.this.write(C_SET_AVG_STATE, CHANNELS[channel], OUTPUT_ON);
                    }
            );
        }
    }

    private class MedRFilter extends MedianRepeatFilter {

        public MedRFilter(int channel, String command, Object... args) {
            super(
                    () -> K2600B.this.queryDouble(command, args),
                    (c) -> {
                        K2600B.this.write(C_SET_AVG_COUNT, CHANNELS[channel], 1);
                        K2600B.this.write(C_SET_AVG_MODE, CHANNELS[channel], FILTER_REPEAT_MEAN);
                        K2600B.this.write(C_SET_AVG_STATE, CHANNELS[channel], OUTPUT_OFF);
                    }
            );
        }
    }


    private class MedMFilter extends BypassFilter {

        public MedMFilter(int channel, String command, Object... args) {
            super(
                    () -> K2600B.this.queryDouble(command, args),
                    (c) -> {
                        K2600B.this.write(C_SET_AVG_COUNT, CHANNELS[channel], c);
                        K2600B.this.write(C_SET_AVG_MODE, CHANNELS[channel], FILTER_MOVING_MEDIAN);
                        K2600B.this.write(C_SET_AVG_STATE, CHANNELS[channel], OUTPUT_ON);
                    }
            );
        }
    }

    public K2600B(InstrumentAddress address) throws IOException, DeviceException {

        super(address);

        clearRead();

        // TODO: Check that this IDN check actually works
        try {
            String[] idn = getIDN().split(", ");
            if (!idn[1].trim().substring(0, 8).equals("Model 26")) {
                throw new DeviceException("The instrument at address %s is not a Keithley 2600 series!", address.getVISAAddress());
            }
        } catch (IOException e) {
            throw new DeviceException("The instrument at address %s is not responding!", address.getVISAAddress());
        }

        for (int i = 0; i < getNumChannels(); i++) {
            setAverageMode(i, AMode.NONE);
        }

        LINE_FREQUENCY = queryDouble(C_QUERY_LFR);

    }

    @Override
    public double getVoltage(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return filterV[channel].getValue();

    }

    @Override
    public double getCurrent(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return filterI[channel].getValue();

    }

    @Override
    public void setVoltage(int channel, double voltage) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_VOLT, CHANNELS[channel], voltage);
        setSource(channel, Source.VOLTAGE);

    }

    @Override
    public void setCurrent(int channel, double current) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_CURR, CHANNELS[channel], current);
        setSource(channel, Source.CURRENT);

    }

    @Override
    public void turnOn(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_OUTPUT, CHANNELS[channel], OUTPUT_ON);

    }

    @Override
    public void turnOff(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_OUTPUT, CHANNELS[channel], OUTPUT_OFF);

    }

    @Override
    public boolean isOn(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return query(C_QUERY_OUTPUT, CHANNELS[channel]).trim().equals(OUTPUT_ON);
    }

    @Override
    public void setSource(int channel, Source source) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_SOURCE, CHANNELS[channel], SFunc.fromSMU(source).toString());

    }

    @Override
    public Source getSource(int channel) throws DeviceException, IOException {
        return getSourceMode(channel).toSMU();
    }

    private SFunc getSourceMode(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return SFunc.fromString(query(C_QUERY_FUNC, CHANNELS[channel]));
    }

    private SFunc getMeasureMode(int channel) throws DeviceException, IOException {

        switch (getSourceMode(channel)) {

            case VOLTAGE:
                return SFunc.CURRENT;

            case CURRENT:
                return SFunc.VOLTAGE;

            default:
                return SFunc.CURRENT;

        }

    }

    @Override
    public void setBias(int channel, double level) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        switch (getSource(channel)) {

            case VOLTAGE:
                setVoltage(channel, level);
                break;

            case CURRENT:
                setCurrent(channel, level);
                break;

            default:
                setVoltage(channel, level);
                break;

        }

    }

    @Override
    public double getSourceValue(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        switch (getSource(channel)) {

            case VOLTAGE:
                return getVoltage(channel);

            case CURRENT:
                return getCurrent(channel);

            default:
                return getVoltage(channel);

        }

    }

    @Override
    public double getMeasureValue(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        switch (getSource(channel)) {

            case VOLTAGE:
                return getCurrent(channel);

            case CURRENT:
                return getVoltage(channel);

            default:
                return getCurrent(channel);

        }
    }

    @Override
    public int getNumChannels() {
        return 2;
    }

    @Override
    public void useFourProbe(int channel, boolean fourProbes) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_SENSE, CHANNELS[channel], fourProbes ? SENSE_REMOTE : SENSE_LOCAL);

    }

    @Override
    public boolean isUsingFourProbe(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return query(C_QUERY_SENSE, CHANNELS[channel]).trim().equals(SENSE_REMOTE);

    }

    @Override
    public void setAverageMode(int channel, AMode mode) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        switch (mode) {

            case NONE:
                filterV[channel] = new BlankFilter(channel, C_QUERY_VOLT, CHANNELS[channel]);
                filterI[channel] = new BlankFilter(channel, C_QUERY_CURR, CHANNELS[channel]);
                break;

            case MEAN_REPEAT:
                filterV[channel] = new MeanRFilter(channel, C_QUERY_VOLT, CHANNELS[channel]);
                filterI[channel] = new MeanRFilter(channel, C_QUERY_CURR, CHANNELS[channel]);
                break;

            case MEAN_MOVING:
                filterV[channel] = new MeanMFilter(channel, C_QUERY_VOLT, CHANNELS[channel]);
                filterI[channel] = new MeanMFilter(channel, C_QUERY_CURR, CHANNELS[channel]);
                break;

            case MEDIAN_REPEAT:
                filterV[channel] = new MedRFilter(channel, C_QUERY_VOLT, CHANNELS[channel]);
                filterI[channel] = new MedRFilter(channel, C_QUERY_CURR, CHANNELS[channel]);
                break;

            case MEDIAN_MOVING:
                filterV[channel] = new MedMFilter(channel, C_QUERY_VOLT, CHANNELS[channel]);
                filterI[channel] = new MedMFilter(channel, C_QUERY_CURR, CHANNELS[channel]);
                break;

        }

        filterMode[channel] = mode;
        resetFilters(channel);

    }

    private void resetFilters(int channel) throws IOException, DeviceException {

        filterV[channel].setCount(filterCount[channel]);
        filterI[channel].setCount(filterCount[channel]);

        filterV[channel].setUp();
        filterI[channel].setUp();

        filterV[channel].clear();
        filterI[channel].clear();

    }

    @Override
    public void setAverageCount(int channel, int count) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        filterCount[channel] = count;
        resetFilters(channel);

    }

    @Override
    public int getAverageCount(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return filterCount[channel];

    }

    @Override
    public AMode getAverageMode(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return filterMode[channel];

    }

    @Override
    public void setSourceRange(int channel, double value) throws DeviceException, IOException {

        switch (getSourceMode(channel)) {

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
        checkChannel(channel);
        return queryDouble(C_QUERY_SOURCE_RANGE, CHANNELS[channel], getSourceMode(channel).getSymbol());
    }

    @Override
    public void useAutoSourceRange(int channel) throws DeviceException, IOException {

        switch (getSourceMode(channel)) {

            case VOLTAGE:
                useAutoVoltageRange(channel);
                break;

            case CURRENT:
                useAutoCurrentRange(channel);
                break;

        }
    }

    @Override
    public boolean isSourceRangeAuto(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return query(C_QUERY_SOURCE_AUTO_RANGE, CHANNELS[channel], getSourceMode(channel).getSymbol()).trim().equals(OUTPUT_ON);
    }

    @Override
    public void setMeasureRange(int channel, double value) throws DeviceException, IOException {

        switch (getMeasureMode(channel)) {

            case VOLTAGE:
                setVoltageRange(channel, value);
                break;

            case CURRENT:
                setCurrentRange(channel, value);
                break;

        }

    }

    @Override
    public double getMeasureRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble(C_QUERY_MEASURE_RANGE, CHANNELS[channel], getMeasureMode(channel).getSymbol());
    }

    @Override
    public void useAutoMeasureRange(int channel) throws DeviceException, IOException {

        switch (getMeasureMode(channel)) {

            case VOLTAGE:
                useAutoVoltageRange(channel);
                break;

            case CURRENT:
                useAutoCurrentRange(channel);
                break;

        }

    }

    @Override
    public boolean isMeasureRangeAuto(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return query(C_QUERY_MEASURE_AUTO_RANGE, CHANNELS[channel], getMeasureMode(channel).getSymbol()).trim().equals(OUTPUT_ON);
    }

    @Override
    public void setVoltageRange(int channel, double value) throws DeviceException, IOException {

        checkChannel(channel);
        write(C_SET_MEASURE_AUTO_RANGE, CHANNELS[channel], SFunc.VOLTAGE.getSymbol(), OUTPUT_OFF);
        write(C_SET_SOURCE_AUTO_RANGE, CHANNELS[channel], SFunc.VOLTAGE.getSymbol(), OUTPUT_OFF);
        write(C_SET_SOURCE_RANGE, CHANNELS[channel], SFunc.VOLTAGE.getSymbol(), value);
        write(C_SET_MEASURE_RANGE, CHANNELS[channel], SFunc.VOLTAGE.getSymbol(), value);

    }

    @Override
    public double getVoltageRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble(C_QUERY_SOURCE_RANGE, CHANNELS[channel], SFunc.VOLTAGE.getSymbol());
    }

    @Override
    public void useAutoVoltageRange(int channel) throws DeviceException, IOException {

        checkChannel(channel);
        write(C_SET_MEASURE_AUTO_RANGE, CHANNELS[channel], SFunc.VOLTAGE.getSymbol(), OUTPUT_ON);
        write(C_SET_SOURCE_AUTO_RANGE, CHANNELS[channel], SFunc.VOLTAGE.getSymbol(), OUTPUT_ON);

    }

    @Override
    public boolean isVoltageRangeAuto(int channel) throws DeviceException, IOException {

        checkChannel(channel);
        return query(C_QUERY_SOURCE_AUTO_RANGE, CHANNELS[channel], SFunc.VOLTAGE.getSymbol()).trim().equals(OUTPUT_ON);

    }

    @Override
    public void setCurrentRange(int channel, double value) throws DeviceException, IOException {

        write(C_SET_MEASURE_AUTO_RANGE, CHANNELS[channel], SFunc.CURRENT.getSymbol(), OUTPUT_OFF);
        write(C_SET_SOURCE_AUTO_RANGE, CHANNELS[channel], SFunc.CURRENT.getSymbol(), OUTPUT_OFF);
        write(C_SET_SOURCE_RANGE, CHANNELS[channel], SFunc.CURRENT.getSymbol(), value);
        write(C_SET_MEASURE_RANGE, CHANNELS[channel], SFunc.CURRENT.getSymbol(), value);

    }

    @Override
    public double getCurrentRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble(C_QUERY_SOURCE_RANGE, CHANNELS[channel], SFunc.CURRENT.getSymbol());
    }

    @Override
    public void useAutoCurrentRange(int channel) throws DeviceException, IOException {

        checkChannel(channel);
        write(C_SET_MEASURE_AUTO_RANGE, CHANNELS[channel], SFunc.CURRENT.getSymbol(), OUTPUT_ON);
        write(C_SET_SOURCE_AUTO_RANGE, CHANNELS[channel], SFunc.CURRENT.getSymbol(), OUTPUT_ON);

    }

    @Override
    public boolean isCurrentRangeAuto(int channel) throws DeviceException, IOException {

        checkChannel(channel);
        return query(C_QUERY_SOURCE_AUTO_RANGE, CHANNELS[channel], SFunc.CURRENT.getSymbol()).trim().equals(OUTPUT_ON);

    }

    @Override
    public void setOutputLimit(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        write(C_SET_LIMIT, CHANNELS[channel], getMeasureMode(channel).getSymbol(), value);
    }

    @Override
    public double getOutputLimit(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble(C_QUERY_LIMIT, CHANNELS[channel], getMeasureMode(channel).getSymbol());
    }

    @Override
    public void setVoltageLimit(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        write(C_SET_LIMIT, CHANNELS[channel], SFunc.VOLTAGE.getSymbol(), value);
    }

    @Override
    public double getVoltageLimit(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble(C_QUERY_LIMIT, CHANNELS[channel], SFunc.VOLTAGE.getSymbol());
    }

    @Override
    public void setCurrentLimit(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        write(C_SET_LIMIT, CHANNELS[channel], SFunc.CURRENT.getSymbol(), value);
    }

    @Override
    public double getCurrentLimit(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble(C_QUERY_LIMIT, CHANNELS[channel], SFunc.CURRENT.getSymbol());
    }

    @Override
    public void setIntegrationTime(int channel, double time) throws DeviceException, IOException {
        checkChannel(channel);
        write(C_SET_NPLC, CHANNELS[channel], time * LINE_FREQUENCY);
    }

    @Override
    public double getIntegrationTime(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return queryDouble(C_QUERY_NPLC, CHANNELS[channel]) / LINE_FREQUENCY;
    }

    @Override
    public TType getTerminalType(int channel, Terminals terminals) throws DeviceException, IOException {

        checkChannel(channel);
        if (terminals == Terminals.REAR) {
            return TType.PHOENIX;
        } else {
            return TType.NONE;
        }

    }

    @Override
    public void setTerminals(int channel, Terminals terminals) throws DeviceException, IOException {
        checkChannel(channel);
        if (terminals != Terminals.REAR) {
            throw new DeviceException("Keithley 2600B does not have front terminals", terminals.name());
        }
    }

    @Override
    public Terminals getTerminals(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return Terminals.REAR;
    }

    private enum SFunc {

        VOLTAGE("1", "v", Source.VOLTAGE),
        CURRENT("0", "i", Source.CURRENT);

        private static HashMap<String, SFunc>     fMap = new HashMap<>();
        private static HashMap<SMU.Source, SFunc> sMap = new HashMap<>();

        static {
            for (SFunc f : values()) {
                fMap.put(f.toString(), f);
                sMap.put(f.toSMU(), f);
            }
        }

        private String     tag;
        private String     symbol;
        private SMU.Source smu;

        public static SFunc fromString(String tag) {
            return fMap.getOrDefault(tag, null);
        }

        public static SFunc fromSMU(SMU.Source s) {
            return sMap.getOrDefault(s, null);
        }

        SFunc(String tag, String symbol, SMU.Source smu) {
            this.tag = tag;
            this.symbol = symbol;
            this.smu = smu;
        }

        public String toString() {
            return tag;
        }

        public String getSymbol() {
            return symbol;
        }

        public SMU.Source toSMU() {
            return smu;
        }

    }

}
