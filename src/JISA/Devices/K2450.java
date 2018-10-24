package JISA.Devices;

import JISA.Addresses.InstrumentAddress;

import java.io.IOException;
import java.util.HashMap;

public class K2450 extends SMU {

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
    private static final String C_QUERY_AVG_COUNT       = "AVER:COUNT?";
    private static final String C_SET_AVG_MODE          = "AVER:TCON %s";
    private static final String C_QUERY_AVG_MODE        = "AVER:TCON?";
    private static final String C_SET_AVG_STATE         = "AVER %s";
    private static final String C_QUERY_AVG_STATE       = "AVER?";
    private static final String OUTPUT_ON               = "1";
    private static final String OUTPUT_OFF              = "0";

    private final MedianRepeatFilter mrfV = new MedianRepeatFilter(() -> queryDouble(C_MEASURE_VOLTAGE));
    private final MedianRepeatFilter mrfI = new MedianRepeatFilter(() -> queryDouble(C_MEASURE_CURRENT));
    private final MedianMovingFilter mmfV = new MedianMovingFilter(() -> queryDouble(C_MEASURE_VOLTAGE));
    private final MedianMovingFilter mmfI = new MedianMovingFilter(() -> queryDouble(C_MEASURE_CURRENT));
    private final BypassFilter       bpV  = new BypassFilter(() -> queryDouble(C_MEASURE_VOLTAGE));
    private final BypassFilter       bpI  = new BypassFilter(() -> queryDouble(C_MEASURE_CURRENT));

    private ReadFilter filterV     = bpV;
    private ReadFilter filterI     = bpI;
    private boolean    usingFilter = false;
    private AMode      filterMode  = AMode.MEAN_REPEAT;

    /**
     * Constant values for referring to the FRONT and REAR terminals of the Keithley 2450 SMU
     */
    public static class Terminals {
        public static final int FRONT = 0;
        public static final int REAR  = 1;
    }

    public K2450(InstrumentAddress address) throws IOException, DeviceException {

        super(address);

        try {

            String[] iden = query("*IDN?").split(",");

            if (!iden[1].trim().equals("MODEL 2450")) {
                throw new DeviceException("Device at address %s is not a Keithley 2450!", address.getVISAAddress());
            }

            setAverageMode(AMode.MEAN_REPEAT);
            useAverage(false);

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.getVISAAddress());
        }

    }

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
    public void setAverageMode(AMode mode) throws IOException {
        String code = "";

        switch (mode) {

            case MEAN_REPEAT:
                filterV = bpV;
                filterI = bpI;
                write(C_SET_AVG_MODE, "REPEAT");
                break;

            case MEAN_MOVING:
                filterV = bpV;
                filterI = bpI;
                write(C_SET_AVG_MODE, "MOVING");
                break;

            case MEDIAN_REPEAT:
                filterV = mrfV;
                filterI = mrfI;
                filterV.setCount(getAverageCount());
                filterI.setCount(getAverageCount());
                write(C_SET_AVG_MODE, "REPEAT");
                break;

            case MEDIAN_MOVING:
                filterV = mmfV;
                filterI = mmfI;
                filterV.setCount(getAverageCount());
                filterI.setCount(getAverageCount());
                write(C_SET_AVG_MODE, "REPEAT");
                break;

        }

        useAverage(isAverageUsed());

    }

    @Override
    public void setAverageCount(int count) throws IOException {
        write(C_SET_AVG_COUNT, count);
        mmfV.setCount(count);
        mmfI.setCount(count);
        mrfV.setCount(count);
        mrfI.setCount(count);
    }

    @Override
    public AMode getAverageMode() throws IOException {

        if (filterV instanceof BypassFilter) {
            String response = query(C_QUERY_AVG_MODE).trim();
            if (response.equals("MOV")) {
                return AMode.MEAN_MOVING;
            } else if (response.equals("REP")) {
                return AMode.MEAN_REPEAT;
            }
        } else {
            if (filterV instanceof MedianRepeatFilter) {
                return AMode.MEDIAN_REPEAT;
            } else if (filterV instanceof MeanMovingFilter) {
                return AMode.MEDIAN_MOVING;
            }
        }

        return AMode.MEAN_REPEAT;

    }

    @Override
    public void useAverage(boolean use) throws IOException {

        if (filterV instanceof BypassFilter) {
            write(C_SET_AVG_STATE, OUTPUT_OFF);
        } else {
            write(C_SET_AVG_STATE, use ? OUTPUT_ON : OUTPUT_OFF);
        }

        usingFilter = use;
    }

    @Override
    public boolean isAverageUsed() {
        return usingFilter;
    }

    @Override
    public int getAverageCount() {
        return filterV.getCount();
    }

    public double getVoltage() throws DeviceException, IOException {
        return isAverageUsed() ? filterV.getValue() : bpV.getValue();
    }

    public double getCurrent() throws IOException, DeviceException {
        return isAverageUsed() ? filterI.getValue() : bpI.getValue();
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

        VOLTAGE("VOLT", SMU.Source.VOLTAGE),
        CURRENT("CURR", SMU.Source.CURRENT);

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
        private SMU.Source orig;

        Source(String tag, SMU.Source orig) {
            this.tag = tag;
            this.orig = orig;
        }

        String getTag() {
            return tag;
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
