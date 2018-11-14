package JISA.Devices;

import JISA.Addresses.InstrumentAddress;

import java.io.IOException;
import java.util.HashMap;

public class K2450 extends SMU {

    // == COMMANDS =====================================================================================================
    private static final String C_MEASURE_VOLTAGE       = ":MEAS:VOLT?";
    private static final String C_MEASURE_CURRENT       = ":MEAS:CURR?";
    private static final String C_MEASURE_RESISTANCE    = ":MEAS:RES?";
    private static final String C_SET_SOURCE_FUNCTION   = ":SOUR:FUNC %s";
    private static final String C_SET_OUTPUT_STATE      = ":OUTP:STATE %s";
    private static final String C_QUERY_SOURCE_FUNCTION = ":SOUR:FUNC?";
    private static final String C_QUERY_OUTPUT_STATE    = ":OUTP:STATE?";
    private static final String C_SET_SOURCE_VALUE      = ":SOUR:%s %f";
    private static final String C_SET_TERMINALS         = ":ROUT:TERM %s";
    private static final String C_QUERY_TERMINALS       = ":ROUT:TERM?";
    private static final String C_SET_PROBE_MODE        = ":SENS:RSEN %s";
    private static final String C_QUERY_PROBE_MODE      = ":SENS:RSEN?";
    private static final String C_SET_AVG_COUNT         = "AVER:COUNT %d";
    private static final String C_QUERY_AVG_COUNT       = "VOLT:AVER:COUNT?";
    private static final String C_SET_AVG_MODE          = "AVER:TCON %s";
    private static final String C_QUERY_AVG_MODE        = "VOLT:AVER:TCON?";
    private static final String C_SET_AVG_STATE         = "AVER %s";
    private static final String C_QUERY_AVG_STATE       = "VOLT:AVER?";
    private static final String C_SET_SRC_RANGE         = ":SOUR:%s:RANG %f";
    private static final String C_QUERY_SRC_RANGE       = ":SOUR:%s:RANG?";
    private static final String C_SET_SRC_AUTO_RANGE    = ":SOUR:%s:RANG:AUTO %s";
    private static final String C_QUERY_SRC_AUTO_RANGE  = ":SOUR:%s:RANG:AUTO?";
    private static final String C_SET_MEAS_RANGE        = ":SENS:%s:RANG %f";
    private static final String C_QUERY_MEAS_RANGE      = ":SENS:%s:RANG?";
    private static final String C_SET_MEAS_AUTO_RANGE   = ":SENS:%s:RANG:AUTO %s";
    private static final String C_QUERY_MEAS_AUTO_RANGE = ":SENS:%s:RANG:AUTO?";
    private static final String C_SET_LIMIT             = ":SOUR:%s:%sLIM %f";
    private static final String C_QUERY_LIMIT           = ":SOUR:%s:%sLIM?";
    private static final String OUTPUT_ON               = "1";
    private static final String OUTPUT_OFF              = "0";

    // == FILTERS ======================================================================================================
    private final MedianRepeatFilter MEDIAN_REPEAT_V = new MedianRepeatFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    private final MedianRepeatFilter MEDIAN_REPEAT_I = new MedianRepeatFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            });

    private final MedianMovingFilter MEDIAN_MOVING_V = new MedianMovingFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    private final MedianMovingFilter MEDIAN_MOVING_I = new MedianMovingFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            });

    private final BypassFilter MEAN_REPEAT_V = new BypassFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter MEAN_REPEAT_I = new BypassFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter MEAN_MOVING_V = new BypassFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "MOVING");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter MEAN_MOVING_I = new BypassFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "MOVING");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter NONE_V = new BypassFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    private final BypassFilter NONE_I = new BypassFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    // == INTERNAL VARIABLES ===========================================================================================
    private ReadFilter filterV     = NONE_V;
    private ReadFilter filterI     = NONE_I;
    private AMode      filterMode  = AMode.NONE;
    private int        filterCount = 1;

    /**
     * Constant values for referring to the FRONT and REAR terminals of the Keithley 2450 SMU
     */
    public static class Terminals {
        public static final int FRONT = 0;
        public static final int REAR  = 1;
    }

    // == CONSTRUCTORS =================================================================================================
    public K2450(InstrumentAddress address) throws IOException, DeviceException {

        super(address);

        clearRead();
        write(":SYSTEM:CLEAR");

        try {

            String[] iden = query("*IDN?").split(",");

            if (!iden[1].trim().equals("MODEL 2450")) {
                throw new DeviceException("Device at address %s is not a Keithley 2450!", address.getVISAAddress());
            }

            setAverageMode(AMode.NONE);

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.getVISAAddress());
        }

    }

    // == METHODS ======================================================================================================
    public void useFourProbe(boolean fourProbe) throws IOException {

        if (fourProbe) {
            write(C_SET_PROBE_MODE, OUTPUT_ON);
        } else {
            write(C_SET_PROBE_MODE, OUTPUT_OFF);
        }

    }

    public boolean isUsingFourProbe() throws IOException {
        return query(C_QUERY_PROBE_MODE).trim().equals(OUTPUT_ON);
    }

    @Override
    public void setAverageMode(AMode mode) throws IOException, DeviceException {

        switch (mode) {

            case NONE:
                filterV = NONE_V;
                filterI = NONE_I;
                break;

            case MEAN_REPEAT:
                filterV = MEAN_REPEAT_V;
                filterI = MEAN_REPEAT_I;
                break;

            case MEAN_MOVING:
                filterV = MEAN_MOVING_V;
                filterI = MEAN_MOVING_I;
                break;

            case MEDIAN_REPEAT:
                filterV = MEDIAN_REPEAT_V;
                filterI = MEDIAN_REPEAT_I;
                break;

            case MEDIAN_MOVING:
                filterV = MEDIAN_MOVING_V;
                filterI = MEDIAN_MOVING_I;
                break;

        }

        filterMode = mode;
        resetFilters();

    }

    private void resetFilters() throws IOException, DeviceException {

        filterV.setCount(filterCount);
        filterI.setCount(filterCount);

        filterV.setUp();
        filterI.setUp();

        filterV.clear();
        filterI.clear();
    }

    @Override
    public void setAverageCount(int count) throws IOException, DeviceException {
        filterCount = count;
        resetFilters();
    }

    @Override
    public AMode getAverageMode() {
        return filterMode;

    }

    @Override
    public int getAverageCount() {
        return filterCount;
    }

    @Override
    public void setSourceRange(double value) throws IOException {

        switch (getSourceMode()) {

            case VOLTAGE:
                setVoltageRange(value);
                break;

            case CURRENT:
                setCurrentRange(value);
        }

    }

    @Override
    public double getSourceRange() throws IOException {

        switch (getSourceMode()) {

            case VOLTAGE:
                return getVoltageRange();

            case CURRENT:
                return getCurrentRange();

            default:
                return getVoltageRange();

        }

    }

    @Override
    public void useAutoSourceRange() throws IOException {

        switch (getSourceMode()) {

            case VOLTAGE:
                useAutoVoltageRange();
                break;

            case CURRENT:
                useAutoSourceRange();
                break;

        }

    }

    @Override
    public boolean isSourceRangeAuto() throws IOException {

        switch (getSourceMode()) {

            case VOLTAGE:
                return isVoltageRangeAuto();

            case CURRENT:
                return isCurrentRangeAuto();

            default:
                return isVoltageRangeAuto();

        }

    }

    @Override
    public void setMeasureRange(double value) throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                setVoltageRange(value);
                break;

            case CURRENT:
                setCurrentRange(value);
        }

    }

    @Override
    public double getMeasureRange() throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                return getVoltageRange();

            case CURRENT:
                return getCurrentRange();

            default:
                return getCurrentRange();

        }

    }

    @Override
    public void useAutoMeasureRange() throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                useAutoVoltageRange();
                break;

            case CURRENT:
                useAutoSourceRange();
                break;

        }

    }

    @Override
    public boolean isMeasureRangeAuto() throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                return isVoltageRangeAuto();

            case CURRENT:
                return isCurrentRangeAuto();

            default:
                return isCurrentRangeAuto();

        }

    }

    @Override
    public void setVoltageRange(double value) throws IOException {
        switch(getSourceMode()) {

            case VOLTAGE:
                write(C_SET_SRC_AUTO_RANGE, Source.VOLTAGE.getTag(), OUTPUT_OFF);
                write(C_SET_SRC_RANGE, Source.VOLTAGE.getTag(), value);
                break;

            case CURRENT:
                write(C_SET_MEAS_AUTO_RANGE, Source.VOLTAGE.getTag(), OUTPUT_OFF);
                write(C_SET_MEAS_RANGE, Source.VOLTAGE.getTag(), value);

        }
    }

    @Override
    public double getVoltageRange() throws IOException {
        return queryDouble(C_QUERY_SRC_RANGE, Source.VOLTAGE.getTag());
    }

    @Override
    public void useAutoVoltageRange() throws IOException {
        write(C_SET_SRC_AUTO_RANGE, Source.VOLTAGE.getTag(), OUTPUT_ON);
    }

    @Override
    public boolean isVoltageRangeAuto() throws IOException {
        return query(C_QUERY_SRC_AUTO_RANGE, Source.VOLTAGE.getTag()).trim().equals(OUTPUT_ON);
    }

    @Override
    public void setCurrentRange(double value) throws IOException {

        switch(getSourceMode()) {

            case CURRENT:
                write(C_SET_SRC_AUTO_RANGE, Source.CURRENT.getTag(), OUTPUT_OFF);
                write(C_SET_SRC_RANGE, Source.CURRENT.getTag(), value);
                break;

            case VOLTAGE:
                write(C_SET_MEAS_AUTO_RANGE, Source.CURRENT.getTag(), OUTPUT_OFF);
                write(C_SET_MEAS_RANGE, Source.CURRENT.getTag(), value);

        }
    }

    @Override
    public double getCurrentRange() throws IOException {
        return queryDouble(C_QUERY_SRC_RANGE, Source.CURRENT.getTag());
    }

    @Override
    public void useAutoCurrentRange() throws IOException {
        write(C_SET_SRC_AUTO_RANGE, Source.CURRENT.getTag(), OUTPUT_ON);
    }

    @Override
    public boolean isCurrentRangeAuto() throws IOException {
        return query(C_QUERY_SRC_AUTO_RANGE, Source.CURRENT.getTag()).trim().equals(OUTPUT_ON);
    }

    @Override
    public void setOutputLimit(double value) throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                setVoltageLimit(value);
                break;

            case CURRENT:
                setCurrentLimit(value);
                break;

        }

    }

    @Override
    public double getOutputLimit() throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                return getVoltageLimit();

            case CURRENT:
                return getCurrentLimit();

            default:
                return getCurrentLimit();

        }

    }

    @Override
    public void setVoltageLimit(double voltage) throws IOException {
        write(C_SET_LIMIT, Source.VOLTAGE.getTag(), Source.VOLTAGE.getSymbol(), voltage);
        write(C_SET_LIMIT, Source.CURRENT.getTag(), Source.VOLTAGE.getSymbol(), voltage);
    }

    @Override
    public double getVoltageLimit() throws IOException {
        return queryDouble(C_QUERY_LIMIT, getSourceMode().getTag(), Source.VOLTAGE.getSymbol());
    }

    @Override
    public void setCurrentLimit(double current) throws IOException {
        write(C_SET_LIMIT, Source.VOLTAGE.getTag(), Source.CURRENT.getSymbol(), current);
        write(C_SET_LIMIT, Source.CURRENT.getTag(), Source.CURRENT.getSymbol(), current);
    }

    @Override
    public double getCurrentLimit() throws IOException {
        return queryDouble(C_QUERY_LIMIT, getSourceMode().getTag(), Source.CURRENT.getSymbol());
    }

    public double getVoltage() throws DeviceException, IOException {
        return filterV.getValue();
    }

    public double getCurrent() throws IOException, DeviceException {
        return filterI.getValue();
    }

    public void turnOn() throws IOException {
        setOutputState(true);
    }

    public void turnOff() throws IOException {
        setOutputState(false);
    }

    public void setOutputState(boolean on) throws IOException {
        write(C_SET_OUTPUT_STATE, on ? OUTPUT_ON : OUTPUT_OFF);
    }

    public void setSource(Source mode) throws IOException {
        write(C_SET_SOURCE_FUNCTION, mode.getTag());
    }

    public SMU.Source getSource() throws IOException {
        return Source.fromTag(query(C_QUERY_SOURCE_FUNCTION)).getSMU();
    }

    public Source getSourceMode() throws IOException {
        return Source.fromTag(query(C_QUERY_SOURCE_FUNCTION));
    }

    public Source getMeasureMode() throws IOException {

        switch (Source.fromTag(query(C_QUERY_SOURCE_FUNCTION))) {
            case VOLTAGE:
                return Source.CURRENT;

            case CURRENT:
                return Source.VOLTAGE;
        }

        return Source.CURRENT;

    }

    public void setSource(SMU.Source source) throws IOException {
        write(C_SET_SOURCE_FUNCTION, Source.fromSMU(source).getTag());
    }

    public boolean isOn() throws IOException {
        return query(C_QUERY_OUTPUT_STATE).equals(OUTPUT_ON);
    }

    public void setVoltage(double voltage) throws IOException {
        setSourceValue(Source.VOLTAGE, voltage);
    }

    public void setCurrent(double current) throws IOException {
        setSourceValue(Source.CURRENT, current);
    }

    public void setBias(double value) throws IOException {

        switch (getSource()) {

            case VOLTAGE:
                setVoltage(value);
                break;

            case CURRENT:
                setCurrent(value);
                break;

        }

    }

    @Override
    public double getSourceValue() throws DeviceException, IOException {

        switch (getSource()) {

            case VOLTAGE:
                return getVoltage();

            case CURRENT:
                return getCurrent();

            default:
                return getVoltage();

        }

    }


    @Override
    public double getMeasureValue() throws DeviceException, IOException {
        switch (getSource()) {

            case VOLTAGE:
                return getCurrent();

            case CURRENT:
                return getVoltage();

            default:
                return getCurrent();

        }

    }

    @Override
    public int getNumTerminals() {
        return 2;
    }

    public void setSourceValue(Source type, double value) throws IOException {
        write(C_SET_SOURCE_VALUE, type.getTag(), value);
        setSource(type);
    }

    public void setTerminals(int terminalIndex) throws IOException, DeviceException {

        if (terminalIndex >= getNumTerminals()) {
            throw new DeviceException("Those terminals do not exist!");
        }

        write(C_SET_TERMINALS, Terms.fromInt(terminalIndex).getTag());
    }

    public int getTerminals() throws IOException {
        return Terms.fromTag(query(C_QUERY_TERMINALS)).toInt();
    }

    public enum Source {

        VOLTAGE("VOLT", "V", SMU.Source.VOLTAGE),
        CURRENT("CURR", "I", SMU.Source.CURRENT);

        private static HashMap<String, Source>     lookup  = new HashMap<>();
        private static HashMap<SMU.Source, Source> convert = new HashMap<>();

        static {
            for (Source mode : Source.values()) {
                lookup.put(mode.getTag(), mode);
                convert.put(mode.getSMU(), mode);
            }
        }

        public static Source fromTag(String tag) {
            return lookup.getOrDefault(tag.trim(), null);
        }

        public static Source fromSMU(SMU.Source orig) {
            return convert.getOrDefault(orig, null);
        }

        private String     tag;
        private String     symbol;
        private SMU.Source orig;

        Source(String tag, String symbol, SMU.Source orig) {
            this.tag = tag;
            this.symbol = symbol;
            this.orig = orig;
        }

        String getTag() {
            return tag;
        }

        String getSymbol() {
            return symbol;
        }

        SMU.Source getSMU() {
            return orig;
        }

    }

    private enum Terms {

        FRONT(0, "FRON"),
        REAR(1, "REAR");

        private static HashMap<String, Terms>  lookup  = new HashMap<>();
        private static HashMap<Integer, Terms> indices = new HashMap<>();

        static {
            for (Terms t : Terms.values()) {
                lookup.put(t.getTag(), t);
                indices.put(t.toInt(), t);
            }
        }

        public static Terms fromTag(String tag) {
            return lookup.getOrDefault(tag.trim(), null);
        }

        public static Terms fromInt(int index) {
            return indices.getOrDefault(index, null);
        }

        private String tag;
        private int    index;

        Terms(int index, String tag) {
            this.index = index;
            this.tag = tag;
        }

        String getTag() {
            return tag;
        }

        int toInt() {
            return index;
        }

    }


}
